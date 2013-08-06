package org.nuthatchery.pgf.util;

import org.nuthatchery.testutil.Modified;

/**
 * A list which tracks the last N objects entered into it.
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
public interface HistoryList<T> {

	/**
	 * Clear the list
	 */
	void clear();


	/**
	 * Get the element at position i.
	 * 
	 * @param i
	 *            An index, 0 <= i < size()
	 * @return The element at position i
	 */
	T get(int i);


	/**
	 * @return Maximum size of the list
	 */
	int maxSize();


	/**
	 * Insert an element at position 0.
	 * 
	 * @param obj
	 *            The element
	 */
	void put(T obj);


	/**
	 * @return Current size of the list
	 */
	int size();


	class Axioms<T> {
		/**
		 * Size is zero after clearing.
		 */
		public boolean clearSize(@Modified HistoryList<T> list) {
			list.clear();
			return list.size() == 0;
		}


		/**
		 * Putting will insert the item at position 0.
		 */
		public boolean putGet(@Modified HistoryList<T> list, T t) {
			if(list.maxSize() > 0) {
				list.put(t);
				return t == list.get(0);
			}
			else {
				return true;
			}
		}


		/**
		 * The entry at position i will be at position i+1 after inserting an
		 * item.
		 */
		public boolean putGetI(@Modified HistoryList<T> list, T t, int i) {
			if(list.maxSize() > 1 && list.size() > 0) {
				i = i % Math.min(list.maxSize() - 1, list.size());
				T old = list.get(i);
				list.put(t);
				return list.get(i + 1) == old;
			}
			else {
				return true;
			}
		}


		/**
		 * Size will increase on insertion until it hits max size.
		 */
		public boolean putSize(@Modified HistoryList<T> list, T t) {
			if(list.maxSize() > 0) {
				int old = list.size();
				list.put(t);
				if(old == list.maxSize()) {
					return list.size() == old;
				}
				else {
					return list.size() == old + 1;
				}
			}
			else {
				return true;
			}
		}
	}
}
