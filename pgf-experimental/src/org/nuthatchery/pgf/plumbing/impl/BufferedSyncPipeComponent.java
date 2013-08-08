package org.nuthatchery.pgf.plumbing.impl;

import java.util.ConcurrentModificationException;

import org.nuthatchery.pgf.plumbing.ForwardPipe;
import org.nuthatchery.pgf.plumbing.ForwardStream;
import org.nuthatchery.pgf.processors.Processor;
import org.nuthatchery.pgf.tokens.Token;

public class BufferedSyncPipeComponent<T, U> implements ForwardPipe<T, U> {

	ForwardStream<U> next = null;
	private boolean working = false;
	private final BufferedConnector<T, U> conn;
	final Processor<T, U> proc;


	public BufferedSyncPipeComponent(Processor<T, U> proc) {
		this.proc = proc;
		conn = new BufferedConnector<T, U>(proc.cfgMinLookAhead(), proc.cfgMinLookBehind());
	}


	@Override
	public <R> ForwardPipe<U, R> connect(ForwardPipe<U, R> next) {
		connect((ForwardStream<U>) next);
		return next;
	}


	@Override
	public synchronized void connect(ForwardStream<U> next) {
		if(proc.cfgIsSink()) {
			throw new IllegalArgumentException("Trying to connect to sink");
		}
		this.next = next;
		conn.connect(next);
	}


	@Override
	public synchronized void end() {
		if(working) {
			throw new ConcurrentModificationException();
		}
		try {
			working = true;
			conn.end();
			if(proc.cfgUseEndMarker()) {
				conn.add(proc.cfgEndMarker());
			}

			while(!conn.isEmpty()) {
				if(!proc.process(conn)) {
					break;
				}
			}
		}
		finally {
			working = false;
		}
	}


	@Override
	public synchronized ForwardStream<U> getNextPipe() {
		return next;
	}


	@Override
	public synchronized void put(T obj) {
		if(working) {
			throw new ConcurrentModificationException();
		}
		try {
			working = true;
			conn.add(obj);

			while(conn.sizeAhead() > proc.cfgMinLookAhead()) {
				if(!proc.process(conn)) {
					break;
				}
			}
		}
		finally {
			working = false;
		}
	}


	@Override
	public synchronized void restart() {
		if(working) {
			throw new ConcurrentModificationException();
		}
		try {
			working = true;

			proc.restart();
			conn.clear();
			next.restart();
		}
		finally {
			working = false;
		}
	}
}
