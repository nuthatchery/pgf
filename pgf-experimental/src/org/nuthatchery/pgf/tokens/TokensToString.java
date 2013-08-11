package org.nuthatchery.pgf.tokens;

import org.nuthatchery.pgf.plumbing.PipeConnector;
import org.nuthatchery.pgf.processors.ProcessorBase;

public class TokensToString extends ProcessorBase<Token, Token> {
	StringBuilder buffer = new StringBuilder();


	@Override
	public boolean cfgUseEndMarker() {
		return false;
	}


	@Override
	public boolean process(PipeConnector<Token, Token> io) {
		Token token = io.get();

		buffer.append(token.toText());

		return true;
	}


	@Override
	public void restart() {
		buffer = new StringBuilder();

	}


	@Override
	public String toString() {
		return buffer.toString();
	}

}
