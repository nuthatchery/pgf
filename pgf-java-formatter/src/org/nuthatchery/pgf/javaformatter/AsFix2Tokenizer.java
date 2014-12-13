package org.nuthatchery.pgf.javaformatter;

import static nuthatch.library.JoinPoints.down;
import static nuthatch.library.JoinPoints.up;
import static nuthatch.pattern.StaticPatternFactory.or;
import static nuthatch.stratego.pattern.SPatternFactory._;
import static nuthatch.stratego.pattern.SPatternFactory.var;
import static org.nuthatchery.pgf.javaformatter.AsFix2Patterns.appl;
import static org.nuthatchery.pgf.javaformatter.AsFix2Patterns.cf;
import static org.nuthatchery.pgf.javaformatter.AsFix2Patterns.iter;
import static org.nuthatchery.pgf.javaformatter.AsFix2Patterns.iter_sep;
import static org.nuthatchery.pgf.javaformatter.AsFix2Patterns.iter_star;
import static org.nuthatchery.pgf.javaformatter.AsFix2Patterns.iter_star_sep;
import static org.nuthatchery.pgf.javaformatter.AsFix2Patterns.layout;
import static org.nuthatchery.pgf.javaformatter.AsFix2Patterns.lex;
import static org.nuthatchery.pgf.javaformatter.AsFix2Patterns.list;
import static org.nuthatchery.pgf.javaformatter.AsFix2Patterns.lit;
import static org.nuthatchery.pgf.javaformatter.AsFix2Patterns.opt;
import static org.nuthatchery.pgf.javaformatter.AsFix2Patterns.parametrized_sort;
import static org.nuthatchery.pgf.javaformatter.AsFix2Patterns.sort;

import java.util.ArrayList;
import java.util.List;

import nuthatch.library.BaseWalk;
import nuthatch.pattern.Environment;
import nuthatch.pattern.EnvironmentFactory;
import nuthatch.pattern.Pattern;
import nuthatch.stratego.adapter.STermCursor;
import nuthatch.stratego.adapter.SWalker;

import org.nuthatchery.pgf.config.TokenizerConfig;
import org.nuthatchery.pgf.plumbing.ForwardStream;
import org.nuthatchery.pgf.tokens.CategoryStore;
import org.nuthatchery.pgf.tokens.CtrlToken;
import org.nuthatchery.pgf.tokens.DataToken;
import org.nuthatchery.pgf.tokens.Token;
import org.nuthatchery.pgf.tokens.TokenizerHelper;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoInt;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;

public class AsFix2Tokenizer {
	protected final CategoryStore categories;
	protected final TokenizerConfig config;


	public AsFix2Tokenizer(TokenizerConfig config) {
		this.config = config;
		this.categories = null; // config.cfgCategories();
	}

	public static Pattern<IStrategoTerm, Integer> applProdArgs = appl(var("prod"), var("args"));


	public void tokenize(STermCursor parseTree, final ForwardStream<Token> output) {
		final List<Integer> endStack = new ArrayList<Integer>();
		BaseWalk<SWalker> toTerm = new BaseWalk<SWalker>() {
			@Override
			public int step(SWalker w) {
				Environment<STermCursor> env = EnvironmentFactory.env();

				if(down(w)) {
					if(w.match(applProdArgs, env)) {
						STermCursor prod = env.get("prod");
						STermCursor args = env.get("args");
						String sort = getProdSort(prod);
						if(isLex(prod, env)) {
							String string = yield(args);
							//System.err.print("\"" + string + "\":" + sort + " ");
							output.put(new DataToken(string, config.getCatForLexical(string)));
							return PARENT;
						}
						else if(isLit(prod, env)) {
							String string = yield(args);
							//System.err.print("\"" + string + "\":" + sort + " ");
							//System.out.print(string);
							output.put(new DataToken(string, config.getCatForLiteral(string)));
							return PARENT;
						}
						else if(isLayout(prod, env)) {
							String string = yield(args);
							//System.err.print("\"" + string + "\":" + sort + " ");
							//System.out.print(string);
							TokenizerHelper.splitComment(string, output, config);
							return PARENT;
						}

						if(config.cfgNestSorts().contains(sort)) {
							output.put(new CtrlToken(config.cfgCatForCtrlBegin()));
							endStack.add(w.depth());
						}

						return 2;
					}
					else if(up(w)) {
						if(!endStack.isEmpty() && endStack.get(endStack.size() - 1) == w.depth()) {
							endStack.remove(endStack.size() - 1);
							output.put(new CtrlToken(config.cfgCatForCtrlEnd()));
						}
					}
				}

				return NEXT;
			}
		};

		// instantiate walker with an example tree and the above step function
		SWalker toTermWalker = new SWalker(parseTree, toTerm);
		// run it
		toTermWalker.start();

	}


	public static String getProdSort(STermCursor tree) {
		return getSymSort(getProdType(tree));
	}

	public static final Pattern<IStrategoTerm, Integer> sortXOrParametrizedSortX = or(sort(var("x")), parametrized_sort(var("x"), _));
	public static final Pattern<IStrategoTerm, Integer> cfXOrLexXOrOptX = or(cf(var("x")), lex(var("x")), opt(var("x")));
	public static final Pattern<IStrategoTerm, Integer> iterated = or(iter(var("x")), iter_star(var("x")), iter_sep(var("x"), _), iter_star_sep(var("x"), _));


	public static String getSymSort(STermCursor symbol) {
		Environment<STermCursor> env = EnvironmentFactory.env();
		if(sortXOrParametrizedSortX.match(symbol, env) && env.get("x").getData() instanceof IStrategoString) {
			return ((IStrategoString) env.get("x").getData()).stringValue();
		}
		else if(cfXOrLexXOrOptX.match(symbol, env)) {
			return getSymSort(env.get("x"));
		}
		else if(iterated.match(symbol, env)) {
			return getSymSort(env.get("x")) + "*";
		}
		return "";
	}

	public static final Pattern<IStrategoTerm, Integer> lex = lex(_);
	public static final Pattern<IStrategoTerm, Integer> listLex = list(lex(_));


	public static boolean isLex(STermCursor tree, Environment<STermCursor> env) {
		// System.out.println(tree.treeToString());
		if(lex.match(getProdType(tree), env)) {
			return true;
		}
		if(listLex.match(getProdDefined(tree), env)) {
			System.err.println("hmm");
		}
		return false;

	}

	public static final Pattern<IStrategoTerm, Integer> lit = lit(_);


	public static boolean isLit(STermCursor tree, Environment<STermCursor> env) {
		// System.out.println(tree.treeToString());
		return lit.match(getProdType(tree), env);
	}

	public static final Pattern<IStrategoTerm, Integer> cfLayout = cf(layout());


	public static boolean isLayout(STermCursor tree, Environment<STermCursor> env) {
		// System.out.println(tree.treeToString());
		return cfLayout.match(getProdType(tree), env);
	}

	public static final Pattern<IStrategoTerm, Integer> sortOrParametrizedSort = cf(or(sort(_), parametrized_sort(_, _)));


	public static boolean isSort(STermCursor tree, Environment<STermCursor> env) {
		// System.out.println(tree.treeToString());
		return sortOrParametrizedSort.match(getProdType(tree), env);
	}


	public static STermCursor getProdType(STermCursor tree) {
		if(tree.hasName("prod")) {
			return tree.getBranchCursor(2);
		}
		else {
			throw new IllegalArgumentException("Not a production");
		}
	}


	public static STermCursor getProdDefined(STermCursor tree) {
		if(tree.hasName("prod")) {
			return tree.getBranchCursor(1);
		}
		else {
			throw new IllegalArgumentException("Not a production");
		}
	}

	public static Pattern<IStrategoTerm, Integer> appl = appl(_, _);


	String yield(STermCursor tree) {
		final StringBuilder s = new StringBuilder();
		yield(tree.getData(), s);
		/*
		 * BaseWalk<SWalker> toString = new BaseWalk<SWalker>() {
			@Override
			public int step(SWalker w) {
				if(down(w)) {
					if(w.hasName("appl")) {
						w.go(2);
					}
					else if(w.hasType(IStrategoTerm.INT)) {
						s.append(Character.toChars(((IStrategoInt) w.getData()).intValue()));
					}
					else {
						//System.out.println(w.treeToString());
					}
				}
				return NEXT;
			}
		};
		new SWalker(tree, toString).start();
		*/
		// System.out.println(" => " + s);
		return s.toString();
	}


	@SuppressWarnings("unchecked")
	private void yield(IStrategoTerm data, StringBuilder s) {
		while(data instanceof IStrategoAppl) {
			data = data.getSubterm(1);
		}

		switch(data.getTermType()) {
		case IStrategoTerm.INT:
			s.append(Character.toChars(((IStrategoInt) data).intValue()));
			break;
		case IStrategoTerm.LIST:
			for(IStrategoTerm t : (Iterable<IStrategoTerm>) data) {
				yield(t, s);
			}
			break;
		case IStrategoTerm.STRING:
			s.append(((IStrategoString) data).stringValue());
		}
	}
}
