// run with rascal-src
module org::nuthatchery::pgf::rascal::tests::DumpExprData

import org::nuthatchery::pgf::rascal::engines::Spacer;
import org::nuthatchery::pgf::rascal::tests::SimpleExprLang;
import org::nuthatchery::pgf::rascal::PrettyParseTree;
import org::nuthatchery::pgf::rascal::Token;
import org::nuthatchery::pgf::rascal::Tseq;

import IO;
import List;

list[Expr] progs = 
  [
   (Expr)`abc + cde`,
   (Expr)`abc + cde + def`,
   (Expr)`!a-b`,
   (Expr)`f(abc, cde)`,
   (Expr)`a * b + c / d`
  ];

/** Dumps test data (token streams of SimpleExprLang) as Rascal data
    structures. Can be used for testing (in batch files and such)
    without having to generate SimpleExprLang parser Java classes. */
public void main(list[str] args) {
  Tseq f(Expr e) {
    return prettyParseTree(tseqNull, e);
  }
  println("// generated file (by DumpExprData.rsc)");
  println("module org::nuthatchery::pgf::rascal::tests::ExprTestData");
  println("import org::nuthatchery::pgf::rascal::Token;");
  println("import org::nuthatchery::pgf::rascal::Tseq;");
  println("public list[Tseq] exprTestData = ");
  println(mapper(progs, f));
  println(";");
}
