package org.nuthatchery.pgf.plumbing;

public interface ForwardStream<T> {
	void end();


	void put(T obj);


	void restart();
}
