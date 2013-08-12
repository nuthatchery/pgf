package org.nuthatchery.pgf.rules;

import org.nuthatchery.pgf.plumbing.PipeConnector;
import org.nuthatchery.pgf.processors.Processor;
import org.nuthatchery.pgf.rules.actions.Action;
import org.nuthatchery.pgf.rules.pattern.Pattern;
import org.nuthatchery.pgf.tokens.Category;
import org.nuthatchery.pgf.tokens.CategoryStore;
import org.nuthatchery.pgf.tokens.CtrlToken;
import org.nuthatchery.pgf.tokens.Token;

public class RuleProcessor implements Processor<Token, Token> {
	private int lookBehind = 0;
	private int lookAhead = 0;
	private CategoryStore store;
	private Decision[] table;
	private int pri = 0;


	public RuleProcessor(CategoryStore store) {
		this.store = store;
		this.table = new Decision[store.numCats() * store.numCats()];
	}


	public void addPriorityLevel() {
		pri = pri + 2 * store.numCats();
	}


	public void addRule(Pattern pat, Action act) {
		if(pat.numLookAhead() > 1) {
			throw new IllegalArgumentException("Only one token lookahead is supported (" + pat.numLookAhead() + " requested)");
		}
		if(pat.numLookBehind() > 1) {
			throw new IllegalArgumentException("Only one token lookbehind is supported (" + pat.numLookBehind() + " requested)");
		}

		lookAhead = Math.max(pat.numLookAhead(), lookAhead);
		lookBehind = Math.max(pat.numLookBehind(), lookBehind);

		Category aheadCat = store.category("TOKEN");
		Category behindCat = store.category("TOKEN");

		if(pat.numLookAhead() > 0) {
			aheadCat = pat.getLookAhead()[0];
		}

		if(pat.numLookBehind() > 0) {
			behindCat = pat.getLookBehind()[0];
		}

		if(!act.isReader()) {
			act = PatternFactory.seq(act, PatternFactory.move);
		}

		addRule(aheadCat, behindCat, act);
	}


	@Override
	public Token cfgEndMarker() {
		return null;
	}


	@Override
	public boolean cfgIsSink() {
		return false;
	}


	@Override
	public int cfgMinLookAhead() {
		return 1;
	}


	@Override
	public int cfgMinLookBehind() {
		return 1;
	}


	@Override
	public boolean cfgUseEndMarker() {
		return false;
	}


	@Override
	public boolean process(PipeConnector<Token, Token> io) {
		Token thisToken = io.lookAhead(0);
		while(thisToken instanceof CtrlToken) {
			io.get();
			io.putNoHistory(thisToken);

			if(thisToken.hasSubCatOf(store.category("BEGIN"))) {
				nestBegin(thisToken.getCategory());
			}
			else if(thisToken.hasSubCatOf(store.category("END"))) {
				nestEnd(thisToken.getCategory());
			}

			if(io.isEmpty()) {
				return true;
			}
			thisToken = io.lookAhead(0);
		}
		Category ahead = thisToken.getCategory();
		Category behind;
		if(io.sizeBehind() > 0) {
			behind = io.lookBehind(0).getCategory();
		}
		else {
			behind = store.category("START");
		}

		Decision decision = get(ahead, behind);
		if(decision == null) {
			// default;
			PatternFactory.move.perform(io);
		}
		else {
			decision.action.perform(io);
		}

		assert io.sizeAhead() == 0 || io.lookAhead(0) != thisToken;

		return true;
	}


	@Override
	public void restart() {

	}


	private void addRule(Category aheadCat, Category behindCat, Action act) {
		addRule(aheadCat, behindCat, aheadCat, behindCat, act, pri);

		for(Category c2 : store.subCategoriesOf(behindCat)) {
			addRule(aheadCat, behindCat, aheadCat, c2, act, pri + store.superDist(behindCat, c2));
		}

		for(Category c1 : store.subCategoriesOf(aheadCat)) {
			addRule(aheadCat, behindCat, c1, behindCat, act, pri + store.superDist(aheadCat, c1));
			for(Category c2 : store.subCategoriesOf(behindCat)) {
				addRule(aheadCat, behindCat, c1, c2, act, pri + store.superDist(aheadCat, c1) + store.superDist(behindCat, c2));
			}
		}
	}


	private void addRule(Category declAheadCat, Category declBehindCat, Category actualAheadCat, Category actualBehindCat, Action act, int i) {
		Decision d = get(actualAheadCat, actualBehindCat);
		if(d == null) {
			d = new Decision(declAheadCat, declBehindCat, i, act);
			if(i == pri) {
				System.err.println("Adding     " + d);
			}
			put(actualAheadCat, actualBehindCat, d);
		}
		else if(d.priority > i) {
			if(i == pri) {
				System.err.println("Overriding " + d);
			}
			d = new Decision(declAheadCat, declBehindCat, i, act);
			if(i == pri) {
				System.err.println("      with " + d);
			}
			put(actualAheadCat, actualBehindCat, d);
		}
		else if(d.priority == i && !d.action.toString().equals(act.toString())) {
			System.err.println("***\nRULE CONFLICT for " + actualBehindCat + " @ " + actualAheadCat + ", pri " + i);
			System.err.println("   Between " + d);
			System.err.println("       and " + new Decision(declAheadCat, declBehindCat, i, act));
			System.err.println("***");
		}
	}


	protected Decision get(Category a, Category b) {
		return table[a.getId() * store.numCats() + b.getId()];
	}


	/**
	 * Called when a BEGIN control token is encountered.
	 * 
	 * Override this if you need to track nesting.
	 * 
	 * @param category
	 *            The particular category of the token
	 */
	protected void nestBegin(Category category) {
	}


	/**
	 * Called when an END control token is encountered.
	 * 
	 * Override this if you need to track nesting.
	 * 
	 * @param category
	 *            The particular category of the token
	 */
	protected void nestEnd(Category category) {
	}


	protected void put(Category a, Category b, Decision val) {
		table[a.getId() * store.numCats() + b.getId()] = val;
	}


	static class Decision {
		final Category ahead;

		final Category behind;
		final int priority;
		final Action action;


		public Decision(Category ahead, Category behind, int priority, Action action) {
			super();
			this.ahead = ahead;
			this.behind = behind;
			this.priority = priority;
			this.action = action;
		}


		@Override
		public String toString() {
			return "rule " + behind + " @ " + ahead + " => " + action;
		}
	}
}
