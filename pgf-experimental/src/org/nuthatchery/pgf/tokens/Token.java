package org.nuthatchery.pgf.tokens;

public interface Token {

	Category getCategory();


	@Override
	String toString();


	String toText();
}
