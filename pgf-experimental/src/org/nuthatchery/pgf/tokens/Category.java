package org.nuthatchery.pgf.tokens;

public interface Category {

	String getName();


	CategoryStore getStore();


	Iterable<Category> supers();

}
