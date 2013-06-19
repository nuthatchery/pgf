module org::nuthatchery::pgf::rascal::TseqList

/*
  A pure Rascal implementation of the Tseq type.
*/

import List;

data Tseq = ListTseq(list[value] lst);

@doc{Returns an empty deque.}
public Tseq tseqEmpty() = ListTseq([]);

@doc{Checks if the given value is a deque.}
public bool tseqIs(value q) = (ListTseq(_) := q);

@doc{Checks if the given deque is empty.}
public bool tseqIsEmpty(Tseq q) = (ListTseq([]) := q);

@doc{Pushes element to the front of the queue.}
public Tseq tseqPushF(ListTseq(lst), value e) = ListTseq([e] + lst);

@doc{Pushes element to the rear of the queue.}
public Tseq tseqPushR(ListTseq(lst), value e) = ListTseq(lst + [e]);

@doc{Returns the first element in the front.}
public value tseqPeekF(ListTseq(lst)) = ListTseq(head(lst));

@doc{Returns the first element at the rear.}
public value tseqPeekR(ListTseq(lst)) = ListTseq(last(lst));

@doc{Drops the first element from the front.}
public Tseq tseqTailF(ListTseq(lst)) = ListTseq(tail(lst));

@doc{Drops the first element from the rear.}
public Tseq tseqTailR(ListTseq(lst)) = ListTseq(remove(lst, size(lst) - 1));

@doc{Appends second argument to the front of the first.}
public Tseq tseqPrepend(ListTseq(l1), ListTseq(l2)) = ListTseq(l2 + l1);

@doc{Appends second argument to the rear of the first.}
public Tseq tseqAppend(ListTseq(l1), ListTseq(l2)) = ListTseq(l1 + l2);

@doc{Makes a new deque, using elements of the given list.}
public Tseq tseqMake(list[value] lst) = ListTseq(lst);

@doc{Pops an element from the front.}
public tuple[value, Tseq] tseqPopF(ListTseq(lst)) = <head(lst), ListTseq(tail(lst))>;
