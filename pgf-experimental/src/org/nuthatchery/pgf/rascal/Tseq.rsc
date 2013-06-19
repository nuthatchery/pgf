module org::nuthatchery::pgf::rascal::Tseq

// Tseq is an abstract data type.
// This controls which Tseq implementation is used.
extend org::nuthatchery::pgf::rascal::TseqList;
//extend org::nuthatchery::pgf::rascal::TseqDeque;

// It seems that 'extend' may only be safely used in a "virtual"
// modules such as this.
//
// Wanted to do the 'extend' in the Token module for backward
// compatibility, but importing Token via two or more modules we would
// get "RedeclaredVariableError Redeclared variable: eofTok". This is
// presumably a Rascal bug, but then, 'extend' isn't really
// documented.
