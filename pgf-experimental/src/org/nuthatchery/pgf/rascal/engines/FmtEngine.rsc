// run with rascal-src
module org::nuthatchery::pgf::rascal::engines::FmtEngine

import org::nuthatchery::pgf::rascal::ListOp;
import org::nuthatchery::pgf::rascal::Token;
import org::nuthatchery::pgf::rascal::Tseq;

import IO;
import List;
import String;

// --------------------------------------------------
// indentation
// --------------------------------------------------

alias LvStack = list[str];

// from Magnolia compiler
str spaces(int n) {
  return ("" | "<it> " | x <- [0..n], x > 0);
}

str subMargin(int k, str s, LvInc(n)) {
  if (n >= 0) {
    return s + spaces(n);
  } else {
    int len = size(s);
    int nlen = len + n;
    if (nlen > 0)
      return substring(s, 0, nlen);
    else
      return "";
  }
}

str subMargin(int k, str s, LvStr(ns)) {
  return s + ns;
}

str subMargin(int k, str s, LvAbs(n)) {
  if (n > 0)
    return spaces(n);
  else
    return "";
}

str subMargin(int k, str s, LvRel(n)) {
  return subMargin(k, s, LvAbs(k + n));
}

// st:: indentation state
// k:: current column (integer)
// s:: indentation string to adjust (string)
// lv:: level specification (Lv)
// Returns:: new indentation state and string
tuple[LvStack, str] margin(LvStack st, int k, str s, LvPop()) {
  return spop(st);
}

tuple[LvStack, str] margin(LvStack st, int k, str s, Lv lv) {
  ns = subMargin(k, s, lv);
  nst = spush(st, s);
  return <nst, ns>;
}

// --------------------------------------------------
// groupings
// --------------------------------------------------

/*
  The operations that must be defined for each grouping.

  These Grouping functions:

  begin:: creates fresh state for this grouping;
          returns state for GrpSt (any value)

  put:: buffers a token within region

  accept:: accepts a token from an inner grouping into this one

  end:: ends this grouping

  eof:: handles an EOF within this grouping

  The way we do this is that we have both an input stream and grouping
  state in the algorithm. If there is any grouping state then the
  input is fed into the grouping machinery. Some input may be held in
  the grouping state before it becomes available to the algorithm
  proper. Calling 'flush' will cause an error if there's an incomplete
  grouping.
*/

data Grouping = 
    Grouping(
             str name,
             value () begin,
             tuple[value, Tseq] (value, Token) put,
             tuple[value, Tseq] (value, Tseq, str) accept,
             Tseq (value) end,
             void (value, str) eof
             );

public map[str, Grouping] grpMap = ();

public void addGrouping(str name, Grouping g) {
  grpMap[name] = g;
}

public Grouping groupingByName(str name) {
  return grpMap[name];
}

// tp:: grouping type (Grouping)
// st:: grouping-specific state (any)
data Token = GrpSt(Grouping tp, value st) | 
  NoGrpSt();

// cw:: specified page width (constant)
// w:: page width
// outDoc:: formatted document
// inDoc:: unread input
// k:: current column
// lvStack:: nesting stack
// i:: nesting string
// bt:: backtracking state (if any; can be chained)
// grp:: grouping state (may be NoGrpSt)
// grps:: grouping stack
data FmtSt = FmtSt(int cw, num w, Tseq outDoc, Tseq inDoc, 
                   int k, LvStack lvStack, str i, FmtSt bt,
                   Token grp, list[Token] grps) | 
  NoFmtSt();

// Emits the specified tokens from a source group to an outer group.
// The outer group (if any) is assumed to be in the 'grp' field, and
// the source group need not be in the state any longer (it must be
// specified as 'sg'). The outer group may further emit tokens
// forward, all the way up to the top-level (i.e. beyond all the
// groupings).
//
// st:: formatting state (FmtSt)
// sg:: source group (Token)
// s:: token stream to receive (Tseq or tseqNull)
// Returns:: formatting state (FmtSt)
FmtSt grpEmit(FmtSt st, Token sg, Tseq s) {
  FmtSt to_top() {
    st.inDoc = tseqPrepend(st.inDoc, s);
    return st;
  }

  FmtSt to_outer(str name, Token tg) {
    g_type = tg.tp;
    g_st = tg.st;
    <n_g_st, r> = g_type.accept(g_st, s, name);
    n_grp = GrpSt(g_type, n_g_st);
    FmtSt n_st = st;
    n_st.grp = n_grp;
    if (tseqIsEmpty(r))
      return n_st;
    // Since the current grouping produced output, that
    // belongs to the next grouping, if any. Hence we make
    // the next grouping the current one, and then prepend
    // the tokens to be received.
    st = grpUnshift(n_st);
    n_st.inDoc = tseqPrepend(st.inDoc, r);
    return n_st;
  }

  if (tseqIsEmpty(s))
    return st;

  Token tg = st.grp;
  if (tg == NoGrpSt())
    return to_top();
  return to_outer(sg.tp.name, tg);
}

// st:: formatting state (FmtSt)
// Returns:: formatting state (FmtSt)
FmtSt grpFlush(FmtSt st) {
  grp = st.grp;
  if (grp != NoGrpSt()) {
    g_type = grp.tp;
    // Executed for side effects only.
    // Intended for checking state,
    // and reporting errors.
    g_type.eof(grp.st, g_type.name);
  }
  return st;
}

// st:: formatting state (FmtSt)
// grp:: group to push (Token)
// Returns:: formatting state (FmtSt)
FmtSt grpPush(FmtSt st, Token grp) {
  //println("push <grp> <st.grp> <st.grps>");
  Token old_grp = st.grp;
  if (old_grp == NoGrpSt()) {
    st.grp = grp;
    return st;
  } else {
    st.grp = grp;
    st.grps = spush(st.grps, old_grp);
    return st;
  }
}

// st:: formatting state (FmtSt)
// Returns:: formatting state (FmtSt)
FmtSt grpPop(FmtSt st) {
  //println("pop <st.grp> <st.grps>");
  if (st.grp == NoGrpSt())
    throw "no grouping to pop";
  grps = st.grps;
  if (isEmpty(grps)) {
    st.grp = NoGrpSt();
    st.grps = grps;
    return st;
  }
  <grps, grp> = spop(grps);
  st.grp = NoGrpSt();
  st.grps = grps;
  return st;
}

// st:: formatting state (FmtSt)
// Returns:: formatting state (FmtSt)
FmtSt grpUnshift(FmtSt st) {
  Token old_grp = st.grp;
  st = grpPop(st);
  st.inDoc = tseqPushF(st.inDoc, old_grp);
  return st;
}

// st:: formatting state (FmtSt)
// h:: Begin token (Token)
// Returns:: formatting state (FmtSt)
FmtSt grpBegin(FmtSt st, Token h) {
  Grouping g_type = groupingByName(h.s);
  Token inner = GrpSt(g_type, g_type.begin());
  return grpPush(st, inner);
}

// st:: formatting state (FmtSt)
// t:: End token (Token)
// Returns:: formatting state (FmtSt)
FmtSt grpEnd(FmtSt st, Token t) {
  Grouping t_type = groupingByName(t.s);
  Token grp = st.grp;
  if (grp == NoGrpSt()) {
    t_name = t_type.name;
    ctx = tseqTake(st.inDoc, 5);
    throw "close <t_name> grouping without open: before <ctx>";
  }
  Grouping g_type = grp.tp;
  if (g_type != t_type) {
    t_name = t_type.name;
    g_name = g_type.name;
    throw "close <t_name> grouping while <g_name> grouping open";
  }
  value g_st = grp.st;
  Tseq r = g_type.end(g_st);
  st = grpPop(st);
  if (tseqIsEmpty(r))
    return st;
  return grpEmit(st, grp, r);
}

// st:: formatting state, with open groupings (FmtSt)
// h:: token belonging in the grouping (Token)
// Returns:: formatting state (FmtSt)
FmtSt grpPut(FmtSt st, Token h) {
  Token grp = st.grp;
  Grouping g_type = grp.tp;
  value g_st = grp.st;
  <n_g_st, r> = g_type.put(g_st, h);
  n_grp = GrpSt(g_type, n_g_st);
  FmtSt n_st = st;
  n_st.grp = n_grp;
  return grpEmit(grpUnshift(n_st), grp, r);
}

// --------------------------------------------------
// formatting algorithm
// --------------------------------------------------

// w:: page width (integer)
// inDoc:: unread input
public FmtSt newFmtSt(int w, Tseq inDoc) {
  return FmtSt(w, w, tseqNull, inDoc, 
               0, [], "", NoFmtSt(), NoGrpSt(), []);
}

// w:: page width (integer)
public FmtSt newFmtSt(int w) {
  return newFmtSt(w, tseqNull);
}

public FmtSt newFmtSt() {
  return newFmtSt(80);
}

// Flushes buffered documents, committing decisions made thus far.
// After this it is safe to consume all of 'outDoc'.
public FmtSt flush(FmtSt st) {
  //println("flush <st.grp> <st.grps>");
  st = grpFlush(st);
  st.bt = NoFmtSt();
  return st;
}

// This function unshifts grouping state out of the way so that
// prepending input 's' becomes possible. Note that any tokens that
// have reached the algorithm proper have been past all groupings
// already, and have nothing to do with groupings.
//
// st:: current state (FmtSt)
// s:: stream to prepend to inDoc (Tseq)
// Returns:: new state (FmtSt)
public FmtSt FmtSt_cons(FmtSt st, Tseq s) {
  Token grp = st.grp;
  Tseq inDoc = st.inDoc;
  if (grp == NoGrpSt()) {
    st.inDoc = tseqPrepend(inDoc, s);
    st.grp = grp;
    return st;
  }
  // outermost grouping will be read first
  inDoc = tseqPrepend(tseq(s, reverse(st.grps), grp), inDoc);
  st.inDoc = inDoc;
  st.grp = NoGrpSt();
  st.grps = [];
  return st;
}

// Before calling this function ensure that all state (except for the
// argument token) is consistent and in 'st'. Note that this function
// does not deal with groupings.
//
// Note that 'outDoc' cannot externally be considered committed for as
// long as backtracking is possible. Except if one specifically wants
// to flush the output.
//
// st:: current state (FmtSt)
// d:: token to process (Token)
// Returns:: new state (FmtSt)
FmtSt processTokenAlgo(FmtSt st, Token d) {
  k = st.k;
  i = st.i;
  switch (d) 
    {
    case Nest(lv): 
      { 
        <lvStack, i> = margin(st.lvStack, k, i, lv);
        st.i = i;
        st.lvStack = lvStack;
        return st;
      }
    case Text(s): 
      {
        // Here we must check whether the text still fits. If it
        // doesn't, we'll only continue if we don't have a way back.
        k += size(s);
        if ((k > st.w) && (st.bt != NoFmtSt())) {
          //println("backtrack at <k>");
          return st.bt; // backtrack
        }
        st.k = k;
        st.outDoc = tseqPushR(st.outDoc, d);
        return st;
      }
    case Line(): 
      { 
        // A line break always fits, and then we're committed, and won't
        // backtrack from here. Note that if you need line suffixes you
        // can use something like union(Text(" "), tseq(Text("\\"),
        // Line())). Here although if the left choice won't fit, then the
        // right won't fit either, the right choice is still taken if
        // backtracking is not possible.
        st.outDoc = tseqAppend(st.outDoc, tseqMake([Line(), Text(i)]));
        st.k = size(i);
        st.bt = NoFmtSt();
        return st;
      }
    case Union(l, r): 
      {
        // Pick left option, leave right for backtracking.
        FmtSt r_st = FmtSt_cons(st, r);
        FmtSt l_st = st;
        l_st.bt = r_st;
        return FmtSt_cons(l_st, l);
      }
    case Width(w): 
      {
        st.w = w;
        return st;
      }
    case NilToken(): 
      {
        return st;
      }
      // Spacer compat. Space(str s);
    case Space(s): 
      {
        return FmtSt_cons(st, unionAsTseq(Text(s), Line()));
      }
    default:
      {
  	throw "FmtEngine::processTokenAlgo: unknown token: <d>";
      }
    }
}

// Processes a token (if there is input). Supports groupings.
//
// st:: current state (FmtSt)
// Returns:: new state (FmtSt)
FmtSt processToken(FmtSt st) {
  <d, st.inDoc> = tseqGet(st.inDoc);
  if (d == Eof())
    return st;
  switch (d) {
  case GrpSt(_, _): return grpPush(st, d);
  case BeginGr(_): return grpBegin(st, d);
  case EndGr(_): return grpEnd(st, d);
  }
  if (st.grp != NoGrpSt())
    return grpPut(st, d);
  return processTokenAlgo(st, d);
}

// Whether the state has any more data that can be processed (without
// additional input).
public bool isPending(FmtSt st) {
  // Any non-GrpSt tokens are always kept in inDoc, so this is enough.
  return !tseqIsEmpty(st.inDoc);
}

// Adds a tseq to input.
public FmtSt write(FmtSt st, Tseq s) {
  st.inDoc = tseqAppend(st.inDoc, s);
  return st;
}

// Adds a token to input.
public FmtSt write(FmtSt st, Token t) {
  st.inDoc = tseqPushR(st.inDoc, t);
  return st;
}

// Drives formatting for as long as there is anything pending.
public FmtSt drive(FmtSt st) {
  while (true) {
    if (!isPending(st))
      return st;
    //println("before <st>");
    st = processToken(st);
    //println("after <st>");
  }
}

// --------------------------------------------------
// text output
// --------------------------------------------------

// We shall have a simple 'pretty' and 'print' functions that select
// margins and produce complete output. Note that the 'print' function
// shall output as it completes formatting, so that we get the desired
// pipeline behavior reminiscent of the Unix shell.

str tokenToString(Text(s)) {
  return s;
}

// Could have a configuration option in FmtSt if wanted this to be
// customizable to sometimes print CRLF, for instance.
str tokenToString(Line()) {
  return "\n";
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

// st: pretty printing state
public str pretty(FmtSt st) {
  st = flush(drive(st));
  return docToString(st.outDoc);
}

// w: output line width
// ts: a (complete) input document 
public str pretty(int w, Tseq ts) {
  FmtSt st = newFmtSt(w, ts);
  return pretty(st);
}

// Clears output buffer by printing it all out. Printing is done as
// soon as individual tokens are converted to strings.
public FmtSt printBuffered(FmtSt st) {
  Tseq ts = st.outDoc;
  while (true) {
    <t, ts> = tseqGet(ts);
    if (t == Eof()) {
      st.outDoc = ts;
      return st;
    }
    print(tokenToString(t));
  }
}

// Processes as much input as is available, and prints as much as
// safely can. Works incrementally so that printing happens as soon as
// there is text ready for output.
public FmtSt prettyPrint(FmtSt st) {
  while (true) {
    if (st.bt == NoFmtSt())
      // cannot backtrack so safe to print outDoc
      st = printBuffered(st);
    if (!isPending(st))
      return st;
    //println("before <st>");
    st = processToken(st);
    //println("after <st>");
  }
}

public FmtSt prettyPrintFlush(FmtSt st) {
  return printBuffered(flush(prettyPrint(st)));
}

public FmtSt prettyPrintFlushLn(FmtSt st) {
  st = prettyPrintFlush(st);
  println("");
  return st;
}

// --------------------------------------------------
// 'group' function
// --------------------------------------------------

Tseq lazyFlatten(value x) {
  //println("lazyFlatten <x>");
  if (Tseq ts := x) {
    Token t;
    <t, ts> = tseqGet(ts);
    //println("lazyFlatten got <t>, <ts>");
    if (t == Eof())
      return tseqNull;
    //println(ts);
    switch (t) 
      {
      case Line():
        return tseqMake([Text(" "), LazyTseq(lazyFlatten, ts)]);
      case Union(l, _):
        return tseqMake([LazyTseq(lazyFlatten, tseqPrepend(ts, l))]);
      default:
        return tseqMake([t, LazyTseq(lazyFlatten, ts)]);
      }
  } else
    throw "expected Tseq";
}

// Behaves lazily. Note the use of stateless iterators to avoid the
// cost of creating a new closure for every iteration. This is
// inspired by Lua's ipairs, although we require no invariant state.
// http://www.lua.org/pil/7.3.html
public Tseq flatten(value x) {
  x = tseqEnsure(x);
  return tseqMake([LazyTseq(lazyFlatten, x)]);
}

public Tseq group(value x) {
  x = tseqEnsure(x);
  return unionAsTseq(flatten(x), x);
}
