package org.nuthatchery.pgf.tokens.errors;

public class UnknownCategory extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2532933013215988715L;

	public UnknownCategory(String name) {
		super(name);
	}

}
