package org.nuthatchery.pgf.processors;

import org.nuthatchery.pgf.plumbing.PipeConnector;

public interface Processor<T, U> {
	int CFG_NO_LOOK_AHEAD = 0;
	int CFG_NO_LOOK_BEHIND = 0;


	T cfgEndMarker();


	boolean cfgIsSink();


	int cfgMinLookAhead();


	int cfgMinLookBehind();


	boolean cfgUseEndMarker();


	boolean process(PipeConnector<T, U> io);


	void restart();
}
