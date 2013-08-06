package org.nuthatchery.pgf.processors;

import org.nuthatchery.pgf.plumbing.PipeConnector;

public class CopyProcessor<T> extends ProcessorBase<T, T> {

	@Override
	public boolean process(PipeConnector<T, T> io) {
		if(!io.isAtEnd()) {
			io.put(io.get());
			return true;
		}
		else {
			return false;
		}
	}

}
