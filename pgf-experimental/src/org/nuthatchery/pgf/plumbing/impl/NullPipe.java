package org.nuthatchery.pgf.plumbing.impl;

import org.nuthatchery.pgf.plumbing.ForwardPipe;

public class NullPipe<T> implements ForwardPipe<T, T> {

	@Override
	public void connect(ForwardPipe<T, ?> next) {
		throw new UnsupportedOperationException();
	}


	@Override
	public void end() {
	}


	@Override
	public ForwardPipe<T, ?> getNextPipe() {
		return null;
	}


	@Override
	public void put(T obj) {
	}


	@Override
	public void restart() {
	}

}
