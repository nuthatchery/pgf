package org.nuthatchery.pgf.javaformatter;

import nuthatch.stratego.pattern.SPatternFactory;
import nuthatch.pattern.Pattern;

import org.spoofax.interpreter.terms.IStrategoTerm;


@SuppressWarnings("unchecked")
public class AsFix2Patterns {

	public static Pattern<IStrategoTerm, Integer> id(Pattern<IStrategoTerm, Integer> arg0) {
		return termAppl("id", arg0);
	}

	@SafeVarargs
	private static Pattern<IStrategoTerm, Integer> termAppl(String string, Pattern<IStrategoTerm, Integer> ... children) {
		return SPatternFactory.appl(string, children);
	}

	public static Pattern<IStrategoTerm, Integer> alt(Pattern<IStrategoTerm, Integer> arg0, Pattern<IStrategoTerm, Integer> arg1) {
		return termAppl("alt", arg0, arg1);
	}

	public static Pattern<IStrategoTerm, Integer> amb(Pattern<IStrategoTerm, Integer> arg0) {
		return termAppl("amb", arg0);
	}

	public static Pattern<IStrategoTerm, Integer> appl(Pattern<IStrategoTerm, Integer> arg0, Pattern<IStrategoTerm, Integer> arg1) {
		return termAppl("appl", arg0, arg1);
	}

	public static Pattern<IStrategoTerm, Integer> assoc() {
		return termAppl("assoc");
	}

	public static Pattern<IStrategoTerm, Integer> assoc(Pattern<IStrategoTerm, Integer> arg0) {
		return termAppl("assoc", arg0);
	}

	public static Pattern<IStrategoTerm, Integer> attrs(Pattern<IStrategoTerm, Integer> arg0) {
		return termAppl("attrs", arg0);
	}

	public static Pattern<IStrategoTerm, Integer> avoid() {
		return termAppl("avoid");
	}

	public static Pattern<IStrategoTerm, Integer> bracket() {
		return termAppl("bracket");
	}

	public static Pattern<IStrategoTerm, Integer> cf(Pattern<IStrategoTerm, Integer> arg0) {
		return termAppl("cf", arg0);
	}

	public static Pattern<IStrategoTerm, Integer> char_(Pattern<IStrategoTerm, Integer> arg0) {
		return termAppl("char", arg0);
	}

	public static Pattern<IStrategoTerm, Integer> charClass(Pattern<IStrategoTerm, Integer> arg0) {
		return termAppl("char-class", arg0);
	}

	public static Pattern<IStrategoTerm, Integer> character(Pattern<IStrategoTerm, Integer> arg0) {
		return termAppl("character", arg0);
	}

	public static Pattern<IStrategoTerm, Integer> cons(Pattern<IStrategoTerm, Integer> arg0) {
		return termAppl("cons", arg0);
	}

	public static Pattern<IStrategoTerm, Integer> empty() {
		return termAppl("empty");
	}

	public static Pattern<IStrategoTerm, Integer> func(Pattern<IStrategoTerm, Integer> arg0, Pattern<IStrategoTerm, Integer> arg1) {
		return termAppl("func", arg0, arg1);
	}

	public static Pattern<IStrategoTerm, Integer> iter(Pattern<IStrategoTerm, Integer> arg0) {
		return termAppl("iter", arg0);
	}

	public static Pattern<IStrategoTerm, Integer> iter_sep(Pattern<IStrategoTerm, Integer> arg0, Pattern<IStrategoTerm, Integer> arg1) {
		return termAppl("iter-sep", arg0, arg1);
	}

	public static Pattern<IStrategoTerm, Integer> iter_star(Pattern<IStrategoTerm, Integer> arg0) {
		return termAppl("iter-star", arg0);
	}

	public static Pattern<IStrategoTerm, Integer> iter_star_sep(Pattern<IStrategoTerm, Integer> arg0, Pattern<IStrategoTerm, Integer> arg1) {
		return termAppl("iter-star-sep", arg0, arg1);
	}

	public static Pattern<IStrategoTerm, Integer> layout() {
		return termAppl("layout");
	}

	public static Pattern<IStrategoTerm, Integer> left() {
		return termAppl("left");
	}

	public static Pattern<IStrategoTerm, Integer> lex(Pattern<IStrategoTerm, Integer> arg0) {
		return termAppl("lex", arg0);
	}

	public static Pattern<IStrategoTerm, Integer> list(Pattern<IStrategoTerm, Integer> arg0) {
		return termAppl("list", arg0);
	}

	public static Pattern<IStrategoTerm, Integer> lit(Pattern<IStrategoTerm, Integer> arg0) {
		return termAppl("lit", arg0);
	}

	public static Pattern<IStrategoTerm, Integer> no_attrs() {
		return termAppl("no-attrs");
	}

	public static Pattern<IStrategoTerm, Integer> non_assoc() {
		return termAppl("non-assoc");
	}

	public static Pattern<IStrategoTerm, Integer> opt(Pattern<IStrategoTerm, Integer> arg0) {
		return termAppl("opt", arg0);
	}

	public static Pattern<IStrategoTerm, Integer> parametrized_sort(Pattern<IStrategoTerm, Integer> arg0, Pattern<IStrategoTerm, Integer> arg1) {
		return termAppl("parametrized-sort", arg0, arg1);
	}

	public static Pattern<IStrategoTerm, Integer> parsetree(Pattern<IStrategoTerm, Integer> arg0, Pattern<IStrategoTerm, Integer> arg1) {
		return termAppl("parsetree", arg0, arg1);
	}

	public static Pattern<IStrategoTerm, Integer> prefer() {
		return termAppl("prefer");
	}

	public static Pattern<IStrategoTerm, Integer> prod(Pattern<IStrategoTerm, Integer> arg0, Pattern<IStrategoTerm, Integer> arg1, Pattern<IStrategoTerm, Integer> arg2) {
		return termAppl("prod", arg0, arg1, arg2);
	}

	public static Pattern<IStrategoTerm, Integer> range(Pattern<IStrategoTerm, Integer> arg0, Pattern<IStrategoTerm, Integer> arg1) {
		return termAppl("range", arg0, arg1);
	}

	public static Pattern<IStrategoTerm, Integer> reject() {
		return termAppl("reject");
	}

	public static Pattern<IStrategoTerm, Integer> right() {
		return termAppl("right");
	}

	public static Pattern<IStrategoTerm, Integer> seq(Pattern<IStrategoTerm, Integer> arg0, Pattern<IStrategoTerm, Integer> arg1) {
		return termAppl("seq", arg0, arg1);
	}

	public static Pattern<IStrategoTerm, Integer> sort(Pattern<IStrategoTerm, Integer> arg0) {
		return termAppl("sort", arg0);
	}

	public static Pattern<IStrategoTerm, Integer> strategy(Pattern<IStrategoTerm, Integer> arg0, Pattern<IStrategoTerm, Integer> arg1) {
		return termAppl("strategy", arg0, arg1);
	}

	public static Pattern<IStrategoTerm, Integer> term(Pattern<IStrategoTerm, Integer> arg0) {
		return termAppl("term", arg0);
	}

	public static Pattern<IStrategoTerm, Integer> tuple(Pattern<IStrategoTerm, Integer> arg0, Pattern<IStrategoTerm, Integer> arg1) {
		return termAppl("tuple", arg0, arg1);
	}

	public static Pattern<IStrategoTerm, Integer> varsym(Pattern<IStrategoTerm, Integer> arg0) {
		return termAppl("varsym", arg0);
	}

}
