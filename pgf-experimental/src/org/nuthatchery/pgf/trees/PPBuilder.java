package org.nuthatchery.pgf.trees;


import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;

import org.nuthatchery.pgf.config.TokenizerConfig;
import org.nuthatchery.pgf.plumbing.ForwardStream;
import org.nuthatchery.pgf.plumbing.impl.BufferedSyncPipeComponent;
import org.nuthatchery.pgf.tokens.Category;
import org.nuthatchery.pgf.tokens.DataToken;
import org.nuthatchery.pgf.tokens.Token;
import org.nuthatchery.pgf.tokens.TokensToString;
import org.nuthatchery.pgf.tokens.TokensToWriter;

import nuthatch.library.Action;
import nuthatch.library.ActionBuilder;
import nuthatch.library.ActionFactory;
import nuthatch.library.BaseAction;
import nuthatch.library.MatchAction;
import nuthatch.library.MatchBuilder;
import nuthatch.library.Walk;
import nuthatch.pattern.BuildContext;
import nuthatch.pattern.Pattern;
import nuthatch.tree.TreeCursor;
import nuthatch.tree.TreeHandle;
import nuthatch.walker.Walker;
import nuthatch.walker.impl.SimpleWalker;


/**
 * Build a {@link org.nuthatchery.prg.trees.PrettyPrinter}, which traverses a
 * tree and outputs tokens to a stream.
 * 
 * @param <Value>
 * @param <Type>
 * @param <C>
 * @param <W>
 */
public class PPBuilder<Value, Type, C extends TreeCursor<Value, Type>, W extends Walker<Value, Type>> {
	@SuppressWarnings("unchecked")
	protected final ActionFactory<Value, Type, C, W> af = (ActionFactory<Value, Type, C, W>) ActionFactory.actionFactory;
	protected final TokenizerConfig config;

	protected final List<Rule<Value, Type>> rules = new ArrayList<>();
	protected final BuildContext<Value, Type, C> context;


	/**
	 * Construct a builder.
	 * 
	 * Rules are added by one of {@link #addList(Pattern, Object...)},
	 * {@link #addTmpl(Pattern, String)} (or the typed counterparts), and the
	 * pretty printer can be obtained by calling {@link #compile()}.
	 * 
	 * @param config
	 *            A configuration detailing how strings are categorised as
	 *            tokens
	 * @param context
	 *            The build context of the tree type
	 */
	@SuppressWarnings("unchecked")
	public PPBuilder(TokenizerConfig config, BuildContext<Value, Type, C> context) {
		ActionFactory<Value, Type, C, W> af = (ActionFactory<Value, Type, C, W>) ActionFactory.actionFactory;
		this.context = context;
		this.config = config;
	}


	/**
	 * Add a pretty-printing rule.
	 * 
	 * @param pat
	 *            A pattern
	 * @param ppRules
	 *            A list of tokes to be output for the pattern
	 * @return This builder, for method chaining
	 */
	public PPBuilder<Value, Type, C, W> addList(Pattern<Value, Type> pat, Object... ppRules) {
		return addTypedList(pat, null, ppRules);
	}


	/**
	 * Add a pretty-printing rule.
	 * 
	 * Format of the template:
	 * <ul>
	 * <li>Each group of non-spaces becomes a literal token</li>
	 * <li>All tokens must be separated by a space</li>
	 * <li>For multiple spaces, all spaces except the first in the sequence
	 * becomes a space token</li>
	 * <li>Special tokens:
	 * <ul>
	 * <li>&lt;<i>N</i>&gt; (or &lt;<i>N0.N1.N2...</i>&gt;) identifies a child
	 * (path) to be inserted at that point (<i>N...</i> are branch numbers where
	 * the first child is 1)</li>
	 * <li>&lt;commaSep(<i>N</i>)&gt; identifies a child (or dot-separated path)
	 * to be inserted as a list separated by commas</li>
	 * <li>&lt;sepBy(<i>N</i>,'<i>c</i>')&gt; identifies a child (or
	 * dot-separated path) to be inserted as a list separated by '<i>c</i>'s</li>
	 * <li>&lt;lit(<i>TOK</i>)&gt; explicitly classifies <i>TOK</i> as a literal
	 * token (the default)</li>
	 * <li>&lt;lex(<i>TOK</i>)&gt; explicitly classifies <i>TOK</i> as a
	 * non-literal token (e.g., an identifier)</li>
	 * <li>&lt;spc(<i>TOK</i>)&gt; explicitly classifies <i>TOK</i> as a space
	 * token</li>
	 * <li>&lt;str(<i>N</i>,<i>CAT</i>)&gt; prints child (or descendant)
	 * <i>N</i> as a token classified as <i>CAT</i> token</li>
	 * </ul>
	 * </ul>
	 * 
	 * @param pat
	 *            A pattern
	 * @param template
	 *            A string of tokes to be output for the pattern
	 * @return This builder, for method chaining
	 */
	public PPBuilder<Value, Type, C, W> addTmpl(Pattern<Value, Type> pat, String template) {
		return addTypedTmpl(pat, null, template);
	}


	/**
	 * Add a pretty-printing rule.
	 * 
	 * @param pat
	 *            A pattern
	 * @param ppRules
	 *            A list of tokes to be output for the pattern
	 * @return This builder, for method chaining
	 */
	public PPBuilder<Value, Type, C, W> addTypedList(Pattern<Value, Type> pat, String sort, Object... ppRules) {
		List<PPAtom> rule = new ArrayList<>();

		for(Object o : ppRules) {
			if(o instanceof Integer) {
				rule.add(new PPChild(new int[] { (int) o }));
			}
			else if(o instanceof PPAtom) {
				rule.add((PPAtom) o);
			}
			else if(o instanceof String) {
				String s = (String) o;
				Category cat = config.getCatForLiteral(s);
				rule.add(new PPToken(new DataToken(s, cat)));
			}
			else if(o instanceof PPSpace) {
				String s = ((PPSpace) o).s;
				rule.add(new PPToken(new DataToken(s, config.cfgCatHorizSpace())));
			}
			else if(o instanceof PPLiteral) {
				String s = ((PPSpace) o).s;
				rule.add(new PPToken(new DataToken(s, config.getCatForLiteral(s))));
			}
			else if(o instanceof PPLexical) {
				String s = ((PPSpace) o).s;
				rule.add(new PPToken(new DataToken(s, config.getCatForLexical(s))));
			}
			else if(o instanceof Token) {
				rule.add(new PPToken((Token) o));
			}
			else {
				throw new IllegalArgumentException("Don't know what to do with rule object " + o);
			}

		}
		rules.add(new Rule<>(pat, rule));

		return this;
	}


	/**
	 * Add a pretty-printing rule.
	 * 
	 * Format of the template:
	 * <ul>
	 * <li>Each group of non-spaces becomes a literal token</li>
	 * <li>All tokens must be separated by a space</li>
	 * <li>For multiple spaces, all spaces except the first in the sequence
	 * becomes a space token</li>
	 * <li>Special tokens:
	 * <ul>
	 * <li>&lt;<i>N</i>&gt; (or &lt;<i>N0.N1.N2...</i>&gt;) identifies a child
	 * (path) to be inserted at that point (<i>N...</i> are branch numbers where
	 * the first child is 1)</li>
	 * <li>&lt;commaSep(<i>N</i>)&gt; identifies a child (or dot-separated path)
	 * to be inserted as a list separated by commas</li>
	 * <li>&lt;sepBy(<i>N</i>,'<i>c</i>')&gt; identifies a child (or
	 * dot-separated path) to be inserted as a list separated by '<i>c</i>'s</li>
	 * <li>&lt;lit(<i>TOK</i>)&gt; explicitly classifies <i>TOK</i> as a literal
	 * token (the default)</li>
	 * <li>&lt;lex(<i>TOK</i>)&gt; explicitly classifies <i>TOK</i> as a
	 * non-literal token (e.g., an identifier)</li>
	 * <li>&lt;spc(<i>TOK</i>)&gt; explicitly classifies <i>TOK</i> as a space
	 * token</li>
	 * <li>&lt;str(<i>N</i>,<i>CAT</i>)&gt; prints child (or descendant)
	 * <i>N</i> as a token classified as <i>CAT</i> token</li>
	 * </ul>
	 * </ul>
	 * 
	 * @param pat
	 *            A pattern
	 * @param sort
	 *            A string identifying the grammar non-terminal the pattern
	 *            corresponds to
	 * @param template
	 *            A string of tokes to be output for the pattern
	 * @return This builder, for method chaining
	 */
	public PPBuilder<Value, Type, C, W> addTypedTmpl(Pattern<Value, Type> pat, String sort, String template) {
		List<PPAtom> decoded = decodeTemplate(template);

		rules.add(new Rule<>(pat, decoded));
		return null;
	}


	/**
	 * Compiles the currently added rules into a pretty printer.
	 * 
	 * @return A pretty printer
	 */
	public PrettyPrinter<Value, Type, C, W> compile() {
		PP<Value, Type, C, W> printer = new PP<>();
		MatchBuilder<Value, Type, C, W> builder = af.matchBuilder(context);
		for(Rule<Value, Type> r : rules) {
			builder.add(r.pat, compileRule(r.pat, r.tokens, printer));
		}
		Action<W> action = builder.first();
		printer.setWalk(action, af.walk(action));
		return printer;
	}


	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj == null) {
			return false;
		}
		if(getClass() != obj.getClass()) {
			return false;
		}
		PPBuilder<?, ?, ?, ?> other = (PPBuilder<?, ?, ?, ?>) obj;
		if(rules == null) {
			if(other.rules != null) {
				return false;
			}
		}
		else if(!rules.equals(other.rules)) {
			return false;
		}

		return true;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((rules == null) ? 0 : rules.hashCode());
		return result;
	}


	@Override
	public String toString() {
		return "PPBuilder [rules=" + rules + "]";
	}


	@SuppressWarnings("unchecked")
	private MatchAction<Value, Type, C, W> compileRule(Pattern<Value, Type> pat, List<PPAtom> rule, PP<Value, Type, C, W> printer) {
		int from = 0;
		List<PPAtom> tmp = new ArrayList<>();
		ActionBuilder<W> cb = af.combineBuilder();
		for(int i = 0; i < rule.size(); i++) {
			PPAtom atom = rule.get(i);

			if(atom instanceof PPToken) {
				tmp.add(atom);
			}
			else if(atom instanceof PPChild) {
				PPChild child = (PPChild) atom;
				if(child.path.length == 1) {
					cb.add(af.from(from, af.seq(printer.action(tmp), af.go(child.path[0]))));
					tmp = new ArrayList<>();
					from = child.path[0];
				}
				else {
					tmp.add(atom);
				}
			}
			else if(atom instanceof PPSepBy) {
				tmp.add(atom);
			}
			else if(atom instanceof PPCmdChild) {
				tmp.add(atom);
			}

		}
		if(!tmp.isEmpty()) {
			cb.add(af.from(from, af.seq(printer.action(tmp), af.go(0))));
			tmp = new ArrayList<>();
		}
		else {
			cb.add(af.from(from, af.go(0)));
		}
		Action<W> action = cb.done();
		System.out.println("Pattern:     " + pat);
		System.out.println("  => Rule:   " + rule);
		System.out.println("  => Action: " + action);
		return af.action(action);

	}


	private int[] decodePath(String s) {
		String[] ss = s.split("\\.");
		int[] path = new int[ss.length];
		for(int i = 0; i < ss.length; i++) {
			path[i] = Integer.parseInt(ss[i]);
		}
		return path;
	}


	private List<PPAtom> decodeTemplate(String template) {
		List<PPAtom> rules = new ArrayList<>();
		int len = template.length();
		int pos = 0;
		Matcher childMatcher = java.util.regex.Pattern.compile("^[ ]?<(\\d+(\\.\\d+)*)>").matcher(template);
		Matcher sepMatcher = java.util.regex.Pattern.compile("^[ ]?<sepBy\\((\\d+(\\.\\d+)*),\\s*'([^\"]*)'\\)>").matcher(template);
		Matcher commaSepMatcher = java.util.regex.Pattern.compile("^[ ]?<commaSep\\((\\d+(\\.\\d+)*)\\)>").matcher(template);
		Matcher tokenMatcher = java.util.regex.Pattern.compile("^[ ]?(\\S+)").matcher(template);
		Matcher strChildMatcher = java.util.regex.Pattern.compile("^[ ]?<str\\((\\d+(\\.\\d+)*),(\\w+)\\)>").matcher(template);
		Matcher cmdStringMatcher = java.util.regex.Pattern.compile("^[ ]?<(lit|lex|spc)\\(([^)]*)\\)>").matcher(template);
		Matcher spaceMatcher = java.util.regex.Pattern.compile("^(\\s+)([ ]|$)").matcher(template);

		while(pos < len) {
			if(childMatcher.region(pos, len).lookingAt()) {
				rules.add(new PPChild(decodePath(childMatcher.group(1))));
				pos = childMatcher.end();
			}
			else if(sepMatcher.region(pos, len).lookingAt()) {
				rules.add(new PPSepBy(decodePath(sepMatcher.group(1)), sepMatcher.group(3)));
				pos = sepMatcher.end();
			}
			else if(commaSepMatcher.region(pos, len).lookingAt()) {
				rules.add(new PPSepBy(decodePath(commaSepMatcher.group(1)), ","));
				pos = commaSepMatcher.end();
			}
			else if(strChildMatcher.region(pos, len).lookingAt()) {
				String cat = strChildMatcher.group(3);
				rules.add(new PPCmdChild(decodePath(strChildMatcher.group(1)), config.cfgCategories().category(cat)));
				pos = strChildMatcher.end();
			}
			else if(cmdStringMatcher.region(pos, len).lookingAt()) {
				String cmd = cmdStringMatcher.group(1);
				String s = cmdStringMatcher.group(2);
				Category cat = config.cfgCatText();
				switch(cmd) {
				case "lex":
					cat = config.getCatForLexical(s);
					break;
				case "lit":
					cat = config.getCatForLiteral(s);
					break;
				case "spc":
					cat = config.cfgCatHorizSpace();
					break;
				}
				rules.add(new PPToken(new DataToken(s, cat)));
				pos = cmdStringMatcher.end();
			}
			else if(spaceMatcher.region(pos, len).lookingAt()) {
				String s = spaceMatcher.group(1);
				rules.add(new PPToken(new DataToken(s, config.cfgCatHorizSpace())));
				pos = spaceMatcher.end();
			}
			else if(tokenMatcher.region(pos, len).lookingAt()) {
				String s = tokenMatcher.group(1);
				rules.add(new PPToken(new DataToken(s, config.getCatForLiteral(s))));
				pos = tokenMatcher.end();
			}
			else {
				throw new IllegalArgumentException("Unable to decode template: '" + template + "'");
			}
		}

		return rules;
	}


	/**
	 * @param s
	 * @return A lexical token template, for use with
	 *         {@link #addList(Pattern, Object...)} and
	 *         {@link #addTypedList(Pattern, String, Object...)}
	 */
	public static PPLexical lex(String s) {
		return new PPLexical(s);
	}


	/**
	 * @param s
	 * @return A literal token template, for use with
	 *         {@link #addList(Pattern, Object...)} and
	 *         {@link #addTypedList(Pattern, String, Object...)}
	 */
	public static PPLiteral lit(String s) {
		return new PPLiteral(s);
	}


	/**
	 * @param s
	 * @return A separate-by token template, for use with
	 *         {@link #addList(Pattern, Object...)} and
	 *         {@link #addTypedList(Pattern, String, Object...)}
	 */
	public static PPAtom sepBy(int child, String sep) {
		return new PPSepBy(new int[] { child }, sep);
	}


	/**
	 * @return A space token template, for use with
	 *         {@link #addList(Pattern, Object...)} and
	 *         {@link #addTypedList(Pattern, String, Object...)}
	 */
	public static PPSpace space() {
		return new PPSpace(" ");
	}


	/**
	 * @param s
	 * @return A space token template, for use with
	 *         {@link #addList(Pattern, Object...)} and
	 *         {@link #addTypedList(Pattern, String, Object...)}
	 */
	public static PPSpace space(String s) {
		return new PPSpace(s);
	}


	private static class PPLexical {
		String s;


		public PPLexical(String s) {
			this.s = s;
		}
	}


	private static class PPLiteral {
		String s;


		public PPLiteral(String s) {
			this.s = s;
		}
	}


	private static class PPSpace {
		String s;


		public PPSpace(String s) {
			this.s = s;
		}
	}


	private static class Rule<Value, Type> {
		Pattern<Value, Type> pat;
		List<PPAtom> tokens;


		public Rule(Pattern<Value, Type> pat, List<PPAtom> tokens) {
			this.pat = pat;
			this.tokens = tokens;
		}


		@Override
		public boolean equals(Object obj) {
			if(this == obj) {
				return true;
			}
			if(obj == null) {
				return false;
			}
			if(getClass() != obj.getClass()) {
				return false;
			}
			Rule<?, ?> other = (Rule<?, ?>) obj;
			if(pat == null) {
				if(other.pat != null) {
					return false;
				}
			}
			else if(!pat.equals(other.pat)) {
				return false;
			}
			if(tokens == null) {
				if(other.tokens != null) {
					return false;
				}
			}
			else if(!tokens.equals(other.tokens)) {
				return false;
			}
			return true;
		}


		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((pat == null) ? 0 : pat.hashCode());
			result = prime * result + ((tokens == null) ? 0 : tokens.hashCode());
			return result;
		}


		@Override
		public String toString() {
			return "Rule [pat=" + pat + ", tokens=" + tokens + "]";
		}

	}


	/**
	 * Pretty printer implementation
	 * 
	 * @param <Value>
	 * @param <Type>
	 * @param <C>
	 * @param <W>
	 */
	static class PP<Value, Type, C extends TreeCursor<Value, Type>, W extends Walker<Value, Type>> implements PrettyPrinter<Value, Type, C, W> {
		protected ForwardStream<Token> stream;
		protected Action<W> step;
		protected Walk<W> walk;


		@Override
		synchronized public void connect(ForwardStream<Token> next) {
			stream = next;
		}


		@Override
		synchronized public void print(C tree) {
			SimpleWalker<Value, Type> walker = new SimpleWalker<Value, Type>(tree, (Walk<SimpleWalker<Value, Type>>) walk);
			walker.start();
			stream.end();
		}


		@Override
		synchronized public void print(C tree, ForwardStream<Token> output) {
			connect(output);
			print(tree);
			connect(null);
		}


		@Override
		synchronized public void print(C tree, PrintStream output) {
			connect(new BufferedSyncPipeComponent<>(new TokensToWriter(output)));
			print(tree);
			connect(null);
		}


		@Override
		synchronized public String toString(C tree) {
			TokensToString tokensToString = new TokensToString();
			connect(new BufferedSyncPipeComponent<>(tokensToString));
			print(tree);
			connect(null);
			return tokensToString.toString();
		}


		PPAction action(List<PPAtom> toks) {
			return new PPAction(toks);
		}


		void setWalk(Action<W> s, Walk<W> w) {
			step = s;
			walk = w;
		}


		class PPAction extends BaseAction<W> {
			private List<PPAtom> toks;


			public PPAction(List<PPAtom> toks) {
				this.toks = toks;
			}


			@Override
			public int step(W walker) {
				for(PPAtom atom : toks) {
					if(atom instanceof PPBuilder.PPToken) {
						stream.put(((PPToken) atom).token);
					}
					else if(atom instanceof PPCmdChild) {
						PPCmdChild cmdChild = (PPCmdChild) atom;
						if(cmdChild.path.length == 1 && cmdChild.path[0] == 0) {
							String s = walker.treeToString();
							stream.put(new DataToken(s, cmdChild.cat));

						}
						else if(cmdChild.path.length == 1) {
							TreeHandle<Value, Type> handle = walker.getBranchHandle(cmdChild.path[0]);
							String s = handle.treeToString();
							stream.put(new DataToken(s, cmdChild.cat));
						}
						else {
							throw new UnsupportedOperationException();
						}
					}
					else if(atom instanceof PPChild) {
						throw new UnsupportedOperationException();
					}
					else if(atom instanceof PPSepBy) {
						throw new UnsupportedOperationException();
					}
					else {
						throw new IllegalArgumentException();
					}
				}
				return PROCEED;
			}


			@Override
			public String toString() {
				return "put(" + toks + ")";
			}

		}

	}


	static interface PPAtom {

	}


	static class PPChild implements PPAtom {
		int[] path;


		PPChild(int[] path) {
			this.path = path;
		}


		@Override
		public boolean equals(Object obj) {
			if(this == obj) {
				return true;
			}
			if(obj == null) {
				return false;
			}
			if(getClass() != obj.getClass()) {
				return false;
			}
			PPChild other = (PPChild) obj;
			if(!Arrays.equals(path, other.path)) {
				return false;
			}
			return true;
		}


		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + Arrays.hashCode(path);
			return result;
		}


		@Override
		public String toString() {
			return "PPChild [path=" + Arrays.toString(path) + "]";
		}
	}


	static class PPCmdChild implements PPAtom {
		int[] path;
		Category cat;


		PPCmdChild(int[] path, Category cat) {
			this.path = path;
			this.cat = cat;
		}


		@Override
		public boolean equals(Object obj) {
			if(this == obj) {
				return true;
			}
			if(obj == null) {
				return false;
			}
			if(getClass() != obj.getClass()) {
				return false;
			}
			PPCmdChild other = (PPCmdChild) obj;
			if(cat == null) {
				if(other.cat != null) {
					return false;
				}
			}
			else if(!cat.equals(other.cat)) {
				return false;
			}
			if(!Arrays.equals(path, other.path)) {
				return false;
			}
			return true;
		}


		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((cat == null) ? 0 : cat.hashCode());
			result = prime * result + Arrays.hashCode(path);
			return result;
		}


		@Override
		public String toString() {
			return "PPCmdChild [path=" + Arrays.toString(path) + ", cat=" + cat + "]";
		}

	}


	static class PPSepBy implements PPAtom {
		int[] path;
		String sep;


		PPSepBy(int[] path, String sep) {
			this.path = path;
			this.sep = sep;
		}


		@Override
		public boolean equals(Object obj) {
			if(this == obj) {
				return true;
			}
			if(obj == null) {
				return false;
			}
			if(getClass() != obj.getClass()) {
				return false;
			}
			PPSepBy other = (PPSepBy) obj;
			if(!Arrays.equals(path, other.path)) {
				return false;
			}
			if(sep == null) {
				if(other.sep != null) {
					return false;
				}
			}
			else if(!sep.equals(other.sep)) {
				return false;
			}
			return true;
		}


		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + Arrays.hashCode(path);
			result = prime * result + ((sep == null) ? 0 : sep.hashCode());
			return result;
		}


		@Override
		public String toString() {
			return "PPSepBy [path=" + Arrays.toString(path) + ", sep=" + sep + "]";
		}

	}


	static class PPToken implements PPAtom {
		Token token;


		PPToken(Token t) {
			token = t;
		}


		@Override
		public boolean equals(Object obj) {
			if(this == obj) {
				return true;
			}
			if(obj == null) {
				return false;
			}
			if(getClass() != obj.getClass()) {
				return false;
			}
			PPToken other = (PPToken) obj;
			if(token == null) {
				if(other.token != null) {
					return false;
				}
			}
			else if(!token.equals(other.token)) {
				return false;
			}
			return true;
		}


		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((token == null) ? 0 : token.hashCode());
			return result;
		}


		@Override
		public String toString() {
			return token.toString();
		}
	}

}
