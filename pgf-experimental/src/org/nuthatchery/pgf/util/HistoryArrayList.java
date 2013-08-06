package org.nuthatchery.pgf.util;

/**
 * A list which tracks the last N objects entered into it.
 * 
 * This implementation uses an array, and all operations are O(1).
 * 
 * New elements can only be inserted in front (i.e., at position 0). Elements
 * may be accessed at any position.
 * 
 * Once the list reaches size N, the oldest element is dropped whenever a new
 * object is inserted.
 * 
 * @param <T>
 *            The element type
 */
public class HistoryArrayList<T> implements HistoryList<T> {

	private final T[] data;
	private int size = 0;
	private int pos = 0;


	/**
	 * @param size
	 *            The maximum number of elements
	 */
	@SuppressWarnings("unchecked")
	public HistoryArrayList(int size) {
		if(size < 1) {
			throw new IllegalArgumentException(String.valueOf(size));
		}
		data = (T[]) new Object[size];
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.nuthatchery.pgf.util.HistoryList#clear()
	 */
	@Override
	public void clear() {
		pos = 0;
		size = 0;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj == null) {
			return false;
		}
		if(!(obj instanceof HistoryList)) {
			return false;
		}
		@SuppressWarnings("unchecked")
		HistoryList<T> other = (HistoryList<T>) obj;
		if(size != other.size()) {
			return false;
		}
		if(data.length != other.maxSize()) {
			return false;
		}
		for(int i = 0; i < size; i++) {
			T e = get(i);
			if(e != null && !get(i).equals(other.get(i))) {
				return false;
			}
			else if(other.get(i) != null) {
				return false;
			}
		}

		return true;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.nuthatchery.pgf.util.HistoryList#get(int)
	 */
	@Override
	public T get(int i) {
		if(i < size) {
			return data[(pos - 1 - i + size) % size];
		}
		else {
			throw new IndexOutOfBoundsException(String.valueOf(i));
		}
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		for(int i = 0; i < size; i++) {
			result = prime * result + get(i).hashCode();
		}
		return result;
	}


	@Override
	public int maxSize() {
		return data.length;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.nuthatchery.pgf.util.HistoryList#put(T)
	 */
	@Override
	public void put(T obj) {
		data[pos++] = obj;
		pos = pos % data.length;
		if(size < data.length) {
			size++;
		}
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.nuthatchery.pgf.util.HistoryList#size()
	 */
	@Override
	public int size() {
		return size;
	}
}
