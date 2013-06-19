// run with rascal-src
module org::nuthatchery::pgf::rascal::tests::TestToken

import org::nuthatchery::pgf::rascal::Token;
import org::nuthatchery::pgf::rascal::Tseq;

import IO;
import List;
import Type; // for typeOf

// more specific overloads may match if the 'default' tseqGet is undefined
list[Tseq] testData = 
  [
   //tseq(["{",Nest(LvInc(2)),Nest(LvPop()),Line(),"}"]) // works ok
   tseq(["class X ",tseq(["{",Nest(LvInc(2)),Nest(LvPop()),Line(),"}"]),";"]) // does not work with or without 'default' getter, perhaps since we have two unrelated types as list elements, resulting in 'value' being inferred, but why no dynamic inference for ListTseq, it seems that overload is not being considered -- seems like a Rascal bug, may need a workaround -- possibly introduced in https://github.com/cwi-swat/rascal/commit/07dd838885e759ad0b6dfbcc685f9b2415f57927#src/org/rascalmpl/interpreter/result/OverloadedFunction.java -- as workaround we now implement tseqGet using a 'switch' rather than overloading
   //tseq([tseq([Text("x")])]) // works only without default
   //tseq([tseqNull]) // does not work with 'default', presumably due to the static type of the list elements (here: tseqNull) being 'value'; otherwise does work
   ];

void consumeTseq(Tseq s) {
  while (true) {
    println("get from <s> (<typeOf(s)>)");
    <t, s> = tseqGet(s);
    if (t == Eof())
      return;
  }
}

void testOverloading() {
  str f(str s) { return "str"; }
  str f(value s) { return "value"; }
  str f([e]) { return f(e); }

  value v1 = "string";
  println(f(v1)); // => "str"
  str v2 = "string";
  println(f(v2)); // => "str"

  /* These yield "str" when str f(value s)
     is undefined, but otherwise "value". */
  v3 = ["string"];
  println(f(v3)); // => "value"
  list[str] v4 = ["string"];
  println(f(v4)); // => "value"
}

public void runTestToken() {
  testOverloading();
  for (s <- testData) {
    println(s);
    consumeTseq(s);
  }
}

public void main(list[str] args) {
  runTestToken();
}
