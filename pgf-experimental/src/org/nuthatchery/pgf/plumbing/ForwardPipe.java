package org.nuthatchery.pgf.plumbing;

public interface ForwardPipe<T, U> extends ForwardStream<T> {
	/**
	 * @param next
	 * @return The argument, for chaining purposes
	 */
	<R> ForwardPipe<U, R> connect(ForwardPipe<U, R> next);


	void connect(ForwardStream<U> next);


	ForwardStream<U> getNextPipe();
}
