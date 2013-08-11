package org.nuthatchery.pgf.rules.pattern;

import org.nuthatchery.pgf.tokens.Category;

public interface Pattern {
	Pattern after(Category... tokens);


	Pattern at(Category... tokens);


	Category[] getLookAhead();


	Category[] getLookBehind();


	int numLookAhead();


	int numLookBehind();
}
