module org::nuthatchery::pgf::rascal::TseqDeque

/*
  A banker's deque for Rascal. Operates on an external deque type. The
  deque is generic in that it may contain any Rascal values. It is an
  error to use the tail operations on an empty deque (i.e. things will
  break); use dqIsEmpty first to check.

  E.g.

  (rimport "org::nuthatchery::pgf::rascal::Token")
  (reval "tseqEmpty()")
  (reval "tseqIs(tseqEmpty())")
  (reval "tseqIsEmpty(tseqEmpty())")
  (def e (rcall "tseqEmpty"))
  (rcall "tseqPushF" e (rinteger 1))
  (rcall "tseqMake" (reval "[1, 2, 3, 4]"))
*/

alias Tseq = value;

@doc{Returns an empty deque.}
@javaClass{org.magnolialang.testutil.BankersDequeRsc}
public java Tseq tseqEmpty();

@doc{Checks if the given value is a deque.}
@javaClass{org.magnolialang.testutil.BankersDequeRsc}
public java bool tseqIs(value q);

@doc{Checks if the given deque is empty.}
@javaClass{org.magnolialang.testutil.BankersDequeRsc}
public java bool tseqIsEmpty(Tseq q);

@doc{Pushes element to the front of the queue.}
@javaClass{org.magnolialang.testutil.BankersDequeRsc}
public java Tseq tseqPushF(Tseq q, value e);

@doc{Pushes element to the rear of the queue.}
@javaClass{org.magnolialang.testutil.BankersDequeRsc}
public java Tseq tseqPushR(Tseq q, value e);

@doc{Returns the first element in the front.}
@javaClass{org.magnolialang.testutil.BankersDequeRsc}
public java value tseqPeekF(Tseq q);

@doc{Returns the first element at the rear.}
@javaClass{org.magnolialang.testutil.BankersDequeRsc}
public java value tseqPeekR(Tseq q);

@doc{Drops the first element from the front.}
@javaClass{org.magnolialang.testutil.BankersDequeRsc}
public java Tseq tseqTailF(Tseq q);

@doc{Drops the first element from the rear.}
@javaClass{org.magnolialang.testutil.BankersDequeRsc}
public java Tseq tseqTailR(Tseq q);

@doc{Appends second argument to the front of the first.}
@javaClass{org.magnolialang.testutil.BankersDequeRsc}
public java Tseq tseqPrepend(Tseq q, Tseq qa);

@doc{Appends second argument to the rear of the first.}
@javaClass{org.magnolialang.testutil.BankersDequeRsc}
public java Tseq tseqAppend(Tseq q, Tseq qa);

@doc{Makes a new deque, using elements of the given list.}
@javaClass{org.magnolialang.testutil.BankersDequeRsc}
public java Tseq tseqMake(list[value] lst);

@doc{Pops an element from the front.}
@javaClass{org.magnolialang.testutil.BankersDequeRsc}
public java tuple[value, Tseq] tseqPopF(Tseq q);

