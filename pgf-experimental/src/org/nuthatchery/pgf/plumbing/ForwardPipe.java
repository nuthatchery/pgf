package org.nuthatchery.pgf.plumbing;

public interface ForwardPipe<T, U> extends ForwardStream<T> {
	void connect(ForwardPipe<U, ?> next);


	ForwardPipe<U, ?> getNextPipe();
}
