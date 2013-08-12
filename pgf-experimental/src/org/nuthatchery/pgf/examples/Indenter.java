package org.nuthatchery.pgf.examples;

import org.nuthatchery.pgf.plumbing.PipeConnector;
import org.nuthatchery.pgf.rules.RuleProcessor;
import org.nuthatchery.pgf.rules.actions.Action;
import org.nuthatchery.pgf.tokens.Category;
import org.nuthatchery.pgf.tokens.CategoryStore;
import org.nuthatchery.pgf.tokens.DataToken;
import org.nuthatchery.pgf.tokens.Token;

import static org.nuthatchery.pgf.rules.PatternFactory.*;

public class Indenter extends RuleProcessor {

	protected int nest;
	final Category WS;


	@SuppressWarnings("unused")
	public Indenter(CategoryStore store) {
		super(store);
		boolean spaceBeforeCond = true;
		WS = store.category("WS");

		final Category TOKEN = store.category("TOKEN");
		final Category TXT = store.category("TXT");
		final Category SPC = store.category("SPC");
		final Category CTRL = store.category("CTRL");
		final Category START = store.category("START");
		final Category END = store.category("END");

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
		Action indent = new IndentAction();

		addRule(after(NL).at(WS), delete);
		addRule(after(NL).at(NL), nop);

		addRule(after(NL).at(WS), seq(delete, indent));
		addRule(after(NL), seq(indent, move));
	}


	@Override
	protected void nestBegin(Category cat) {
		nest++;
	}


	@Override
	protected void nestEnd(Category cat) {
		--nest;
	}


	class IndentAction implements Action {

		@Override
		public String getCmdName() {
			return "indent";
		}


		@Override
		public String getDescription() {
			return "Indent according to current nesting";
		}


		@Override
		public boolean isReader() {
			return false;
		}


		@Override
		public void perform(PipeConnector<Token, Token> conn) {
			StringBuilder buf = new StringBuilder();
			for(int i = 0; i < nest; i++) {
				buf.append("    ");
			}
			conn.putNoHistory(new DataToken(buf.toString(), WS));
		}
	}
}
