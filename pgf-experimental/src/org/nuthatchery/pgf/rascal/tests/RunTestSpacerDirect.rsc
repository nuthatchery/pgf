// run with rascal-src
module org::nuthatchery::pgf::rascal::tests::RunTestSpacerDirect

import org::nuthatchery::pgf::rascal::PrettyParseTree;
import org::nuthatchery::pgf::rascal::TableBuilder;
import org::nuthatchery::pgf::rascal::Token;
import org::nuthatchery::pgf::rascal::Tseq;
import org::nuthatchery::pgf::rascal::engines::FmtEngine;
import org::nuthatchery::pgf::rascal::engines::Spacer;
import org::nuthatchery::pgf::rascal::tests::SimpleExprLang;

import IO;
import List;

// not visible from org::nuthatchery::pgf::rascal::engines::Spacer !!
// not even if made 'public'
alias DecisionTable = map[tuple[TokenCat current, TokenCat lastSeen], Decision];

set[TokenCat] myCategories = {"TOKEN", "SPC", "BINOP"};

public DecisionTable binOpTable = makeTable((
    <"BINOP","*"> : Sequence([Insert(Text("x")), Insert(Text("y")), Insert(Text("z"))]),
    <"*","*"> : Nothing()
	), myCategories);

void printFormatted(Tseq ts) {
  prettyPrintFlush(newFmtSt(80, ts));
}

void printStream(str name, Tseq ts) {
  println("=== <name> ===");
  print("RAW: "); println(tseqToList(ts));
  print("TOKENS: "); printlnTokenStream(ts);
  print("FORMATTED: "); printFormatted(ts); println("");
}

void runTest(Tseq ts) {
  printStream("original", ts);
  
  list[DecisionTable] tables = [
                                (),
                                alwaysSpaceTable,
                                spaceTable,
                                reSpaceTable,
                                binOpTable
                                ];
  i = 0;
  for (t <- tables) {
    i += 1;
    ctx = SpacerContext(t);
    out = tseqNull;
    info = <ts, out, ctx>;
    info = processTokens(info);
    printStream("spaced <i>", info[1]);
  }
}

list[Expr] progs = 
  [
   (Expr)`abc + cde`,
   (Expr)`abc + cde + def`,
   (Expr)`!a-b`,
   (Expr)`f(abc, cde)`,
   (Expr)`a * b + c/ d`
  ];

public void runRunTestSpacerDirect() {
  Tseq f(Expr e) {
    return prettyParseTree(tseqNull, e);
  }
  list[Tseq] exprTestData = mapper(progs, f);
  for (d <- exprTestData)
    runTest(d);
}

public void main(list[str] args) {
  runRunTestSpacerDirect();
}
