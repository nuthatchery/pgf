package org.nuthatchery.pgf.rascal.uptr;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;

import org.nuthatchery.pgf.tokens.Category;
import org.nuthatchery.pgf.tokens.CategoryStore;

public abstract class TokenizerConfigBase implements TokenizerConfig {
	private final CategoryStore store = new CategoryStore();
	private final Map<String, Category> litStringMap = new HashMap<String, Category>();
	private final Map<Pattern, Category> litRegexMap = new HashMap<Pattern, Category>();
	private final Map<String, Category> lexStringMap = new HashMap<String, Category>();
	private final Map<Pattern, Category> lexRegexMap = new HashMap<Pattern, Category>();

	private Category cat_txt;
	private Category cat_hspc;
	private Category cat_vspc;
	private Category cat_ctrl;
	private Category cat_comment;
	private Category cat_kw;


	public TokenizerConfigBase() {
		switch(cfgInitialCategorySet()) {
		case CATSET_NONE:
			break;
		case CATSET_BASIC:
			addBasicCats();
			break;
		case CATSET_DEFAULT:
			addDefaultCats();
			break;
		}

		if(cfgUseDefaultLiteralClassification()) {
			addDefaultLiterals();
		}

		if(cfgKeywords() != null) {
			for(String kw : cfgKeywords()) {
				litStringMap.put(kw, store.declare(kw, cat_kw.getName()));
			}
		}
	}


	@Override
	public Category cfgCatComment() {
		return cat_comment;
	}


	@Override
	public CategoryStore cfgCategories() {
		return store;
	}


	@Override
	public Category cfgCatHorizSpace() {
		return cat_hspc;
	}


	@Override
	public Category cfgCatText() {
		return cat_txt;
	}


	@Override
	public Category cfgCatVertSpace() {
		return cat_vspc;
	}


	@Override
	public Set<String> cfgNestSorts() {
		return Collections.EMPTY_SET;
	}


	@Override
	public boolean cfgSepComments() {
		return true;
	}


	@Override
	public boolean cfgSepNewlines() {
		return true;
	}


	@Override
	public Category getCatForLexical(String lex) {
		Category cat = lexStringMap.get(lex);
		if(cat != null) {
			return cat;
		}

		for(Entry<Pattern, Category> entry : lexRegexMap.entrySet()) {
			if(entry.getKey().matcher(lex).matches()) {
				return entry.getValue();
			}
		}
		return cat_txt;
	}


	@Override
	public Category getCatForLiteral(String lit) {
		Category cat = litStringMap.get(lit);
		if(cat != null) {
			return cat;
		}

		for(Entry<Pattern, Category> entry : litRegexMap.entrySet()) {
			if(entry.getKey().matcher(lit).matches()) {
				return entry.getValue();
			}
		}
		return cat_txt;
	}


	protected void addBasicCats() {
		store.category("TOKEN");
		cat_kw = cat_txt = store.category("TXT");
		cat_comment = cat_hspc = cat_vspc = store.category("SPC");
		cat_ctrl = store.category("CTRL");

		store.declareSuper("TXT", "TOKEN");
		store.declareSuper("SPC", "TOKEN");
		store.declareSuper("CTRL", "TOKEN");

	}


	protected void addDefaultCats() {
		addBasicCats();

/*			category KEYWORD, PUNCT, ID, LITERAL, PREOP, BINOP, GROUPING <: TXT;
			category IF, ELSE <: KEYWORD;
			category PAREN, BRACE <: GROUPING;
			category LPAREN, RPAREN <: PAREN;
			category LBRACE, RBRACE <: BRACE;
			category LPAREN, LBRACE <: LGROUPING;
			category RPAREN, RBRACE <: RGROUPING;
			category COMMA, SEMICOLON <: PUNCT;
			*/
		cat_hspc = store.declare("WS", "SPC");
		cat_vspc = store.declare("NL", "SPC");
		cat_comment = store.declare("COM", "SPC");

		store.declare("LITERAL", "TXT");
		cat_kw = store.declare("KEYWORD", "LITERAL");
		store.declare("OP", "LITERAL");
		store.declare("PREOP", "OP");
		store.declare("BINOP", "OP");
		store.declare("POSTOP", "OP");

		store.declare("GROUPING", "LITERAL");
		store.declare("PAREN", "GROUPING");
		store.declare("BRACE", "GROUPING");
		store.declare("BRACKET", "GROUPING");
		store.declare("LPAREN", "PAREN", "LGROUPING");
		store.declare("RPAREN", "PAREN", "RGROUPING");
		store.declare("LBRACE", "BRACE", "LGROUPING");
		store.declare("RBRACE", "BRACE", "RGROUPING");
		store.declare("LBRACKET", "BRACKET", "LGROUPING");
		store.declare("RBRACKET", "BRACKET", "RGROUPING");

		store.declare("PUNCT", "LITERAL");
		store.declare("COMMA", "PUNCT");
		store.declare("SEMICOLON", "PUNCT");
		store.declare("COLON", "PUNCT");

	}


	protected void addDefaultLiterals() {
		addLitString("(", "LPAREN");
		addLitString(")", "RPAREN");
		addLitString("[", "LBRACKET");
		addLitString("]", "RBRACKET");
		addLitString("{", "LBRACE");
		addLitString("}", "RBRACE");
		addLitString(",", "COMMA");
		addLitString(".", "DOT");
		addLitString(";", "SEMICOLON");
		addLitString(":", "COLON");
		String regex = cfgKeywordRegex();
		if(regex == null) {
			regex = "^[a-z]+$";
		}
		addLitRegex(regex, "KEYWORD");
	}


	protected void addLexRegex(String regex, String category) {
		Category cat = store.category(category);
		litRegexMap.put(Pattern.compile(regex), cat);
	}


	protected void addLexString(String literal, String category) {
		Category cat = store.category(category);
		litStringMap.put(literal, cat);
	}


	protected void addLitRegex(String regex, String category) {
		Category cat = store.category(category);
		litRegexMap.put(Pattern.compile(regex), cat);
	}


	protected void addLitString(String literal, String category) {
		Category cat = store.category(category);
		litStringMap.put(literal, cat);
	}


	protected abstract CatSet cfgInitialCategorySet();


	/**
	 * @return Regular expression for keywords, or null for default ("^[a-z]+$")
	 */
	protected abstract String cfgKeywordRegex();


	/**
	 * @return A list of keywords (in addition to cfgKeywordRegex()), or null
	 *         for no extra keywords
	 */
	protected abstract Collection<String> cfgKeywords();


	/**
	 * If true, will classify:
	 * 
	 * "(":LPAREN, ")":RPAREN, "[":LBRACKET, "]":RBRACKET, "{":LBRACE,
	 * "}":RBRACE as individual subcategories of GROUPING
	 * 
	 * ",":COMMA, ".":DOT, ";":SEMICOLON, ":":COLON as individual subcategories
	 * of PUNCT
	 * 
	 * cfgKeywordRegex() as keywords.
	 * 
	 * @return True if default literals should be automatically classified.
	 */
	protected abstract boolean cfgUseDefaultLiteralClassification();


	public enum CatSet {
		/**
		 * Predeclare *no* categories
		 */
		CATSET_NONE,
		/**
		 * Predeclare only TOKEN, TXT, SPC and CTRL.
		 */
		CATSET_BASIC,
		/**
		 * Predeclare all basic categories, as well as LITERAL, PUNCT, KEYWORD,
		 * OP,
		 * GROUPING and categories for individual parens/grouping, punctations
		 * and
		 * PREOP/BINOP/POSTOP.
		 */
		CATSET_DEFAULT;
	}
}
