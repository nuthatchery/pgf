package org.nuthatchery.pgf.trees;

import java.io.PrintStream;

import org.nuthatchery.pgf.plumbing.ForwardStream;
import org.nuthatchery.pgf.tokens.Token;

import nuthatch.tree.TreeCursor;
import nuthatch.walker.Walker;

/**
 * A tree pretty printer, which uses Nuthatch to traverse a tree, and a set of
 * rules based on pattern matching for transforming the tree to a stream of
 * tokens.
 * 
 * Obtain a PrettyPrinter object through
 * {@link org.nuthatchery.pgf.trees.PPBuilder}.
 * 
 * If further formatting is needed, the output stream should be connected to a
 * PGF formatting pipeline.
 * 
 * @param <Value>
 * @param <Type>
 * @param <C>
 * @param <W>
 */
public interface PrettyPrinter<Value, Type, C extends TreeCursor<Value, Type>, W extends Walker<Value, Type, W>> {
	/**
	 * Connect the printer to an output.
	 * 
	 * @param output
	 *            The destination stream
	 */
	void connect(ForwardStream<Token> output);


	/**
	 * Prints the tree to the output previously given by
	 * {@link #connect(ForwardStream)}.
	 * 
	 * @param tree
	 *            A tree cursor
	 */
	void print(C tree);


	/**
	 * Prints the tree to the stream.
	 * 
	 * @param tree
	 *            A tree cursor
	 * @param output
	 *            The destination stream
	 */
	void print(C tree, ForwardStream<Token> output);


	/**
	 * Prints the tree to the stream.
	 * 
	 * @param tree
	 *            A tree cursor
	 * @param output
	 *            The destination stream
	 */
	void print(C tree, PrintStream output);


	/**
	 * Prints the tree to the string.
	 * 
	 * @param tree
	 *            A tree cursor
	 * @return The pretty-printed string
	 */
	String toString(C tree);

}
