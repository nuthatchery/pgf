package org.nuthatchery.pgf.rules;

import org.nuthatchery.pgf.plumbing.PipeConnector;
import org.nuthatchery.pgf.processors.Processor;
import org.nuthatchery.pgf.rules.pattern.Action;
import org.nuthatchery.pgf.rules.pattern.Pattern;
import org.nuthatchery.pgf.tokens.Token;

public class RuleProcessor implements Processor<Token, Token> {

	private int lookBehind = 0;
	private int lookAhead = 0;


	public void addRule(Pattern pat, Action act) {
		lookAhead = Math.max(pat.lookAhead(), lookAhead);
		lookBehind = Math.max(pat.lookBehind(), lookBehind);
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
		return lookAhead;
	}


	@Override
	public int cfgMinLookBehind() {
		return lookBehind;
	}


	@Override
	public boolean cfgUseEndMarker() {
		return false;
	}


	@Override
	public boolean process(PipeConnector<Token, Token> io) {
		return false;
	}


	@Override
	public void restart() {

	}

}
