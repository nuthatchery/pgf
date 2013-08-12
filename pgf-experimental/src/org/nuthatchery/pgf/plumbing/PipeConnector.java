package org.nuthatchery.pgf.plumbing;

import org.nuthatchery.testutil.Modified;

public interface PipeConnector<T, U> {
	/**
	 * Remove and retrieve the next token from the input buffer.
	 * 
	 * @return A token
	 * @throws IndexOutOfBoundsException
	 *             if isEmpty()
	 */
	T get();


	/**
	 * Test for end of stream.
	 * 
	 * Implies isEmpty(). The end-of-stream marker must have been removed before
	 * isAtEnd() is true.
	 * 
	 * @return True if at end of stream.
	 */
	boolean isAtEnd();


	/**
	 * Check if input buffer is empty.
	 * 
	 * @return True if no tokens are currently available.
	 */
	boolean isEmpty();


	/**
	 * Test if the final tokens are in the input buffer.
	 * 
	 * @return True if the end of stream is sizeAhead() tokens ahead.
	 */
	boolean isNearEnd();


	/**
	 * Inspect the input buffer.
	 * 
	 * The input buffer is not changed.
	 * 
	 * @param pos
	 *            A position (0 is the next token to be read)
	 * @return Token at position pos
	 * @throws IndexOutOfBoundsException
	 *             if pos < 0 || pos >= sizeAhead()
	 */
	T lookAhead(int pos);


	/**
	 * Inspect the output history
	 * 
	 * The history is not changed.
	 * 
	 * @param pos
	 *            A position (0 is the last token written)
	 * @return Token at position pos
	 * @throws IndexOutOfBoundsException
	 *             if pos < 0 || pos >= sizeBehind()
	 */
	U lookBehind(int pos);


	/**
	 * Output a token
	 * 
	 * @param obj
	 *            The token
	 */
	void put(U obj);


	/**
	 * Output a token, bypassing history
	 * 
	 * @param obj
	 *            The token
	 */
	void putNoHistory(U obj);


	/**
	 * Inspect the input buffer.
	 * 
	 * @return Current size of input buffer
	 */
	int sizeAhead();


	/**
	 * Inspect the output history.
	 * 
	 * @return Current size of output history
	 */
	int sizeBehind();


	/**
	 * Put a token back into the input buffer.
	 * 
	 * The token can be retrieved by a subsequent get().
	 * 
	 * @param obj
	 *            A token
	 */
	void unget(T obj);


	class Axioms<T, U> {
		/**
		 * isEmpty() <=> sizeAhead() == 0
		 */
		public boolean emptySizeAhead(PipeConnector<T, U> conn) {
			return conn.isEmpty() == (conn.sizeAhead() == 0);
		}


		/**
		 * isAtEnd() => isEmpty()
		 * isNearEnd() && isEmpty() <=> isAtEnd()
		 */
		public boolean endEmpty(PipeConnector<T, U> conn) {
			boolean result = true;
			if(conn.isAtEnd()) {
				result &= conn.isEmpty();
			}
			result &= (conn.isNearEnd() && conn.isEmpty()) == conn.isAtEnd();
			return result;
		}


		/**
		 * The result of get() will be the same as lookAhead(0).
		 */
		public boolean getLookAhead1(@Modified PipeConnector<T, U> conn) {
			if(!conn.isEmpty()) {
				T tmp = conn.lookAhead(0);

				return conn.get() == tmp;
			}
			else {
				return true;
			}
		}


		/**
		 * After get(), the input buffer moves one step forward.
		 */
		public boolean getLookAhead2(@Modified PipeConnector<T, U> conn, int i) {
			if(conn.sizeAhead() >= 2) {
				i = i % (conn.sizeAhead() - 1);
				T tmp = conn.lookAhead(i + 1);
				conn.get();

				return conn.lookAhead(i) == tmp;
			}
			else {
				return true;
			}
		}


		/**
		 * After get(), sizeAhead() is reduced by one.
		 */
		public boolean getSizeAhead(@Modified PipeConnector<T, U> conn) {
			if(!conn.isEmpty()) {
				int size = conn.sizeAhead();
				conn.get();
				return conn.sizeAhead() == size - 1;
			}
			else {
				return true;
			}
		}


		/**
		 * After put(), object is in history if history is non-empty
		 */
		public boolean putLookBehind1(@Modified PipeConnector<T, U> conn, U obj) {
			conn.put(obj);
			return conn.sizeBehind() == 0 || conn.lookBehind(0) == obj;
		}


		/**
		 * After put(), output history moves one step
		 */
		public boolean putLookBehind2(@Modified PipeConnector<T, U> conn, U obj, int i) {
			if(conn.sizeBehind() > 1) { // history has at least two elements
				i = i % (conn.sizeBehind() - 1);
				U tmp = conn.lookBehind(i);
				conn.put(obj);
				return conn.lookBehind(i + 1) == tmp;

			}
			else if(conn.sizeBehind() == 1) { // we might have only a one-element history
				U tmp = conn.lookBehind(0);
				conn.put(obj);
				return conn.sizeBehind() == 1 || conn.lookBehind(1) == tmp;
			}
			else {
				return true;
			}
		}


		/**
		 * After put(), sizeBehind() increases by one or stays the same
		 */
		public boolean putSizeBehind(@Modified PipeConnector<T, U> conn, U obj) {
			int size = conn.sizeBehind();
			conn.put(obj);
			return conn.sizeBehind() == size || conn.sizeBehind() == size + 1;
		}

	}
}
