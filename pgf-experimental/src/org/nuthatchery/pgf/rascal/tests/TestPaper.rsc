module org::nuthatchery::pgf::rascal::tests::TestPaper

import org::nuthatchery::pgf::rascal::engines::Spacer;
import org::nuthatchery::pgf::rascal::engines::LineBreaker;
import org::nuthatchery::pgf::rascal::tests::SimpleExprLang;
import org::nuthatchery::pgf::rascal::PrettyParseTree;
import org::nuthatchery::pgf::rascal::Token;
import org::nuthatchery::pgf::rascal::Tseq;
import org::nuthatchery::pgf::rascal::TableBuilder;
import String;
import IO;


public Expr prog1 = (Expr)`abc + cde`;
public Expr prog2 = (Expr)`abc + cde + def`;
public Expr prog3 = (Expr)`!a-b`;
public Expr prog4 = (Expr)`f(abc, cde)`;
public Expr prog5 = (Expr)`a * b + c / d`;

public Expr prog6 = (Stmt)`if(b) {x=3;}else{x=4;}`;
public Expr prog7 = (Stmt)`x = a + b + c + d + e + f + g + h + i + j;`;
public Expr prog8 = (Stmt)`if(b) x = f ( 1,2,3);`;
public Expr prog9 = (Expr)`(1 + 2) * (3 + 4 + (5 + 6 + 7) + 8)`;
public Expr prog10 = (Stmt)`x = a * b + c / d + (c / d * f) + c / d;`;

public Tseq prog1toks = prettyParseTree(tseqNull, prog1);
public Tseq prog2toks = prettyParseTree(tseqNull, prog2);
public Tseq prog3toks = prettyParseTree(tseqNull, prog3);
public Tseq prog4toks = prettyParseTree(tseqNull, prog4);
public Tseq prog5toks = prettyParseTree(tseqNull, prog5);
public Tseq prog6toks = prettyParseTree(tseqNull, prog6);
public Tseq prog7toks = prettyParseTree(tseqNull, prog7);
public Tseq prog8toks = prettyParseTree(tseqNull, prog8);
public Tseq prog9toks = prettyParseTree(tseqNull, prog9);
public Tseq prog10toks = prettyParseTree(tseqNull, prog10);

public void printStream(str name, Tseq tsIn, Tseq ts) {
  println("=== <name> ===");
  print("INPUT RAW: "); println(tseqToList(tsIn));
  print("INPUT TOKENS: "); printlnTokenStream(tsIn);
  print("INPUT FORMATTED: "); println(docToString(tsIn)); println("");
  print("OUTPUT RAW: "); println(tseqToList(ts));
  print("OUTPUT TOKENS: "); printlnTokenStream(ts);
  print("OUTPUT FORMATTED: "); println(docToString(ts)); println("");
}


// Could have a configuration option in FmtSt if wanted this to be
// customizable to sometimes print CRLF, for instance.
str tokenToString(Line()) {
  return "\n";
}


str tokenToString(Text(s)) {
  return s;
}

str tokenToString(Space(s)) {
  return s;
}

default str tokenToString(Token t) {
  return "";
}

str docToString(Tseq ts) {
  r = "";
  while (true) {
    <t, ts> = tseqGet(ts);
    if (t == Eof())
      return r;
    r += tokenToString(t);
  }
}

public DecisionTable myReSpaceTable = makeTable((
		<"SPC", "*"> : Skip(),
		<"*", ""> : Nothing(),
		<"*", "LPAREN"> : Nothing(), 
		<"*", "LBRACKET"> : Nothing(),
//		<"LPAREN", "*"> : Nothing(),
		<"RPAREN", "*"> : Nothing(),
		<"RBRACKET", "*"> : Nothing(),
		<"COMMA", "*"> : Nothing(),
		<"*", "COMMA"> : Insert(Space(" ")),
		<"*", "PREOP"> : Nothing(),
		<"SEMICOLON", "*"> : Nothing(),
		<"*", "DOT"> : Nothing(),
		<"DOT", "*"> : Nothing(),
		<"*", "*"> : Insert(Space(" ")),
		<"*", "LBRACE"> : Insert(Line()),
		<"RBRACE", "*"> : Insert(Line()),
		<"*", "RBRACE"> : Insert(Line()),
		<"LPAREN", "KEYWORD"> : Insert(Space(" ")) 
/*		<"SPC", "*"> : Skip(),
		<"*", ""> : Nothing(),
		<"*", "LPAREN"> : Nothing(), 
		<"LPAREN", "*"> : Nothing(),
		<"RPAREN", "*"> : Nothing(),
		<"COMMA", "*"> : Nothing(),
		<"*", "COMMA"> : Insert(Space(" ")),
		<"*", "PREOP"> : Nothing(),
		<"SEMICOLON", "*"> : Nothing(),
		<"*", "*"> : Insert(Space(" "))*/
	), stdCategories);
	
public void testPaper1() {
	for(toks <- [prog6toks, prog8toks])
		printStream("testPaper1: ", toks, processTokens(<toks, tseqNull, SpacerContext(myReSpaceTable)>)[1]);
}

public void testPaper2() {
	for(inToks <- [prog9toks, prog10toks]) {
		inToks = processTokens(<inToks, tseqNull, SpacerContext(myReSpaceTable)>)[1];
		print("INPUT       : ");
		println(docToString(inToks));
		println("");
		println(inToks);
		for(w <- [20,30,40]) {
			toks =  breakLines(w, inToks, tseqNull, false);
			print("// width <w>\n");
			for(i <- [1..w+1]) {
				print(i % 10);
			}
			print("\n");
			println(docToString(toks));
			println("");
		}
	}
}
