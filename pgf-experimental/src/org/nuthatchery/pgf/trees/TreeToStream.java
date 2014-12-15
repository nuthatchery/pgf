package org.nuthatchery.pgf.trees;

import org.nuthatchery.pgf.plumbing.ForwardStream;
import org.nuthatchery.pgf.tokens.Token;

public interface TreeToStream<Value> {
	void printTree(Value tree, ForwardStream<Token> output);
}
