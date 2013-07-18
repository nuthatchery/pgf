module org::nuthatchery::pgf::rascal::tests::TestPrettyMagnolia

import org::nuthatchery::pgf::rascal::Token;
import org::nuthatchery::pgf::rascal::Tseq;
import org::nuthatchery::pgf::rascal::engines::FmtEngine;
import org::nuthatchery::pgf::rascal::engines::Spacer;
import org::nuthatchery::pgf::rascal::AstToStream;
import org::magnolialang::magnolia::AbstractSyntax;
import org::nuthatchery::pica::terms::Terms;
import org::magnolialang::magnolia::AbstractSyntax;
import org::magnolialang::magnolia::lib::Names;
import org::magnolialang::magnolia::lib::MagnoliaCompileLib;
import org::nuthatchery::pgf::rascal::TableBuilder;

import org::magnolialang::magnolia::MagnoliaFacts;

import org::magnolialang::magnolia::resources::Resources;

import IO;
import String;
import Type; // for typeOf

/*

This is a playground for testing different pretty printings of
Magnolia. Not maintained as such. See Pretty.rsc for the actual
Magnolia pretty printer.

(init-compiler)
(rimport "org::nuthatchery::pgf::rascal::tests::TestPrettyMagnolia")

(run-pretty-magnolia "basic.Basic" false)
(run-pretty-magnolia "basic.Integer" false)
(run-pretty-magnolia "errors.ConstantOverloading" false)
(run-pretty-magnolia "mutification.Integer" false)
(run-pretty-magnolia "mutification.Mutification" false)
(run-pretty-magnolia "mutification.Mutification2" false)
(run-pretty-magnolia "override.Override" false)
(run-pretty-magnolia "scoping.Scoping" false)
(run-pretty-magnolia "toplevel.Operators" false)
(run-pretty-magnolia "toplevel.Parens" false)
(run-pretty-magnolia "toplevel.Predicates" false)
(run-pretty-magnolia "toplevel.Renaming" false)
(run-pretty-magnolia "typecheck.OnDefines" false)
(run-pretty-magnolia "typecheck.Requires" false)
(run-pretty-magnolia "typecheck.TypeDecl" false)

(print-ast-as-mg (ctor-get-val (get-fact-imploded-tree "basic.Basic")))
(print-ast-as-mg (ctor-get-val (get-fact-desugared-tree "basic.Basic")))

(defn run-pretty-magnolia [s rich?]
  (rcall "runPrettyMagnolia" (get-transaction) (rstring s) (rbool rich?)))

*/

public void runPrettyMagnolia(Transaction tr, str modName, bool isRich) {
  name = strToName(modName);
  //tr = startTransaction();

  // print source text
  println(getFact(tr, #ParseTree, name).val);

  // #TypeChecked form is simpler in a way as the syntax is less rich.
  // But we want printing of both to work.

  AST tree;
  if (isRich)
    tree = getFact(tr, #ImplodedTree, name).val;
  else
    tree = getFact(tr, #TypeChecked, name).val;

  //endTransaction(tr);
  //println(yieldTerm(tree, false));
  
  stream = pp(tree, tseqNull);
  //println(stream);

  ctx = SpacerContext(mgSpaceTable);
  <_, stream, _> = processTokens(<stream, tseqNull, ctx>);
  //println(stream);
  printlnTokenStream(stream);
  println(pretty(80, stream));
}
 
default Tseq pp(AST ast, Tseq stream) {
  return put(Text("[[no pp rule for <ast>]]"), stream);
}

Token SemiColon = Text(";")[@cat="SEMICOLON"];
Token Colon = Text(":")[@cat="COLON"];
Token Comma = Text(",")[@cat="COMMA"];
Token LParen = Text("(")[@cat="LPAREN"];
Token RParen = Text(")")[@cat="RPAREN"];
Token LBrace = Text("{")[@cat="LBRACE"];
Token RBrace = Text("}")[@cat="RBRACE"];
Token LBracket = Text("[")[@cat="LBRACKET"];
Token RBracket = Text("]")[@cat="RBRACKET"];
Token Keyword(str s) { return Text(s)[@cat="KEYWORD"]; }
Token PreOp(str s) { return Text(s)[@cat="PREOP"]; }
Token BinOp(str s) { return Text(s)[@cat="BINOP"]; }

// These semantic tokens may be useful later.
Token Begin(str cat) { return NilToken(); }
Token End(str cat) { return NilToken(); }

// For use in spacing table.
Token BeginIndent = Nest(LvInc(4));
Token EndIndent = Nest(LvPop());

// --------------------------------------------------
// 
// --------------------------------------------------

DecisionTable mgSpaceTable = makeTable((
  <"SPC", "*"> : Skip(),
  <"*", "COMMA"> : Insert(Space(" ")),
  <"TOKEN", "TOKEN"> : Insert(Space(" ")),
  <"TOKEN", "KEYWORD"> : Insert(Space(" ")),
  <"KEYWORD", "TOKEN"> : Insert(Space(" ")),
  <"KEYWORD", "KEYWORD"> : Insert(Space(" ")),
  <"KEYWORD", "RBRACKET"> : Insert(Space(" ")),
  <"PREOP", "KEYWORD"> : Insert(Space(" ")),
  <"LPAREN", "KEYWORD"> : Insert(Space(" ")),
  <"*", "BINOP"> : Insert(Space(" ")),
  <"BINOP", "*"> : Insert(Space(" ")),
  <"*", "COLON"> : Insert(Space(" ")),
  <"COLON", "*"> : Insert(Space(" ")),
  <"LBRACE", "KEYWORD"> : Insert(Space(" ")),
  <"LBRACE", "RPAREN"> : Insert(Space(" ")),
  <"KEYWORD", "RPAREN"> : Insert(Space(" ")),
  <"TOKEN", "RPAREN"> : Insert(Space(" ")),
  <"*", "LBRACE"> : Sequence([Insert(BeginIndent), Insert(Line())]),
  <"RBRACE", "SEMICOLON"> : Sequence([Insert(EndIndent), Insert(Line())]),
  <"RBRACE", "*"> : Sequence([Insert(EndIndent), Insert(Line())]),
  <"RBRACE", "RBRACE"> : Sequence([Insert(EndIndent), Insert(Line())]),
  <"SEMICOLON", "RBRACE"> : Nothing(),
  <"*", "RBRACE"> : Insert(Line()),
  <"*", "SEMICOLON"> : Insert(Line()),
  <"*", "LINECOMMENT"> : Insert(Line()),
  <"*", "BLOCKCOMMENT"> : Insert(Line()),
  <"*", "*"> : Nothing()), {"LBRACKET"});

// --------------------------------------------------
// 
// --------------------------------------------------

Tseq pp(leaf(s), Tseq stream) {
	return ast2stream(stream, pp, Text(s));
}

Tseq pp(seq(trees), Tseq stream) {
	for(tree <- trees)
		stream = pp(tree, stream);
	return stream;
}

Tseq pp(Abstract(), Tseq stream) {
  return ast2stream(stream, pp, Keyword("abstract"));
}

Tseq pp(AnonParam(arg0, arg1), Tseq stream) {
  return ast2stream(stream, pp, arg0, arg1);
}

Tseq pp(Apply(arg0, arg1), Tseq stream) {
  return ast2stream(stream, pp, arg0, LParen, SepBy(arg1, Comma), RParen);
}

Tseq pp(Assert(arg0, arg1), Tseq stream) {
  return ast2stream(stream, pp, Keyword("assert"), arg0, arg1, SemiColon);
}

Tseq pp(Assign(arg0, arg1), Tseq stream) {
  return ast2stream(stream, pp, arg0, BinOp("="), arg1, SemiColon);
}

Tseq pp(Attr(arg0), Tseq stream) {
  return ast2stream(stream, pp, arg0);
}

Tseq pp(Attr(arg0, arg1), Tseq stream) {
  return ast2stream(stream, pp, arg0, LParen, SepBy(arg1, Comma), RParen);
}

Tseq pp(Attrs(arg0), Tseq stream) {
  return ast2stream(stream, pp, LBracket, SepBy(arg0, Comma), RBracket);
}

Tseq pp(Axiom(), Tseq stream) {
  return ast2stream(stream, pp, Text("\\axiom"));
}

Tseq pp(AxiomClause(arg0, arg1), Tseq stream) {
  return ast2stream(stream, pp, Keyword("axiom"), arg0, arg1);
}

Tseq pp(t : BinOpSugar(arg0, leaf(arg1), arg2), Tseq stream) {
  //println(typeOf(arg1)); // arg1 is of AST
  return ast2stream(stream, pp, arg0, BinOp(arg1), arg2);
}

Tseq pp(Block(arg0), Tseq stream) {
  return ast2stream(stream, pp, LBrace, arg0, RBrace);
}

Tseq pp(BlockExpr(arg0), Tseq stream) {
  return ast2stream(stream, pp, LBrace, arg0, RBrace);
}

Tseq pp(BlockExprSugar(arg0, arg1), Tseq stream) {
  return ast2stream(stream, pp, Keyword("begin"), LBrace, arg0, arg1, RBrace);
}

Tseq pp(Body(arg0), Tseq stream) {
  if(Block(_) := arg0 || BlockExpr(_) := arg0)
  	return ast2stream(stream, pp, BinOp("="), arg0);
  else
  	return ast2stream(stream, pp, BinOp("="), arg0, SemiColon);
}

Tseq pp(Break(), Tseq stream) {
  return ast2stream(stream, pp, Keyword("break"), SemiColon);
}

Tseq pp(Builtin(), Tseq stream) {
  return ast2stream(stream, pp, Keyword("builtin"));
}

Tseq pp(Builtin(n,_), Tseq stream) {
  return ast2stream(stream, pp, n);
}

Tseq pp(By(arg0), Tseq stream) {
  return ast2stream(stream, pp, Keyword("by"), arg0);
}

Tseq pp(Call(arg0, arg1), Tseq stream) {
  return ast2stream(stream, pp, Keyword("call"), arg0, LParen, SepBy(arg1, Comma), RParen, SemiColon);
}

Tseq pp(ConceptDef(arg0, arg1, arg2, arg3), Tseq stream) {
  return ast2stream(stream, pp, arg0, Begin("DEF"), Keyword("concept"), arg1, arg2, BinOp("="), arg3, End("DEF"));
}

Tseq pp(Congruence(), Tseq stream) {
  return ast2stream(stream, pp, Text("\\EQ"));
}

Tseq pp(Congruence(arg0), Tseq stream) {
  return ast2stream(stream, pp, Keyword("congruence"), arg0, SemiColon);
}

Tseq pp(CongruenceClause(arg0, arg1), Tseq stream) {
  return ast2stream(stream, pp, Keyword("congruence"), arg0, arg1);
}

Tseq pp(CongruenceOn(arg0, arg1), Tseq stream) {
  return ast2stream(stream, pp, Keyword("congruence"), arg0, Keyword("on"), arg1, SemiColon);
}

Tseq pp(DataInvariant(), Tseq stream) {
  return ast2stream(stream, pp, Text("\\DI"));
}

Tseq pp(DataInvariantClause(arg0, arg1), Tseq stream) {
  return ast2stream(stream, pp, Keyword("datainvariant"), arg0, arg1);
}

Tseq pp(DecimalIntegerLiteral(decimal), Tseq stream) {
  return ast2stream(stream, pp, arg0);
}

Tseq pp(Decl(), Tseq stream) {
  return ast2stream(stream, pp, Text("\\decl"));
}

Tseq pp(Decl(arg0, arg1, arg2, arg3), Tseq stream) {
  return ast2stream(stream, pp, arg0, arg1, arg2, arg3);
}

Tseq pp(DeclBody(arg0), Tseq stream) {
  return ast2stream(stream, pp, LBrace, arg0, RBrace);
}

Tseq pp(DeclList(), Tseq stream) {
  return ast2stream(stream, pp, Text("\\decllist"));
}

Tseq pp(DeclaredFilter(arg0, arg1, arg2), Tseq stream) {
  return ast2stream(stream, pp, arg0, Keyword("declared"), arg1, arg2);
}

Tseq pp(Default(), Tseq stream) {
  return ast2stream(stream, pp, Keyword("default"));
}

Tseq ppDefClause(list[AST] attrs, AST arg1, Tseq stream) {
	switch(arg1) {
		case FunClause(n, ps, t): {
			kind = "function";
			if(Predicate() in attrs)
				kind = "predicate";
			else if(Axiom() in attrs)
				kind = "axiom";
			else if(DataInvariant() in attrs)
				kind = "datainvariant";
			else if(Congruence() in attrs)
				kind = "congruence";
			stream = put(Keyword(kind), stream);
			stream = pp(unqualify(basename(n)), stream);
			stream = ppFunParams(ps, stream);
			if(kind == "function") {
				stream = put(Colon, stream);
				stream = pp(basename(t), stream);
			}
		}
		case ProcClause(n, ps): {
			kind = "procedure";
			if(Test() in attrs) {
				stream = ast2stream(stream, pp, Keyword("test"), unqualify(basename(n)));
				stream = ppFunParams(ps, stream);
			}
			else {
				stream = ast2stream(stream, pp, Keyword("procedure"), unqualify(basename(n)));
				stream = ppProcParams(ps, stream);
			}
		}
        case PredClause(n, ps): { //xxx can this have modifiers?
            stream = ast2stream(stream, pp, Keyword("predicate"), unqualify(basename(n)));
            stream = ppFunParams(ps, stream);
        } 
        case AxiomClause(n, ps): { //xxx can this have modifiers? xxx print as axiom, theorem, or proof?
            stream = ast2stream(stream, pp, Keyword("axiom"), unqualify(basename(n)));
            stream = ppFunParams(ps, stream);
        } 
        case TestClause(n, ps): { //xxx can this have modifiers?
            stream = ast2stream(stream, pp, Keyword("test"), unqualify(basename(n)));
            stream = ppFunParams(ps, stream);
        } 
		case TypeClause(n): {
  			stream = ast2stream(stream, pp, Keyword("type"), unqualify(basename(n)));
		}
		case VarClause(n, t): {
  			stream = ast2stream(stream, pp, Keyword("var"), unqualify(basename(n)), Colon, t);
		}
		default: {
			throw "Magnolia::lib::Pretty::pp unknown definition kind: <consOf(arg1)>";
		}
	}
    return stream;
}

// arg1:: clause
// arg2:: modifiers
// arg3:: body
Tseq pp(def:Define(arg1, arg2 : seq(attrs), arg3), Tseq stream) {
	stream = put(Begin("DEF"), stream);
    stream = ppDefClause(attrs, arg1, stream);
	stream = ast2stream(stream, pp, arg2, arg3);
	stream = put(End("DEF"), stream);
	return stream;
}

Tseq pp(Defines(arg0), Tseq stream) {
  return ast2stream(stream, pp, Keyword("defines"), arg0);
}

Tseq pp(def:DefineSugar(seq(mods), arg1, arg2 : seq(attrs), arg3), Tseq stream) {
	stream = put(Begin("DEF"), stream);
	stream = ast2stream(stream, pp, mods);
    // xxx Not sure if 'ppDefClause' works correctly without
    // attributes inferred by the desugarer. Yet we kind of want to
    // print out the sugared version, as that's what we were given. We
    // might apply just the DefineSugar desugaring (if that was
    // available as a separate function) before doing this if no sugar
    // is lost in the process.
    stream = ppDefClause(mods + attrs, arg1, stream);
	stream = ast2stream(stream, pp, arg2, arg3);
	stream = put(End("DEF"), stream);
	return stream;
}

Tseq pp(Del(), Tseq stream) {
  return ast2stream(stream, pp, Keyword("del"));
}

Tseq pp(Derived(), Tseq stream) {
  return ast2stream(stream, pp, Keyword("derived"));
}

Tseq pp(Doc(arg0), Tseq stream) {
  return ast2stream(stream, pp, Keyword("doc"), LBrace, arg0, RBrace);
}

Tseq pp(DotOp(arg0, arg1), Tseq stream) {
  return ast2stream(stream, pp, arg0, Text(".")[@cat="DOT"], arg1);
}

Tseq pp(EmptyBody(), Tseq stream) {
  return put(SemiColon, stream);
}

Tseq pp(Equal(Fun(arg0), arg1, arg2), Tseq stream) {
  op = nameToStr(defNameOf(arg0));
  op = replaceAll(op, "_", "");
  return ast2stream(stream, pp, arg1, Text(op)[@cat="BINOP"], arg2);
}

Tseq pp(Exp(), Tseq stream) {
  return ast2stream(stream, pp, Keyword("exp"));
}

Tseq pp(Export(), Tseq stream) {
  return ast2stream(stream, pp, Keyword("export"));
}

Tseq pp(Expr(arg0), Tseq stream) {
  return ast2stream(stream, pp, Text("\\expr"), LBracket, arg0, RBracket);
}

Tseq pp(Extend(), Tseq stream) {
  return ast2stream(stream, pp, Keyword("extend"));
}

Tseq pp(External(), Tseq stream) {
  return ast2stream(stream, pp, Keyword("external"));
}

Tseq pp(External(arg0), Tseq stream) {
  return ast2stream(stream, pp, Keyword("external"), arg0);
}

Tseq pp(ExternalDefines(arg0, arg1, arg2), Tseq stream) {
  return ast2stream(stream, pp, arg0, arg1, Keyword("defines"), arg2);
}

Tseq pp(ExternalExtendsOnDefines(arg0, arg1, arg2, arg3, arg4), Tseq stream) {
  return ast2stream(stream, pp, arg0, arg1, Keyword("extends"), arg2, Keyword("on"), arg3, Keyword("defines"), arg4);
}

Tseq pp(ExternalFun(arg0, arg1, onPart, arg2, arg3, arg4), Tseq stream) {
  stream = ast2stream(stream, pp, Text("\\external"), LParen, arg0, Comma, basename(arg1), Comma, basename(arg2));
  stream = ppFunParams(arg3, stream);
  stream = ast2stream(stream, pp, Colon, basename(arg4), RParen);
  return stream;
}

Tseq pp(ExternalOnDefines(arg0, arg1, arg2, arg3), Tseq stream) {
  return ast2stream(stream, pp, arg0, arg1, Keyword("on"), arg2, Keyword("defines"), arg3);
}

Tseq pp(ExternalProc(arg0, arg1, onPart, arg2, arg3), Tseq stream) {
  stream = ast2stream(stream, pp, Text("\\external"), LParen, arg0, Comma, basename(arg1), Comma, basename(arg2));
  stream = ppProcParams(arg3, stream);
  stream = put(RParen, stream);
  return stream;
}

Tseq pp(ExternalType(arg0, arg1, onPart, arg2), Tseq stream) {
  return ast2stream(stream, pp, Text("\\external"), LParen, arg0, Comma, basename(arg1), Comma, basename(arg2), RParen);
}

Tseq pp(Field(arg0, arg1), Tseq stream) {
  return ast2stream(stream, pp, arg0, BinOp("="), arg1);
}

Tseq pp(Filtered(arg0, arg1, arg2), Tseq stream) {
  return ast2stream(stream, pp, arg0, arg1, arg2);
}

Tseq pp(For(arg0, arg1, arg2), Tseq stream) {
  return ast2stream(stream, pp, Keyword("for"), arg0, Keyword("in"), arg1, Keyword("do"), arg2, Keyword("end"));
}

Tseq pp(FreeBy(arg0, arg1), Tseq stream) {
  return ast2stream(stream, pp, Keyword("free"), SepBy(arg0, Comma), Keyword("by"), arg1, SemiColon);
}

Tseq pp(Fun(arg0), Tseq stream) {
  if(isName(arg0))
    arg0 = basename(arg0);
  return ast2stream(stream, pp, arg0);
}


Tseq pp(FunName(arg0, arg1, arg2), Tseq stream) {
  return ast2stream(stream, pp, Keyword("function"), arg0, arg1, Colon, arg2);
}

Tseq pp(FunOf(arg0, arg1), Tseq stream) {
  return ast2stream(stream, pp, Text("\\f"), LParen, arg0, Comma, arg1, RParen);
}

Tseq pp(GenerateBy(arg0, arg1), Tseq stream) {
  return ast2stream(stream, pp, Keyword("generate"), SepBy(arg0, Comma), Keyword("by"), arg1, SemiColon);
}

Tseq pp(Giv(), Tseq stream) {
  return ast2stream(stream, pp, Keyword("giv"));
}

Tseq pp(Guard(arg0), Tseq stream) {
  return ast2stream(stream, pp, Keyword("guard"), arg0);
}

Tseq pp(Guard(arg0, arg1), Tseq stream) {
  return ast2stream(stream, pp, Keyword("guard"), arg0, Keyword("by"), arg1, Keyword("end"));
}

Tseq pp(HexIntegerLiteral(hex), Tseq stream) {
  return ast2stream(stream, pp, arg0);
}

Tseq pp(Homomorphism(arg0, arg1, arg2, arg3), Tseq stream) {
  return ast2stream(stream, pp, arg0, Keyword("homomorphisms"), arg1, Keyword("on"), arg2, Keyword("with"), arg3);
}

Tseq pp(HomomorphismOnWith(arg0, arg1, arg2), Tseq stream) {
  return ast2stream(stream, pp, Keyword("homomorphism"), arg0, Keyword("on"), arg1, Keyword("with"), arg2, SemiColon);
}

Tseq pp(If(arg0, arg1, arg2), Tseq stream) {
  return ast2stream(stream, pp, Keyword("if"), arg0, Keyword("then"), arg1, Keyword("else"), arg2, Keyword("end"));
}

Tseq pp(IfThenElseExpr(arg0, arg1, arg2), Tseq stream) {
  return ast2stream(stream, pp, Keyword("if"), arg0, Keyword("then"), arg1, Keyword("else"), arg2, Keyword("end"));
}

Tseq pp(ImplDef(arg0, arg1, arg2, arg3), Tseq stream) {
  return ast2stream(stream, pp, arg0, Begin("DEF"), Keyword("implementation"), arg1, arg2, BinOp("="), arg3, End("DEF"));
}

Tseq pp(RenDef(arg0, arg1, arg2, arg3), Tseq stream) {
  return ast2stream(stream, pp, arg0, Begin("DEF"), Keyword("renaming"), arg1, arg2, BinOp("="), arg3, End("DEF"));
}

Tseq pp(ImportAll(arg0), Tseq stream) {
  return ast2stream(stream, pp, arg0);
}

Tseq pp(Imports(arg0), Tseq stream) {
  return ast2stream(stream, pp, Keyword("imports"), SepBy(arg0, Comma));
}

Tseq pp(IndexSugar(arg0, arg1), Tseq stream) {
  return ast2stream(stream, pp, arg0, LBracket, SepBy(arg1, Comma), RBracket);
}

Tseq pp(InSugar(arg0, leaf(arg1), arg2), Tseq stream) {
  return ast2stream(stream, pp, arg0, BinOp(arg1), arg2);
}

Tseq pp(Lang(arg0), Tseq stream) {
  return ast2stream(stream, pp, arg0);
}

Tseq pp(Let(arg0, arg1), Tseq stream) {
  return ast2stream(stream, pp, Keyword("let"), arg0, Keyword("in"), arg1, Keyword("end"));
}

Tseq pp(LetSugar(arg0), Tseq stream) {
  return ast2stream(stream, pp, arg0, SemiColon);
}

Tseq pp(ProgramDef(arg0, arg1, arg2, arg3), Tseq stream) {
  return ast2stream(stream, pp, arg0, Begin("DEF"), Keyword("program"), arg1, arg2, BinOp("="), arg3, End("DEF"));
}

Tseq pp(ListCons(arg0), Tseq stream) {
  return ast2stream(stream, pp, LBracket, SepBy(arg0, Comma), RBracket);
}

Tseq pp(Literal(arg0), Tseq stream) {
  return ast2stream(stream, pp, arg0);
}

Tseq pp(MagnoliaTree(arg0, arg1), Tseq stream) {
  return ast2stream(stream, pp, arg0, arg1);
}

Tseq pp(Models(arg0, arg1), Tseq stream) {
  return ast2stream(stream, pp, arg0, Keyword("models"), arg1);
}

Tseq pp(ModuleHead(arg0, arg1), Tseq stream) {
  return ast2stream(stream, pp, Keyword("module"), arg0, arg1, SemiColon);
}

Tseq pp(Name(arg0), Tseq stream) {
  return ast2stream(stream, pp, arg0);
}

Tseq pp(Nop(), Tseq stream) {
  return ast2stream(stream, pp, SemiColon);
}

Tseq pp(Nrm(), Tseq stream) {
  return ast2stream(stream, pp, Keyword("nrm"));
}

Tseq pp(Obs(), Tseq stream) {
  return ast2stream(stream, pp, Keyword("obs"));
}

Tseq pp(OctalIntegerLiteral(octal), Tseq stream) {
  return ast2stream(stream, pp, arg0);
}

Tseq pp(OnDefines(arg0, arg1), Tseq stream) {
  return ast2stream(stream, pp, Keyword("on"), arg0, Keyword("defines"), arg1);
}

Tseq pp(OnFilter(arg0, arg1, arg2), Tseq stream) {
  return ast2stream(stream, pp, arg0, Keyword("on"), arg1, arg2);
}

Tseq pp(Out(), Tseq stream) {
  return ast2stream(stream, pp, Keyword("out"));
}

Tseq ppProcParam(Param(arg0, arg1, arg2), Tseq stream) {
  return ast2stream(stream, pp, arg0, arg1, Colon, basename(arg2));
}

Tseq ppProcParam(ParamSugar(n, tn), Tseq stream) {
  return ast2stream(stream, pp, n, Colon, basename(tn));
}

// a Param has name and type
Tseq ppFunParam(Param(arg1, arg2), Tseq stream) {
  return ast2stream(stream, pp, arg1, Colon, basename(arg2));
}

// a Param has name and type, maybe also modifier (discarded)
Tseq ppFunParam(Param(arg0, arg1, arg2), Tseq stream) {
  return ast2stream(stream, pp, arg1, Colon, basename(arg2));
}

Tseq ppFunParam(AST ast, Tseq stream) {
  throw "ppFunParam *****<yieldTerm(ast, false)>*****";
  }

Tseq ppFunParams(ParamList(arg0), Tseq stream) {
  return ast2stream(stream, ppFunParam, LParen, SepBy(arg0, Comma), RParen);
}

Tseq ppProcParams(ParamList(arg0), Tseq stream) {
  return ast2stream(stream, ppProcParam, LParen, SepBy(arg0, Comma), RParen);
}

Tseq pp(PackageHead(arg0, arg1), Tseq stream) {
  return ast2stream(stream, pp, Keyword("package"), arg0, arg1, SemiColon);
}

Tseq pp(PartitionBy(arg0, arg1), Tseq stream) {
  return ast2stream(stream, pp, Keyword("partition"), SepBy(arg0, Comma), Keyword("by"), arg1, SemiColon);
}

Tseq pp(Predicate(), Tseq stream) {
  return ast2stream(stream, pp, Text("\\predicate"));
}

Tseq pp(t : PreOpSugar(leaf(arg0), arg1), Tseq stream) {
  //println(yieldTerm(t, false));
  return ast2stream(stream, pp, PreOp(arg0), arg1);
}

Tseq pp(Preserve(arg0), Tseq stream) {
  return ast2stream(stream, pp, Keyword("preserve"), arg0, SemiColon);
}

Tseq pp(PreserveOn(arg0, arg1), Tseq stream) {
  return ast2stream(stream, pp, Keyword("preserve"), arg0, Keyword("on"), arg1, SemiColon);
}

Tseq pp(Proc(arg0), Tseq stream) {
  if(isName(arg0))
    arg0 = basename(arg0);
  return ast2stream(stream, pp, arg0);
}

Tseq pp(ProcName(arg0, arg1), Tseq stream) {
  return ast2stream(stream, pp, Keyword("procedure"), arg0, arg1);
}

Tseq pp(ProcOf(arg0), Tseq stream) {
  return ast2stream(stream, pp, Text("\\p"), LParen, arg0, RParen);
}

Tseq pp(Protect(), Tseq stream) {
  return ast2stream(stream, pp, Keyword("protect"));
}

Tseq pp(QED(), Tseq stream) {
  return ast2stream(stream, pp, Keyword("qed"));
}

Tseq pp(QName(arg0), Tseq stream) {
  return ast2stream(stream, pp, SepBy(arg0, Text(".")[@cat="DOT"]));
}

Tseq pp(Rename(arg0, arg1), Tseq stream) {
  return ast2stream(stream, pp, arg0, Text("=\>"), arg1);
}

Tseq pp(RenameList(arg0), Tseq stream) {
  return ast2stream(stream, pp, LBracket, SepBy(arg0, Comma), RBracket);
}

Tseq pp(Renamed(arg0, arg1), Tseq stream) {
  return ast2stream(stream, pp, arg0, LBracket, SepBy(arg1, Comma), RBracket);
}

Tseq pp(Renaming(), Tseq stream) {
  return ast2stream(stream, pp, Text("\\renaming"));
}

Tseq pp(Require(), Tseq stream) {
  return ast2stream(stream, pp, Keyword("require"));
}

Tseq pp(Requires(arg0), Tseq stream) {
  return ast2stream(stream, pp, Keyword("requires"), SepBy(arg0, Comma), SemiColon);
}

Tseq pp(Return(), Tseq stream) {
  return ast2stream(stream, pp, Keyword("return"), SemiColon);
}

Tseq pp(Return(arg0), Tseq stream) {
  return ast2stream(stream, pp, Keyword("return"), arg0, SemiColon);
}

Tseq pp(SatDef(arg0, arg1, arg2, arg3), Tseq stream) {
  return ast2stream(stream, pp, arg0, Begin("DEF"), Keyword("satisfaction"), arg1, arg2, BinOp("="), arg3, End("DEF"));
}

Tseq pp(SpecialType(arg0), Tseq stream) {
  return ast2stream(stream, pp, arg0);
}

Tseq pp(Src(arg0), Tseq stream) {
  return ast2stream(stream, pp, Keyword("src"), LBrace, arg0, RBrace);
}

Tseq pp(StringLiteral(string), Tseq stream) {
  return ast2stream(stream, pp, arg0);
}

Tseq pp(Struct(arg0), Tseq stream) {
  return ast2stream(stream, pp, Keyword("struct"), LBrace, arg0, RBrace);
}

Tseq pp(Struct(arg0, arg1), Tseq stream) {
  return ast2stream(stream, pp, arg0, LBrace, SepBy(arg1, Comma), RBrace);
}

Tseq pp(Test(), Tseq stream) {
  return ast2stream(stream, pp, Text("\\test"));
}

Tseq pp(Times(arg0, arg1), Tseq stream) {
  return ast2stream(stream, pp, arg0, Keyword("times"), arg1);
}

Tseq pp(TopBinOp(arg0, leaf(arg1), arg2), Tseq stream) {
  return ast2stream(stream, pp, arg0, BinOp(arg1), arg2);
}

Tseq pp(TopExprTree(arg0), Tseq stream) {
  return ast2stream(stream, pp, arg0);
}

Tseq pp(TopName(arg0, leaf(arg1)), Tseq stream) {
  return ast2stream(stream, pp, Keyword(arg1), arg0);
}

/*Tseq pp(TopParen(arg0), Tseq stream) {
  return ast2stream(stream, pp, LParen, arg0, RParen);
}
*/
Tseq pp(TopPreOp(arg0, arg1), Tseq stream) {
  return ast2stream(stream, pp, arg0, arg1);
}

Tseq pp(Tuple(arg0), Tseq stream) {
  return ast2stream(stream, pp, LParen, SepBy(arg0, Comma), RParen);
}

Tseq pp(TypeExpr(arg0), Tseq stream) {
  return ast2stream(stream, pp, Colon, arg0);
}

Tseq pp(TypeField(arg0), Tseq stream) {
  return ast2stream(stream, pp, arg0, SemiColon);
}

Tseq pp(TypeName(arg0), Tseq stream) {
  return ast2stream(stream, pp, Keyword("type"), arg0);
}

Tseq pp(Undefined(), Tseq stream) {
  return ast2stream(stream, pp, Text("_"));
}

Tseq pp(Unknown(), Tseq stream) {
  return ast2stream(stream, pp, Text("?"));
}

Tseq pp(Upd(), Tseq stream) {
  return ast2stream(stream, pp, Keyword("upd"));
}

Tseq pp(Var(arg0), Tseq stream) {
  if(isName(arg0))
    arg0 = basename(arg0);
  return ast2stream(stream, pp, arg0);
}

Tseq pp(VarDecl(arg0, arg1, arg2, arg3), Tseq stream) {
  return ast2stream(stream, pp, arg0, arg1, Colon, arg2, BinOp("="), arg3);
}

Tseq pp(VarDeclSugarNoType(arg0, arg1), Tseq stream) {
  return ast2stream(stream, pp, Keyword("var"), arg0, BinOp("="), arg1);
}

Tseq pp(VarDeclSugarNoExpr(arg0, arg1), Tseq stream) {
  return ast2stream(stream, pp, Keyword("var"), arg0, Colon, arg1);
}

Tseq pp(VarName(arg0, arg1), Tseq stream) {
  return ast2stream(stream, pp, Keyword("var"), arg0, Colon, arg1);
}

Tseq pp(While(arg0, arg1), Tseq stream) {
  return ast2stream(stream, pp, Keyword("while"), arg0, Keyword("do"), arg1, Keyword("end"));
}

Tseq pp(WithModels(arg0, arg1, arg2), Tseq stream) {
  return ast2stream(stream, pp, arg0, Keyword("with"), arg1, Keyword("models"), arg2);
}

Tseq pp(Yield(arg0), Tseq stream) {
  return ast2stream(stream, pp, Keyword("yield"), arg0, SemiColon);
}

Tseq pp(whitespace(arg0), Tseq stream) {
  return ast2stream(stream, pp, arg0);
}
