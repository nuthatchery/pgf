// run with rascal-src-with-magnolia

module org::nuthatchery::pgf::rascal::tests::DumpMgData

import org::nuthatchery::pgf::rascal::PrettyParseTree;
import org::nuthatchery::pgf::rascal::Token;
import org::nuthatchery::pgf::rascal::Tseq;

import org::magnolialang::magnolia::MagnoliaFacts;
import org::magnolialang::magnolia::lib::Names;
import org::magnolialang::magnolia::resources::Resources;

import IO;
import List;

/** Dumps test data (token streams of Magnolia) as Rascal data
    structures. Can be used for testing (in batch files and such)
    without having to generate parser Java classes. */
public void main(list[str] args) {
  name = strToName("basic.Basic");
  tr = startTransaction();
  //println(getFact(tr, #DefInfo, name));
  pt = getFact(tr, #ParseTree, name).val;
  //println(pt);
  ts = prettyParseTree(tseqNull, pt);
  //println(ts);
  println("// generated file (by DumpMgData.rsc)");
  println("module org::nuthatchery::pgf::rascal::tests::MgTestData");
  println("import org::nuthatchery::pgf::rascal::Token;");
  println("public Tseq basicBasicMgData = ");
  println(ts);
  println(";");
  endTransaction(tr);
}
