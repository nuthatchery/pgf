module org::nuthatchery::pgf::rascal::tests::TestSpacer

import org::nuthatchery::pgf::rascal::engines::Spacer;
import org::nuthatchery::pgf::rascal::tests::SimpleExprLang;
import org::nuthatchery::pgf::rascal::PrettyParseTree;
import org::nuthatchery::pgf::rascal::Token;
import org::nuthatchery::pgf::rascal::Tseq;



public Expr prog1 = (Expr)`abc + cde`;
public Expr prog2 = (Expr)`abc + cde + def`;
public Expr prog3 = (Expr)`!a-b`;
public Expr prog4 = (Expr)`f(abc, cde)`;
public Expr prog5 = (Expr)`a * b + c / d`;

public Tseq prog1toks = prettyParseTree(tseqNull, prog1);
public Tseq prog2toks = prettyParseTree(tseqNull, prog2);
public Tseq prog3toks = prettyParseTree(tseqNull, prog3);
public Tseq prog4toks = prettyParseTree(tseqNull, prog4);
public Tseq prog5toks = prettyParseTree(tseqNull, prog5);
