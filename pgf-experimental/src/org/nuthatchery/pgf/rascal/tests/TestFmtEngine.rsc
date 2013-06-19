// run with rascal-src

/*
  (rimport "org::nuthatchery::pgf::rascal::tests::TestFmtEngine")
  (rcall "runTestFmtEngine")
 */
module org::nuthatchery::pgf::rascal::tests::TestFmtEngine

import org::nuthatchery::pgf::rascal::engines::FmtEngine;
import org::nuthatchery::pgf::rascal::FmtUtil;
import org::nuthatchery::pgf::rascal::FmtUtilC;
import org::nuthatchery::pgf::rascal::Groupings;
import org::nuthatchery::pgf::rascal::Token;
import org::nuthatchery::pgf::rascal::Tseq;

import IO;
import List;

str lorem_ipsum_sentence = "Lorem ipsum dolor sit amet, consectetur adipiscing elit.";

str lorem_ipsum_paragraph = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Donec a diam lectus. Sed sit amet ipsum mauris. Maecenas congue ligula ac quam viverra nec consectetur ante hendrerit. Donec et mollis dolor. Praesent et diam eget libero egestas mattis sit amet vitae augue. Nam tincidunt congue enim, ut porta lorem lacinia consectetur. Donec ut libero sed arcu vehicula ultricies a non tortor. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Aenean ut gravida lorem. Ut turpis felis, pulvinar a semper sed, adipiscing id dolor. Pellentesque auctor nisi id magna consequat sagittis. Curabitur dapibus enim sit amet elit pharetra tincidunt feugiat nisl imperdiet. Ut convallis libero in urna ultrices accumsan. Donec sed odio eros. Donec viverra mi quis quam pulvinar at malesuada arcu rhoncus. Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. In rutrum accumsan ultricies. Mauris vitae nisi at sem facilisis semper ac in est.";

str hyphens(int n) {
  return ("" | "<it>-" | x <- [0..n], x > 0);
}

str divider(int n) {
  return "// " + ("" | "<it><x%10>" | x <- [3..n], x > 0);
}

void testDoc(int w, str name, Tseq d) {
  println("// ==begin==");
  println("// <name> (w=<w>)");
  println(divider(w));
  println(pretty(w, d));
  println("// ---end---");
}

void testIncremental() {
  x = tseq(Text("increase"), 
           Nest(LvInc(4)), Line(), 
           Text("level"));
  y = tseq(Text("decrease"), Nest(LvPop()),
           Line(), Text("restored"));
  st = newFmtSt(40);
  st = prettyPrint(st);
  st.inDoc = tseqAppend(st.inDoc, x);
  st = prettyPrint(st);
  st.inDoc = tseqAppend(st.inDoc, x);
  st = prettyPrint(st);
  st.inDoc = tseqAppend(st.inDoc, x);
  st.inDoc = tseqAppend(st.inDoc, y);
  st = prettyPrint(st);
  st.inDoc = tseqAppend(st.inDoc, y);
  st.inDoc = tseqAppend(st.inDoc, y);
  st.inDoc = tseqAppend(st.inDoc, tseq(Line()));
  prettyPrintFlush(st);
}

/*
  (rimport "org::nuthatchery::pgf::rascal::engines::FmtEngine")
  (rimport "org::nuthatchery::pgf::rascal::Token")
  (rimport "org::nuthatchery::pgf::rascal::Tseq")
  (def d1 (rcall "flatten" (rcall "tseq" (rstring "first") (reval "Line()") (rstring "second"))))
  (rcall "pretty" (rinteger 80) d1)
*/
Tseq d1 = tseq(Text("first"), Line(),
               Text("second"), Line(),
               Text("third"), Line(),
               Text("fourth"));

Tseq d3 = tseq(union(Text("verylongverylong"),
                     Text("short")));

Tseq d4 = tseq("forever {", indent(2), br, "(void)0;", dedent, br, "}");

Token cppBr = union(Text(" "), tseq(Text(" \\"), Line()));

list[tuple[str, Tseq]] d_lst = 
  [
   <"grouped (with groupings)", tseq(group_, d1, _group)>,
   <"many indented lines", cat("margin", indent(2), br, "first", indent(2), br, "second", br, "third", br, "fourth", dedent, dedent, br, "margin")>,
   <"empty block of declarations", cat("class X ", declBlock([]), ";")>,
   <"block of declarations", cat("class X ", declBlock(["int foo;", "int bar;", "int baz;"]), ";")>,
   <"block of declarations with labels", cat("class X ", declBlock([Label("private:"), "int foo;", "int bar;", Label("public:"), "int baz;"]), ";")>,
   <"nested binary operations", cat(indent(2), ppBinaryOp("*", "lvalue", inParens(ppBinaryOp("-", "value1", "value2"))), ";", dedent)>,
   <"unary and binary operations", cat(indent(2), ppBinaryOp("=", "value", ppUnaryOp("-", "another_value")), ";", dedent)>,
   <"empty block statement", stmtBlock([])>,
   <"block statement", stmtBlock(["int foo = 0;", Label("inc:"), "foo += 1;", "foo -= 1;", Label("done:"), "return foo;"])>,
   <"function call", cat("DoItL", argList(["12345", "&x", "67890", "0"]), ";")>,
   <"grouped list of data", groupList(["12345", "234", "67890", "0"])>,
   <"stacked declarations", stackDecls(["int foo;", "int bar;", "int baz;"])>,
   <"stacked statements", stack(["foo();", "bar();", "baz();", "foobar();"])>,
   <"comma separated list", commaSep(["x", "y", "z", "w"])>,
   <"short wrapped text", fillWords(lorem_ipsum_sentence)>,
   <"long wrapped text", fillWords(lorem_ipsum_paragraph)>,
   <"nested blocks", cat("forever {", indent(2), br, d4, dedent, br, "}")>,
   <"forever block", cat("forever {", indent(2), br, "(void)0;", dedent, br, "}")>,
   <"CPP breaking", tseq(Text("#if"), cppBr, Text("defined(__SYMBIAN__)"), cppBr, Text("||"), cppBr, Text("defined(__EPOC32__)"), cppBr, Text("||"), cppBr, Text("defined(__SYMBIAN32__)"))>,
   <"flattened Union", flatten(d3)>,
   <"Union", d3>,
   <"grouped then flattened", flatten(group(d1))>,
   <"grouped", group(d1)>,
   <"flattened", flatten(d1)>,
   <"Space", tseq(Text("myspace"), Space(" "), Text("baz"))>,
   <"LvInc", tseq(Text("foobar"), Nest(LvInc(4)), Line(), Text("baz"), Nest(LvPop()), Line(), Text("bamf"))>,
   <"word", tseq("foobar")>,
   <"empty", tseqNull>
  ];

public void runTestFmtEngine() {
  //println(groupingByName("group"));
  //println(fillWords(lorem_ipsum_paragraph)); return;
  if (false) {
    println(d_lst);
  } else {
    w_lst = reverse([5, 10, 15, 25, 35, 45, 55]);
    for ( a <- [ <w, n, d> | w <- w_lst, <n, d> <- d_lst ] ) {
      //println(a[2]);
      testDoc(a[0], a[1], a[2]);
    }
  }
  if (false)
    testIncremental();
}

public void main(list[str] args) {
  runTestFmtEngine();
}
