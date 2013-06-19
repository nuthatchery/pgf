module org::nuthatchery::pgf::rascal::engines::Spacer

import org::nuthatchery::pgf::rascal::ListOp;
import org::nuthatchery::pgf::rascal::Token;
import org::nuthatchery::pgf::rascal::Tseq;
import org::nuthatchery::pgf::rascal::TableBuilder;
import org::nuthatchery::pgf::rascal::engines::FmtEngine;

import IO;
import List;
import String;

public alias DecisionTable = map[tuple[TokenCat current, TokenCat lastSeen], Decision];

alias SpacerMode = value;

data SpacerContext
	= SpacerContext(
		list[SpacerMode] modeStack,
		SpacerContext nextContext,
		int nesting,
		DecisionTable table,
		TokenCat lastSeen
	)
	| NilContext()
	;
	
data Decision
	= Insert(Token tok)
	| EnterContext(rel[TokenCat, TokenCat, Decision] newTable)
	| ExitContext()
    | Sequence(list[Decision] todo)
	| ModeChoice(map[SpacerMode,Decision] choiceMap, Decision choiceDefault)
    | Nothing() // void action
    | Skip() // skips entire command sequence
    | Break() // skips rest of command sequence
	;
	
public SpacerContext SpacerContext() {
	return SpacerContext([], NilContext(), 0, (), "");
}

public SpacerContext SpacerContext(DecisionTable table) {
	return SpacerContext([], NilContext(), 0, table, "");
}

public SpacerContext enterContext(SpacerContext ctx, DecisionTable table) {
	return SpacerContext(ctx.modeStack, ctx, ctx.nesting + 1, ctx.table);
}

public SpacerContext exitContext(SpacerContext ctx) {
	next = ctx.nextContext;
	if(next == NilContext())
		throw "exitContext: Exiting from last context";
	else if(next.nesting != ctx.nesting-1)
		throw "exitContext: Nesting Error";
	else if(next.modeStack != ctx.modeStack)
		throw "exitContext: Mode Stack Nesting Error";
		
	return ctx.nextContext;
}

public tuple[Tseq outToks, SpacerContext ctx] processToken(Token tok, Tseq outToks, SpacerContext ctx) {
  if (NilToken() := tok)
    return <outToks, ctx>;
  if(getCategory(tok) == ".")
  	return <put(tok, outToks), ctx>;
	
  decision = ctx.table[<getCategory(tok), ctx.lastSeen>] ? Nothing();
  //println("<getCategory(tok)>, <ctx.lastSeen> =\> <decision>");
  list[Decision] decisions;
  if(Sequence(ds) := decision)
    decisions = ds;
  else
    decisions = [decision];
  
  for(d <- decisions) {
    switch(d) {
    case Nothing():
      ;
    case Insert(t):
      outToks = put(t, outToks);
    case Skip():
      return <outToks, ctx>;
    case Break():
      break;
    case EnterContext(tbl):
      ctx = enterContext(ctx, tbl);
    case ExitContext():
      ctx = exitContext(ctx);
    }
  }
  
  outToks = put(tok, outToks);
  ctx.lastSeen = getCategory(tok);
  return <outToks, ctx>;
}

public tuple[Tseq inToks, Tseq outToks, SpacerContext ctx] processTokens(tuple[Tseq inToks, Tseq outToks, SpacerContext ctx] info) {
  <inToks, outToks, ctx> = info;
  while (true) {
    <tok, inToks> = tseqGet(inToks);
    if (tok == Eof())
      break;
    <outToks, ctx> = processToken(tok, outToks, ctx);
  }
  return <inToks, outToks, ctx>;
}

public set[TokenCat] stdCategories = {"TXT", "SPC", "LPAREN", "RPAREN", "LBRACE", "RBRACE", "LBRACKET", "RBRACKET", "BINOP", "PREOP", "COMMA", "KEYWORD", "NUM", "SEMICOLON"};

public DecisionTable alwaysSpaceTable = makeTable((
		<"*",""> : Nothing(),
		<"*","*"> : Insert(Space(" "))
	), stdCategories);
public DecisionTable spaceTable = makeTable(( 
		<"SPC", "*"> : Nothing(),
		<"*", ""> : Nothing(),
		<"*", "SPC"> : Nothing(),
		<"*", "*"> : Insert(Space(" "))
	), stdCategories);
public DecisionTable reSpaceTable = makeTable((
		<"SPC", "*"> : Skip(),
		<"*", ""> : Nothing(),
		<"*", "LPAREN"> : Nothing(), 
		<"*", "LBRACKET"> : Nothing(),
		<"LPAREN", "*"> : Nothing(),
		<"RPAREN", "*"> : Nothing(),
		<"RBRACKET", "*"> : Nothing(),
		<"*", "LBRACE"> : Insert(Line()),
		<"RBRACE", "*"> : Insert(Line()),
		<"*", "RBRACE"> : Insert(Line()),
		<"COMMA", "*"> : Nothing(),
		<"*", "COMMA"> : Insert(Space(" ")),
		<"*", "PREOP"> : Nothing(),
		<"*", "SEMICOLON"> : Insert(Line()),
		<"SEMICOLON", "*"> : Nothing(),
		<"*", "DOT"> : Nothing(),
		<"DOT", "*"> : Nothing(),
		<"*", "*"> : Insert(Space(" "))
	), stdCategories);

public void printlnTokenStream(Tseq toks) {
	while(true) {
		<tok, toks> = tseqGet(toks);
		switch(tok) {
			case Text(t):
				print(" \"<t>\":<getCategory(tok)> ");
			case Space(_): {
                print(" \" \":<getCategory(tok)> ");
			}
			case Line(): {
				print(" \"\\n\":<getCategory(tok)> ");
			}
			case Nest(LvPop()):
				print("[");
			case Nest(_):
				print("]");
            case Eof():
                break;
			default:
				print(" [<tok>]:<getCategory(tok)> ");
		}
	}
	println("");
}
