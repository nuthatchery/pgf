// run with rascal-src
module org::nuthatchery::pgf::rascal::Groupings

import org::nuthatchery::pgf::rascal::engines::FmtEngine;
import org::nuthatchery::pgf::rascal::Token;
import org::nuthatchery::pgf::rascal::Tseq;

import IO;
import List;

public void grpEofDefault(value st, str name) {
  throw "unclosed \'<name>\' grouping";
}

value nil_begin_f() {
    return true;
}

tuple[value, Tseq] nil_put_f(value v, Token t) {
  // no state change, no output
  return <v, tseqNull>;
}

tuple[value, Tseq] nil_accept_f(value v, Tseq s, str name) {
  return <v, tseqNull>;
}

Tseq nil_end_f(value v) { 
  return tseqNull;
}

public Grouping newGrouping(str name) {
  return Grouping(name, nil_begin_f, nil_put_f, nil_accept_f,
                  nil_end_f, grpEofDefault);
}

value tseq_begin_f() {
  return tseqNull;
}

tuple[value, Tseq] tseq_put_f(value v, Token t) {
  return <tseq(v, t), tseqNull>;
}

tuple[value, Tseq] tseq_accept_f(value v, Tseq s, str name) {
  return <tseq(v, s), tseqNull>;
}

Grouping makeGroup() {
  Grouping g = newGrouping("group");
  g.begin = tseq_begin_f;
  g.put = tseq_put_f;
  g.accept = tseq_accept_f;
  // cannot use 'group' directly as it's an overload;
  // must resolve the overload here
  g.end = Tseq (value v) { return group(v); };
  addGrouping(g.name, g);
  return g;
}

Grouping groupGrouping = makeGroup();

public Token group_ = BeginGr("group");
public Token _group = EndGr("group");

public void main(list[str] args) {
}
