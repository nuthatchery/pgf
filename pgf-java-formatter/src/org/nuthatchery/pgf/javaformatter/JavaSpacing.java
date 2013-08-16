package org.nuthatchery.pgf.javaformatter;

import static org.nuthatchery.pgf.rules.PatternFactory.after;
import static org.nuthatchery.pgf.rules.PatternFactory.at;
import static org.nuthatchery.pgf.rules.PatternFactory.drop;
import static org.nuthatchery.pgf.rules.PatternFactory.insert;
import static org.nuthatchery.pgf.rules.PatternFactory.nop;

import org.nuthatchery.pgf.rules.RuleProcessor;
import org.nuthatchery.pgf.rules.actions.Action;
import org.nuthatchery.pgf.tokens.Category;
import org.nuthatchery.pgf.tokens.CategoryStore;

public class JavaSpacing extends RuleProcessor {

	@SuppressWarnings("unused")
	public JavaSpacing(CategoryStore store) {
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
		addRule(at(WS), drop); // delete all spaces

		addPriorityLevel(); // rules above have highest priority

		addRule(after(TXT).at(BINOP), space); // space around ops
		addRule(after(BINOP).at(TXT), space);

		addRule(at(PUNCT), nop); // no space before comma, semi

		// no space after left paren
		addRule(after(LPAR), nop);
		// no spaces before paren
		addRule(at(PAR), nop);
		// but always space between 'if' and '('
		addRule(after(KEYWORD).at(LPAR), space);

		//addRule(after(SEMI).at(TXT), space);

		addPriorityLevel(); // rules below have lowest priority

		// general rule
		addRule(after(TXT).at(TXT), space);

	}
}
