package org.nuthatchery.pgf.tokens;

public interface Token {

	Category getCategory();


	boolean hasSubCatOf(Category category);


	@Override
	String toString();


	String toText();
}
