package org.nuthatchery.pgf.processors;

public abstract class ProcessorBase<T, U> implements Processor<T, U> {

	@Override
	public T cfgEndMarker() {
		return null;
	}


	@Override
	public boolean cfgIsSink() {
		return false;
	}


	@Override
	public int cfgMinLookAhead() {
		return 0;
	}


	@Override
	public int cfgMinLookBehind() {
		return 0;
	}


	@Override
	public boolean cfgUseEndMarker() {
		return false;
	}


	@Override
	public void restart() {

	}
}
