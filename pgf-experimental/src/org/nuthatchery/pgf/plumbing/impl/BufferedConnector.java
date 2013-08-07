package org.nuthatchery.pgf.plumbing.impl;

import java.util.ArrayList;
import java.util.List;

import org.nuthatchery.pgf.plumbing.ForwardPipe;
import org.nuthatchery.pgf.plumbing.ForwardStream;
import org.nuthatchery.pgf.plumbing.PipeConnector;
import org.nuthatchery.pgf.util.HistoryArrayList;
import org.nuthatchery.pgf.util.HistoryList;

public class BufferedConnector<T, U> implements PipeConnector<T, U> {
	private final List<T> inputBuffer;
	private final HistoryList<U> outputBuffer;
	private boolean end = false;
	private ForwardStream<U> output = null;


	public BufferedConnector(int minLookAhead, int minLookBehind) {
		if(minLookAhead < 0) {
			minLookAhead = -minLookAhead;
		}
		minLookAhead = Math.max(8, minLookAhead + 1);
		inputBuffer = new ArrayList<T>(minLookAhead);

		if(minLookBehind > 0) {
			outputBuffer = new HistoryArrayList<U>(minLookBehind);
		}
		else {
			outputBuffer = null;
		}
	}


	public void add(T obj) {
		inputBuffer.add(obj);
	}


	public void connect(ForwardStream<U> output) {
		this.output = output;
	}


	@Override
	public T get() {
		return inputBuffer.remove(0);
	}


	@Override
	public boolean isAtEnd() {
		return end && inputBuffer.isEmpty();
	}


	@Override
	public boolean isEmpty() {
		return inputBuffer.isEmpty();
	}


	@Override
	public boolean isNearEnd() {
		return end;
	}


	@Override
	public T lookAhead(int pos) {
		return inputBuffer.get(pos);
	}


	@Override
	public U lookBehind(int pos) {
		if(outputBuffer != null) {
			return outputBuffer.get(pos);
		}
		else {
			throw new IndexOutOfBoundsException(String.valueOf(pos));
		}
	}


	@Override
	public void put(U obj) {
		if(output != null) {
			output.put(obj);
		}
		if(outputBuffer != null) {
			outputBuffer.put(obj);
		}
	}


	@Override
	public int sizeAhead() {
		return inputBuffer.size();
	}


	@Override
	public int sizeBehind() {
		if(outputBuffer != null) {
			return outputBuffer.size();
		}
		else {
			return 0;
		}
	}


	@Override
	public void unget(T obj) {
		inputBuffer.add(0, obj);
	}


	void clear() {
		inputBuffer.clear();
		if(outputBuffer != null) {
			outputBuffer.clear();
		}
		end = false;
	}


	void end() {
		end = true;
	}

}
