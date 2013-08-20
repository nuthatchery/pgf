package org.nuthatchery.pgf.examples;

import static org.nuthatchery.pgf.rules.PatternFactory.*;

import org.nuthatchery.pgf.rules.RuleProcessor;
import org.nuthatchery.pgf.tokens.Category;
import org.nuthatchery.pgf.tokens.CategoryStore;

public class Respacer extends RuleProcessor {
	public Respacer(CategoryStore store) {
		super(store);
		Category START = store.category("START");
		Category END = store.category("END");
		Category WS = store.category("WS");
		Category NL = store.category("NL");
		Category SPC = store.category("SPC");
		Category TOKEN = store.category("TOKEN");
		Category TXT = store.category("TXT");

		this.addRule(at(SPC), drop);
		this.addPriorityLevel();
		this.addRule(after(START), nop);
		this.addRule(after(NL), nop);
		this.addRule(at(END), nop);
		this.addRule(at(TOKEN), insert(" ", WS));
		this.addRule(after(TXT).at(TOKEN), insert(" ", WS));
	}
}
