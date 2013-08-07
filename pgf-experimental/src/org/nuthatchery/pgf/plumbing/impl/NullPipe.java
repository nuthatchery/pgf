package org.nuthatchery.pgf.plumbing.impl;

import org.nuthatchery.pgf.plumbing.ForwardPipe;
import org.nuthatchery.pgf.plumbing.ForwardStream;

public class NullPipe<T> implements ForwardPipe<T, T> {

	@Override
	public <R> ForwardPipe<T, R> connect(ForwardPipe<T, R> next) {
		connect((ForwardStream<T>) next);
		return next;
	}


	@Override
	public void connect(ForwardStream<T> next) {
		throw new UnsupportedOperationException();
	}


	@Override
	public void end() {
	}


	@Override
	public ForwardStream<T> getNextPipe() {
		return null;
	}


	@Override
	public void put(T obj) {
	}


	@Override
	public void restart() {
	}

}
