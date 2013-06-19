// run with rascal-src
module org::nuthatchery::pgf::rascal::FmtUtil

import org::nuthatchery::pgf::rascal::engines::FmtEngine;
import org::nuthatchery::pgf::rascal::Token;
import org::nuthatchery::pgf::rascal::Tseq;

import IO;
import List;

// deprecated (use tseq)
public Tseq toTokenStream(value x) {
  return tseq(x);
}

// deprecated (use tseq)
public Tseq cat(value xs...) {
  return tseq(xs);
}

public Token br = Line();

public Token nbsp = Text(" ");

public Token sp = union(nbsp, br);

public Token indent0 = Nest(LvAbs(0));

public Token align = Nest(LvRel(0));

public Token dedent = Nest(LvPop());

public Token indent(int n) { return Nest(LvInc(n)); }

public Token exdent(int n) { return Nest(LvInc(-n)); }

public Tseq sepBy(Token sep, list[value] elems) {
  bool first = true;
  Tseq r = tseqNull;
  for (x <- elems) {
    if (first)
      first = false;
    else
      r = tseqPushR(r, sep);
    r = tseqAppend(r, tseq(x));
  }
  return r;
}

public Tseq sepBy(value sep_, list[value] elems) {
  sep = tseq(sep_);
  bool first = true;
  Tseq r = tseqNull;
  for (x <- elems) {
    if (first)
      first = false;
    else
      r = tseqAppend(r, sep);
    r = tseqAppend(r, tseq(x));
  }
  return r;
}

/** Stacks specified elements vertically. */
public Tseq stack(list[value] elems) {
  return sepBy(br, elems);
}

/** Puts breakable space between specified elements.
 */
public Tseq fill(list[value] elems) {
  return sepBy(sp, elems);
}

public list[str] splitWords(str s) {
  list[str] lst = [];
  while (/<word:\S+><post:.*>$/m := s) {
    lst += word;
    s = post;
  }
  return lst;
}

/** Breaks the given string into words and puts breakable space
    between them.
 */
public Tseq fillWords(str s) {
  return fill(splitWords(s));
}
