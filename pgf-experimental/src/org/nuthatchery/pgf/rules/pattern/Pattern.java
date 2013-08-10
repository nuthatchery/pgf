package org.nuthatchery.pgf.rules.pattern;

import java.util.Arrays;

import org.nuthatchery.pgf.plumbing.PipeConnector;
import org.nuthatchery.pgf.tokens.Category;
import org.nuthatchery.pgf.tokens.Token;

public interface Pattern {
	Pattern after(Category... tokens);


	Pattern at(Category... tokens);


	int lookAhead();


	int lookBehind();


	boolean match(PipeConnector<Token, Token> conn);
}
