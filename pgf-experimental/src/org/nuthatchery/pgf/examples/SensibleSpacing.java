package org.nuthatchery.pgf.examples;

import org.nuthatchery.pgf.rules.RuleProcessor;
import org.nuthatchery.pgf.rules.actions.Action;
import org.nuthatchery.pgf.tokens.Category;
import org.nuthatchery.pgf.tokens.CategoryStore;
import static org.nuthatchery.pgf.rules.PatternFactory.*;

public class SensibleSpacing extends RuleProcessor {

	@SuppressWarnings("unused")
	public SensibleSpacing(CategoryStore store) {
		super(store);
		boolean spaceBeforeCond = true;
		final Category TOKEN = store.category("TOKEN");
		final Category TXT = store.category("TXT");
		final Category SPC = store.category("SPC");
		final Category CTRL = store.category("CTRL");
		final Category START = store.category("START");
		final Category END = store.category("END");
		final Category WS = store.category("WS");
		final Category NL = store.category("NL");
		final Category COM = store.category("COM");
		final Category ID = store.category("ID");
		final Category LITERAL = store.category("LITERAL");
		final Category KEYWORD = store.category("KEYWORD");
		final Category OP = store.category("OP");
		final Category PREOP = store.category("PREOP");
		final Category BINOP = store.category("BINOP");
		final Category POSTOP = store.category("POSTOP");
		final Category GRP = store.category("GRP");
		final Category PAR = store.category("PAR");
		final Category BRC = store.category("BRC");
		final Category BRT = store.category("BRT");
		final Category LGRP = store.category("LGRP");
		final Category RGRP = store.category("RGRP");
		final Category LPAR = store.category("LPAR");
		final Category RPAR = store.category("RPAR");
		final Category LBRC = store.category("LBRC");
		final Category RBRC = store.category("RBRC");
		final Category LBRT = store.category("LBRT");
		final Category RBRT = store.category("RBRT");
		final Category PUNCT = store.category("PUNCT");
		final Category COMMA = store.category("COMMA");
		final Category SEMI = store.category("SEMI");
		final Category COLON = store.category("COLON");
		final Category DOT = store.category("DOT");
		Action space = insert(" ", WS);

		// spaces
		addRule(after(START).at(WS), nop);
		addRule(after(NL).at(WS), nop);
		addRule(at(WS), drop);

		addPriorityLevel();

		addRule(after(PREOP).at(TXT), nop);
		addRule(after(PREOP).at(OP), space);
		addRule(after(TXT).at(BINOP), space);
		addRule(after(BINOP).at(TXT), space);
		addRule(after(OP).at(POSTOP), space);
		addRule(after(TXT).at(POSTOP), nop);

		addPriorityLevel();

		addRule(after(COMMA).at(TXT), space);
		addRule(after(PUNCT), nop);
		addRule(at(PUNCT), nop);

		// no spaces around grouping tokens
		addRule(after(GRP), nop);
		addRule(at(GRP), nop);
		if(spaceBeforeCond) {
			addRule(after(KEYWORD).at(LGRP), space);
		}

		addRule(after(COLON).at(TXT), space);
		addRule(after(TXT).at(COLON), space);

		// Space around inline braces
		addRule(after(TXT).at(BRC), space);
		addRule(after(BRC).at(TXT), space);
		addRule(after(BRC).at(PUNCT), nop);
		addRule(after(ID).at(LBRC), nop);

		addRule(after(SEMI).at(TXT), space);

		addPriorityLevel();

		// general rule
		addRule(after(TXT).at(TXT), space);

	}
}
