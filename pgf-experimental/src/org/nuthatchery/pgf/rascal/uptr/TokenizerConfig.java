package org.nuthatchery.pgf.rascal.uptr;

import java.util.Set;

import org.nuthatchery.pgf.tokens.Category;
import org.nuthatchery.pgf.tokens.CategoryStore;

public interface TokenizerConfig {

	/**
	 * @return Category for comments (non-space layout)
	 */
	Category cfgCatComment();


	Category cfgCatCtrl();


	CategoryStore cfgCategories();


	Category cfgCatForCtrlBegin();


	Category cfgCatForCtrlEnd();


	/**
	 * @return The category of horizontal space (i.e., space and tab)
	 */
	Category cfgCatHorizSpace();


	/**
	 * @return Catch-all category for unclassified text tokens
	 */
	Category cfgCatText();


	/**
	 * @return The category of vertical space (i.e., newlines)
	 */
	Category cfgCatVertSpace();


	/**
	 * @return List of sorts that should cause nesting control tokens to be
	 *         emitted
	 */
	Set<String> cfgNestSorts();


	/**
	 * @return True if non-space in layout should be made into a token
	 */
	boolean cfgSepComments();


	/**
	 * @return True if newlines in layout should be made into their own tokens
	 */
	boolean cfgSepNewlines();


	/**
	 * Find the category for a non-literal lexical token
	 * 
	 * @param lex
	 *            The token text
	 * @return The category (or cfgCatText())
	 */
	Category getCatForLexical(String lex);


	/**
	 * Find the category for a literal lexical token
	 * 
	 * @param lit
	 *            The token text
	 * @return The category (or cfgCatText())
	 */
	Category getCatForLiteral(String lit);
}
