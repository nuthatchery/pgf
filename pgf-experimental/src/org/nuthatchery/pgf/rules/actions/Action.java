package org.nuthatchery.pgf.rules.actions;

import org.nuthatchery.pgf.plumbing.PipeConnector;
import org.nuthatchery.pgf.tokens.Token;

public interface Action {

	String getCmdName();


	String getDescription();


	boolean isReader();


	void perform(PipeConnector<Token, Token> conn);
}
