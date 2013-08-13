package org.nuthatchery.pgf.javaformatter;

import static nuthatch.library.JoinPoints.down;
import static nuthatch.library.JoinPoints.up;
import static nuthatch.pattern.StaticPatternFactory.or;
import static nuthatch.stratego.pattern.SPatternFactory._;
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
import nuthatch.library.Action;
import nuthatch.library.BaseWalk;
import nuthatch.pattern.Environment;
import nuthatch.pattern.EnvironmentFactory;
import nuthatch.stratego.actions.SAction;
import nuthatch.stratego.actions.SActionFactory;
import nuthatch.stratego.actions.SMatchAction;
import nuthatch.stratego.adapter.STermCursor;
import nuthatch.stratego.adapter.STermVar;
import nuthatch.stratego.adapter.SWalker;

import org.nuthatchery.pgf.plumbing.ForwardStream;
import org.nuthatchery.pgf.rascal.uptr.TokenizerConfig;
import org.nuthatchery.pgf.tokens.CategoryStore;
import org.nuthatchery.pgf.tokens.CtrlToken;
import org.nuthatchery.pgf.tokens.DataToken;
import org.nuthatchery.pgf.tokens.Token;
import org.nuthatchery.pgf.tokens.TokenizerHelper;
import org.spoofax.interpreter.terms.IStrategoInt;
import org.spoofax.interpreter.terms.IStrategoString;

public class AsFix2Tokenizer {
	protected final CategoryStore categories;
	protected final TokenizerConfig config;


	public AsFix2Tokenizer(TokenizerConfig config) {
		this.config = config;
		this.categories = null; // config.cfgCategories();
	}


	public void tokenize(STermCursor parseTree, final ForwardStream<Token> output) {
		BaseWalk<SWalker> toTerm = new BaseWalk<SWalker>() {
			@Override
			public int step(SWalker w) {
				Environment<STermCursor> env = EnvironmentFactory.env();
				STermVar prod = new STermVar(env);
				STermVar args = new STermVar(env);

				if(w.match(appl(prod, args), env)) {
					String sort = getProdSort(prod.get());
					if(down(w)) {
						if(isLex(prod.get(), env)) {
							String string = yield(args.get());
							//System.err.print("\"" + string + "\":" + sort + " ");
							output.put(new DataToken(string, config.getCatForLexical(string)));
							return PARENT;
						}
						else if(isLit(prod.get(), env)) {
							String string = yield(args.get());
							//System.err.print("\"" + string + "\":" + sort + " ");
							//System.out.print(string);
							output.put(new DataToken(string, config.getCatForLiteral(string)));
							return PARENT;
						}
						else if(isLayout(prod.get(), env)) {
							String string = yield(args.get());
							//System.err.print("\"" + string + "\":" + sort + " ");
							//System.out.print(string);
							TokenizerHelper.splitComment(string, output, config);
							return PARENT;
						}

						if(config.cfgNestSorts().contains(sort)) {
							output.put(new CtrlToken(config.cfgCatForCtrlBegin()));
						}

						return 2;
					}
					else if(up(w)) {
						if(config.cfgNestSorts().contains(sort)) {
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


	public static String getSymSort(STermCursor symbol) {
		Environment<STermCursor> env = EnvironmentFactory.env();
		STermVar x = new STermVar(env);
		if(or(sort(x), parametrized_sort(x, _)).match(symbol, env) && x.get().getData() instanceof IStrategoString) {
			return ((IStrategoString) x.get().getData()).stringValue();
		}
		else if(or(cf(x), lex(x), opt(x)).match(symbol, env)) {
			return getSymSort(x.get());
		}
		else if(or(iter(x), iter_star(x), iter_sep(x, _), iter_star_sep(x, _)).match(symbol, env)) {
			return getSymSort(x.get()) + "*";
		}
		return "";
	}


	public static boolean isLex(STermCursor tree, Environment<STermCursor> env) {
		// System.out.println(tree.treeToString());
		if(lex(_).match(getProdType(tree), env)) {
			return true;
		}
		if(list(lex(_)).match(getProdDefined(tree), env)) {
			System.err.println("hmm");
		}
		return false;

	}


	public static boolean isLit(STermCursor tree, Environment<STermCursor> env) {
		// System.out.println(tree.treeToString());
		return lit(_).match(getProdType(tree), env);
	}


	public static boolean isLayout(STermCursor tree, Environment<STermCursor> env) {
		// System.out.println(tree.treeToString());
		return cf(layout()).match(getProdType(tree), env);
	}


	public static boolean isSort(STermCursor tree, Environment<STermCursor> env) {
		// System.out.println(tree.treeToString());
		return cf(or(sort(_), parametrized_sort(_, _))).match(getProdType(tree), env);
	}


	public static STermCursor getProdType(STermCursor tree) {
		if(tree.hasName("prod")) {
			return tree.getBranch(2);
		}
		else {
			throw new IllegalArgumentException("Not a production");
		}
	}


	public static STermCursor getProdDefined(STermCursor tree) {
		if(tree.hasName("prod")) {
			return tree.getBranch(1);
		}
		else {
			throw new IllegalArgumentException("Not a production");
		}
	}


	@SuppressWarnings("unchecked")
	String yield(STermCursor tree) {
		final StringBuilder s = new StringBuilder();
		Action<SWalker> action = SActionFactory.combine(SActionFactory.atLeaf(new SAction() {
			@Override
			public int step(SWalker walker) {
				if(walker.getData() instanceof IStrategoInt) {
					s.append(Character.toChars(((IStrategoInt) walker.getData()).intValue()));
				}
				return NEXT;
			}
		}), SActionFactory.down(SActionFactory.match(appl(_, _), new SMatchAction() {
			@Override
			public int step(SWalker walker, Environment<STermCursor> env) {
				return 2;
			}

		})));

		new SWalker(tree, SActionFactory.walk(action)).start();

		return s.toString();
	}
}
