// run with rascal-src
module org::nuthatchery::pgf::rascal::tests::RunTestSpacer

import org::nuthatchery::pgf::rascal::tests::ExprTestData; // generated

import org::nuthatchery::pgf::rascal::engines::FmtEngine;
import org::nuthatchery::pgf::rascal::engines::Spacer;
import org::nuthatchery::pgf::rascal::TableBuilder;
import org::nuthatchery::pgf::rascal::Token;
import org::nuthatchery::pgf::rascal::Tseq;

import IO;

// not visible from org::nuthatchery::pgf::rascal::engines::Spacer !!
// not even if made 'public'
alias DecisionTable = map[tuple[TokenCat current, TokenCat lastSeen], Decision];

set[TokenCat] myCategories = {"TOKEN", "SPC", "BINOP"};

public DecisionTable binOpTable = makeTable((
    <"BINOP","*"> : Sequence([Insert(Text("x")), Insert(Text("y")), Insert(Text("z"))]),
    <"*","*"> : Nothing()
	), myCategories);

void printFormatted(Tseq ts) {
  FmtSt st = newFmtSt(80, ts);
  prettyPrintFlush(st);
}

void printStream(str name, Tseq ts) {
  println("=== <name> ===");
  print("RAW: "); println(ts);
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

public void runRunTestSpacer() {
  for (d <- exprTestData)
    runTest(d);
}

public void main(list[str] args) {
  runRunTestSpacer();
}
