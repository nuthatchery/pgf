package org.nuthatchery.pgf.tokens;

public interface Category {

	String getName();


	CategoryStore getStore();


	boolean isSubCatOf(Category category);


	Iterable<Category> supers();

}
