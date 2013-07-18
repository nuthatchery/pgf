module org::nuthatchery::pgf::rascal::tests::TestWithMgModules

import org::nuthatchery::pgf::rascal::engines::Spacer;

import org::nuthatchery::pgf::rascal::engines::FmtEngine;
import org::nuthatchery::pgf::rascal::TableBuilder;
import org::nuthatchery::pgf::rascal::Token;
import org::nuthatchery::pgf::rascal::Tseq;

import org::magnolialang::magnolia::lib::Names;
import org::magnolialang::magnolia::MagnoliaFacts;
import org::nuthatchery::pica::terms::Terms;

import org::magnolialang::magnolia::resources::Resources;

import IO;
import List;
import Node;
import ParseTree;
import String;

void printFormatted(Tseq ts) {
  prettyPrintFlush(newFmtSt(80, ts));
}

set[TokenCat] mgCategories = {
  "TOKEN", "SPC", "LPAREN", "RPAREN", "LBRACE", "RBRACE", 
  "LBRACKET", "RBRACKET", "BINOP", "PREOP", "COMMA", 
  "KEYWORD", "SEMICOLON", "COLON", "DOT", "WORD"
};

Token mgCat(t:Text(s), str defCat) {
	switch(s) {
		case "(":	return t[@cat="LPAREN"];
		case ")":	return t[@cat="RPAREN"];
		case "[":	return t[@cat="LBRACKET"];
		case "]":	return t[@cat="RBRACKET"];
		case "{":	return t[@cat="LBRACE"];
		case "}":	return t[@cat="RBRACE"];
		case ",":	return t[@cat="COMMA"];
		case ";":	return t[@cat="SEMICOLON"];
		case ":":	return t[@cat="COLON"];
		case ".":	return t[@cat="DOT"];
	}
	if(s in ["+", "-", "*", "/", "%", "=", 
             "\<=", "\>=", "\<", "\>", "==", "!=",
             "&&", "||", "^^", "&", "|", "^", "\<\<", "\>\>", "in",
             "=\>", "\<=\>", "\<-\>"])
		return t[@cat="BINOP"];
	if(s in ["!", "~"])
		return t[@cat="PREOP"];
    if (/^\/\// := s) {
      return Text(trim(s))[@cat="LINECOMMENT"];
    }
	return t[@cat=defCat];
}

str treeToStr(Tree t) {
  return yieldTerm(t, false);
}

Tseq treeToTseq(Tseq outStream, Tree t) {
	void addLex(Tree a) {
      str s = unparse(a);
      if (/\/\*/ := s) {
        for (s <- split("\n", s)) {
          s = trim(s);
          if(s != "")
            outStream = put(Text(s)[@cat="BLOCKCOMMENT"], outStream);
        }
      } else if (/^\s*$/ !:= s) {
        outStream = put(mgCat(Text(s), "WORD"), outStream); 
      }
    }
	void addLit(Tree l) { 
      outStream = put(mgCat(Text(unparse(l)), "KEYWORD"), outStream); 
    }
	void addLayouts(Tree l) {
      str s = unparse(l);
      if(s != "")
        outStream = put(Space(s), outStream);
	}

    /* We appear to sometimes be getting duplicates of tokens (such as ";" or "_+_"). Are there dupes in parse tree as well? Due to ambiguity? xxx

TOKENLEX1(_+_ : appl(prod(lex("OperatorIdentifier"), [lit("_"), sort("TOPBINOP"), lit("_")], {}), [appl(prod(lit("_"), [char-class([range(95, 95)])], {}), [char(95)]), appl(prod(sort("TOPBINOP"), [keywords("TOPADDOP")], {}), [appl(prod(keywords("TOPADDOP"), [lit("+")], {}), [appl(prod(lit("+"), [char-class([range(43, 43)])], {}), [char(43)])])]), appl(prod(lit("_"), [char-class([range(95, 95)])], {}), [char(95)])]))
TOKENLEX1(_+_ : appl(prod(lex("OperatorIdentifier"), [lit("_"), lex("BINOP"), lit("_")], {}), [appl(prod(lit("_"), [char-class([range(95, 95)])], {}), [char(95)]), appl(prod(lex("BINOP"), [lex("ADDOP")], {}), [appl(prod(lex("ADDOP"), [char-class([range(43, 43), range(45, 45)])], {}), [char(43)])]), appl(prod(lit("_"), [char-class([range(95, 95)])], {}), [char(95)])]))

Indeed it is an ambiguity issue.

amb({appl(prod(lex("OperatorIdentifier"), [lit("_"), sort("TOPBINOP"), lit("_")], {}), [appl(prod(lit("_"), [char-class([range(95, 95)])], {}), [char(95)]), appl(prod(sort("TOPBINOP"), [keywords("TOPADDOP")], {}), [appl(prod(keywords("TOPADDOP"), [lit("+")], {}), [appl(prod(lit("+"), [char-class([range(43, 43)])], {}), [char(43)])])]), appl(prod(lit("_"), [char-class([range(95, 95)])], {}), [char(95)])]), appl(prod(lex("OperatorIdentifier"), [lit("_"), lex("BINOP"), lit("_")], {}), [appl(prod(lit("_"), [char-class([range(95, 95)])], {}), [char(95)]), appl(prod(lex("BINOP"), [lex("ADDOP")], {}), [appl(prod(lex("ADDOP"), [char-class([range(43, 43), range(45, 45)])], {}), [char(43)])]), appl(prod(lit("_"), [char-class([range(95, 95)])], {}), [char(95)])])})

     */
    if (false) {
	top-down-break visit(t) {
		case a:appl(prod(lex(_),_,_), _):
          println("TOKENLEX1(<a> : <treeToStr(a)>)");
		case a:appl(prod(label(_, lex(_)),_,_), _):
          println("TOKENLEX2(<a> : <treeToStr(a)>)");
		case a:appl(prod(layouts(_),_,_), _):
          println("TOKENLAY3(<a> : <treeToStr(a)>)");
		case a:appl(prod(label(_, layouts(_)),_,_), _):
          println("TOKENLAY4(<a> : <treeToStr(a)>)");
		case a:appl(prod(lit(_),_,_), _):
          println("TOKENLIT5(<a> : <treeToStr(a)>)");
		case a:appl(prod(label(_,lit(_)),_,_), _):
          println("TOKENLIT6(<a> : <treeToStr(a)>)");
		case a:appl(prod(cilit(_),_,_), _):
          println("TOKENLIT7(<a> : <treeToStr(a)>)");
		case a:appl(prod(label(_,cilit(_)),_,_), _):
          println("TOKENLIT8(<a> : <treeToStr(a)>)");
	}
    }
		
	top-down-break visit(t) {
		case a:appl(prod(lex(_),_,_), _):
			addLex(a);
		case a:appl(prod(label(_, lex(_)),_,_), _):
			addLex(a);
            /*
		case l:appl(prod(layouts(_),_,_), _):
			addLayouts(l); 
		case l:appl(prod(label(_, layouts(_)),_,_), _):
			addLayouts(l);
            */
		case l:appl(prod(lit(_),_,_), _):
			addLit(l);
		case l:appl(prod(label(_,lit(_)),_,_), _):
			addLit(l);
		case l:appl(prod(cilit(_),_,_), _):
			addLit(l);
		case l:appl(prod(label(_,cilit(_)),_,_), _):
			addLit(l);
	}

	return outStream;
}

// In the second arg we need only list categories not appearing
// in the rules, or otherwise we get Nothing() as the Decision
// for anything involving that category.
DecisionTable mgSpaceTable = makeTable((
  <"SPC", "*"> : Skip(),
  <"*", "COMMA"> : Insert(Space(" ")),
  <"WORD", "WORD"> : Insert(Space(" ")),
  <"WORD", "KEYWORD"> : Insert(Space(" ")),
  <"KEYWORD", "WORD"> : Insert(Space(" ")),
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
  <"*", "LBRACE"> : Sequence([Insert(Nest(LvInc(2))), Insert(Line())]),
  <"RBRACE", "*"> : Sequence([Insert(Nest(LvPop())), Insert(Line())]),
  <"*", "RBRACE"> : Insert(Line()),
  <"*", "SEMICOLON"> : Insert(Line()),
  <"*", "LINECOMMENT"> : Insert(Line()),
  <"*", "BLOCKCOMMENT"> : Insert(Line()),
  <"*", "*"> : Nothing()), {"LBRACKET"});

/*
  E.g.

  (init-compiler)
  (rimport "org::nuthatchery::pgf::rascal::tests::TestWithMgModules")
  (rcall "testMgSpacer" (rstring "basic.Basic"))

  (rreload "org::nuthatchery::pgf::rascal::tests::TestWithMgModules")
  (rcall "testMgSpacer" (rstring "basic.Basic"))

  Note to reload a dependency you may also have to reload this
  module to get the reloaded dependency re-imported here.
*/
public void testMgSpacer(str modName) {
  /*
    parsing
  */
  name = strToName(modName);
  tr = startTransaction();
  //println(getFact(tr, #DefInfo, name));
  // for printing the ParseTree 'val', 'unparse' is used?
  println(getFact(tr, #ParseTree, name).val);
  //println(getFact(tr, #ParseTree, name)); // huge
  //println(getFact(tr, #ImplodedTree, name)); // smaller
  pt = getFact(tr, #ParseTree, name).val;
  //println(yieldTerm(implode(pt), false));
  //println(pt);
  ts = treeToTseq(tseqNull, pt);
  println(ts);
  printlnTokenStream(ts);
  endTransaction(tr);

  /*
    spacing
  */
  println("SPACED:");
  ctx = SpacerContext(mgSpaceTable);
  out = tseqNull;
  info = <ts, out, ctx>;
  info = processTokens(info);
  out = info[1];
  printlnTokenStream(out);

  /*
    pretty printing
  */
  printFormatted(out);
}
