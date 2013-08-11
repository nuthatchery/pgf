package org.nuthatchery.pgf.tokens;

public interface Category {

	int getId();


	String getName();


	CategoryStore getStore();


	boolean isSubCatOf(Category category);

}
