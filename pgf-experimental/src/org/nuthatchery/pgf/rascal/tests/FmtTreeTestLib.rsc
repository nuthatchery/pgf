// run with rascal-src
module org::nuthatchery::pgf::rascal::tests::FmtTreeTestLib

import List;
import String;

import org::nuthatchery::pgf::rascal::FmtPrim;
import org::nuthatchery::pgf::rascal::FmtHl;

data Tree = Node(str s, list[Tree] lst);

public DOC show_tree1(Tree t) {
  return group(tseq(text(t.s),
                      nest(size(t.s), show_bracket1(t.lst))));
}

public DOC show_bracket1(list[Tree] ts) {
  if (isEmpty(ts))
    return nil();
  return tseq(text("["),
                nest(1, show_trees1(ts)),
                text("]"));
}

public DOC show_trees1(list[Tree] lst) {
  <t, ts> = pop(lst);
  if (isEmpty(ts))
    return show_tree1(t);
  return tseq(show_tree1(t),
                text(","),
                line(),
                show_trees1(ts));
}

public DOC show_tree2(Tree t) {
  return tseq(text(t.s),
                show_bracket2(t.lst));
}

public DOC show_bracket2(list[Tree] ts) {
  if (isEmpty(ts))
    return nil();
  return bracketize("[",
                    show_trees2(ts),
                    "]");
}

// The paper uses exactly the same implementation as for show_trees1,
// but that is probably not right. Surely we want to call the *2
// variants of the functions.
public DOC show_trees2(list[Tree] lst) {
  <t, ts> = pop(lst);
  if (isEmpty(ts))
    return show_tree2(t);
  return tseq(show_tree2(t),
              text(","),
              line(),
              show_trees2(ts));
}
