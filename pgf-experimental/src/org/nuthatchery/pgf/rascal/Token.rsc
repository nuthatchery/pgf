// run with rascal-src
module org::nuthatchery::pgf::rascal::Token

import org::nuthatchery::pgf::rascal::Tseq;

import Exception;
import IO;
import List;
import String;

// --------------------------------------------------
// Token
// --------------------------------------------------

data Lv = 
  LvInc(int n) |
  LvStr(str s) |
  LvAbs(int n) |
  LvRel(int n) |
  LvPop();

// Common tokens. These token types are used from front end to back
// end. Note that Space may also be "". ((Class class, set[Property]
// properties) to be added to Text later.)
data Token
  = Text(str s)
  | Line()
  | Nest(Lv lv)
  | Label(str s)
  | Space(str s)
  | Width(num w)
  | BeginGr(str s) // grouping start
  | EndGr(str s) // grouping end
  | NilToken()
  | Eof();

alias TokenCat = str;

anno TokenCat Token@cat;

//public Token Space(str s) { return Space(s, DefaultStrength); }

public int width(Text(s)) = size(s);
public int width(Space(s)) = size(s);
public default int width(Token t) = 0;

public str toStr(Text(s)) = s;
public str toStr(Space(s)) = s;
public default str toStr(Token t) = "";

public TokenCat getCategory(t:Text(_)) {
	return t@cat ? "TXT";
}

public TokenCat getCategory(t:Space(_)) {
	return t@cat ? "SPC";
}

public default TokenCat getCategory(Token t) {
	return t@cat ? ".";
}

// --------------------------------------------------
// Token operations for Tseq
// --------------------------------------------------

data Token = LazyTseq(Tseq (value) f, value v);

Token eofTok = Eof();

public Tseq tseqNull = tseqEmpty();

Tseq listToTseq(list[value] xs) {
  //println(xs);
  Tseq q = tseqNull;
  for (x <- xs) {
    switch (x) {
    case false: ;
    case Token t: q = tseqPushR(q, t);
    case str s: q = tseqPushR(q, Text(s));
    case list[value] lst: q = tseqAppend(q, listToTseq(lst));
    default:
      {
        if (tseqIs(x))
          q = tseqAppend(q, x);
        else
          throw "unsupported tseq content <x>";
      }
    }
  }
  return q;
}

public Tseq tseq(value xs...) {
  return listToTseq(xs);
}

public Tseq tseqEnsure(value x) {
  // contortions to ensure works with "exotic" types
  if (tseqIs(x) && (Tseq ts := x))
    return ts;
  return tseq(x);
}
  
/**
   Important: tseqIsEmpty may report false even if there actually are
   no more tokens available, which is a consequence of lazy tokens
   that produce nothing. Be sure to store the result sequence to avoid
   non-termination.

   With current Rascal this will not work if the choices are
   implemented using overloading. (See "TestToken.rsc".) Instead we
   use a 'switch', and specify the 'default' modifier to allow for
   extending through overloading.

   (use 'org.magnolialang.testutil.bankers-deque)
   (rimport "org::nuthatchery::pgf::rascal::Token")
   (def ab (rcall "tseq" (rstring "a") (rstring "b")))
   (dq-peek-f ab)
   (rcall "tseqGet" ab)
   (rcall "tseqToList" ab)
   (rcall "tseqPopF" ab)
 */
public default tuple[Token, Tseq] tseqGet(Tseq q) {
  while (true) {
    if (tseqIsEmpty(q))
      return <eofTok, tseqNull>;
    value e;
    <e, q> = tseqPopF(q);
    switch (e) 
      {
      case LazyTseq(f, v):
        {
          Tseq ts = f(v);
          q = tseqPrepend(q, ts);
        }
      case Token t:
        {
          return <t, q>;
        }
      default:
        {
          throw "tseqGet from improper Tseq <x>";
        }
      }
  }
}

public list[Token] tseqToList(Tseq ts) {
  list[Token] lst = [];
  while (true) {
    <t, ts> = tseqGet(ts);
    if (t == Eof())
      return lst;
    lst += [t];
  }
}

public list[Token] tseqTake(Tseq ts, int n) {
  list[Token] lst = [];
  while (n > 0) {
    <t, ts> = tseqGet(ts);
    if (t == Eof())
      return lst;
    lst += [t];
    n += 1;
  }
}

public Tseq put(Token t, Tseq ts) {
  return tseqPushR(ts, t);
}

// --------------------------------------------------
// union
// --------------------------------------------------

data Token = Union(Tseq ldoc, Tseq rdoc);

public Token union(value l, value r) {
  return Union(tseqEnsure(l), tseqEnsure(r));
}

public Tseq unionAsTseq(value l, value r) {
  return tseqMake([union(l, r)]);
}
