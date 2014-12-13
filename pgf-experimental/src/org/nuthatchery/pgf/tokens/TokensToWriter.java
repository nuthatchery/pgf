package org.nuthatchery.pgf.tokens;

import java.io.PrintStream;
import java.io.PrintWriter;

import org.nuthatchery.pgf.plumbing.PipeConnector;
import org.nuthatchery.pgf.processors.ProcessorBase;

public class TokensToWriter extends ProcessorBase<Token, Token> {
	PrintWriter writer;


	public TokensToWriter(PrintStream stream) {
		this.writer = new PrintWriter(stream);
	}


	public TokensToWriter(PrintWriter writer) {
		this.writer = writer;
	}


	@Override
	public boolean cfgUseEndMarker() {
		return true;
	}


	@Override
	public boolean process(PipeConnector<Token, Token> io) {
		Token token = io.get();

		if(token != null) {
			writer.print(token.toText());
		}
		else {
			writer.flush();
		}

		return true;
	}


	@Override
	public void restart() {
	}

}
