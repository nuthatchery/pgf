package org.nuthatchery.pgf.processors;

import java.util.ArrayList;
import java.util.List;

import org.nuthatchery.pgf.plumbing.PipeConnector;

public class Accumulator<T> extends ProcessorBase<T, T> {
	private List<T> data = null;


	@Override
	public boolean cfgIsSink() {
		return true;
	}


	public List<T> drain() {
		List<T> result = data;
		data = null;
		if(result != null) {
			return result;
		}
		else {
			return new ArrayList<T>();
		}
	}


	@Override
	public boolean process(PipeConnector<T, T> io) {
		if(data == null) {
			data = new ArrayList<T>();
		}
		if(!io.isAtEnd()) {
			data.add(io.get());
		}
		return true;
	}

}
