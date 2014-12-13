package org.nuthatchery.pgf.config;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;

import org.nuthatchery.pgf.tokens.Category;
import org.nuthatchery.pgf.tokens.CategoryStore;

public abstract class TokenizerConfigBase implements TokenizerConfig {
	public static final String DEFAULT_ID_REGEX = "^[_a-zA-Z][_a-zA-Z0-9]*$";
	public static final String DEFAULT_KW_REGEX = "^[a-z]+$";
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
	private Category cat_end;
	private Category cat_begin;


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

		//store.compile();
		//System.out.println(store.toJava("store", true, false));

		if(cfgUseDefaultLiteralClassification()) {
			addDefaultLiterals();
		}

		if(cfgKeywords() != null) {
			for(String kw : cfgKeywords()) {
				litStringMap.put(kw, store.declare(kw, cat_kw.getName()));
			}
		}

		moreCategories(store);

		store.compile();
		try {
			PrintWriter stream = new PrintWriter(new FileOutputStream("/tmp/cats.html"));
			stream.print(store.toHTML());
			stream.close();

		}
		catch(FileNotFoundException e) {
			e.printStackTrace();
		}

		System.err.println("Subcats of LGRP: ");
		for(Category c : store.subCategoriesOf(store.category("LGRP"))) {
			System.err.println("  " + c + " dist " + store.superDist(store.category("LGRP"), c));
		}
	}


	@Override
	public Category cfgCatComment() {
		return cat_comment;
	}


	@Override
	public Category cfgCatCtrl() {
		return cat_ctrl;
	}


	@Override
	public CategoryStore cfgCategories() {
		return store;
	}


	@Override
	public Category cfgCatForCtrlBegin() {
		return cat_begin;
	}


	@Override
	public Category cfgCatForCtrlEnd() {
		return cat_end;
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
		store.declare("TOKEN");
		cat_kw = cat_txt = store.declare("TXT");
		cat_comment = cat_hspc = cat_vspc = store.declare("SPC");
		cat_ctrl = store.declare("CTRL");

		store.declare("TXT", "TOKEN");
		store.declare("SPC", "TOKEN");
		store.declare("CTRL", "TOKEN");

		store.declare("START", "CTRL");
		store.declare("STOP", "CTRL");

		cat_begin = store.declare("BEGIN", "CTRL");
		cat_end = store.declare("END", "CTRL");
	}


	protected void addDefaultCats() {
		addBasicCats();

/*			category KEYWORD, PUNCT, ID, LITERAL, PREOP, BINOP, GRP <: TXT;
			category IF, ELSE <: KEYWORD;
			category PAR, BRC <: GRP;
			category LPAR, RPAR <: PAR;
			category LBRC, RBRC <: BRC;
			category LPAR, LBRC <: LGRP;
			category RPAR, RBRC <: RGRP;
			category COMMA, SEMICOLON <: PUNCT;
			*/
		cat_hspc = store.declare("WS", "SPC");
		cat_vspc = store.declare("NL", "SPC");
		cat_comment = store.declare("COM", "SPC");

		store.declare("LITERAL", "TXT");
		store.declare("ID", "TXT");
		cat_kw = store.declare("KEYWORD", "LITERAL");
		store.declare("OP", "LITERAL");
		store.declare("PREOP", "OP");
		store.declare("BINOP", "OP");
		store.declare("POSTOP", "OP");

		store.declare("GRP", "LITERAL");
		store.declare("PAR", "GRP");
		store.declare("BRC", "GRP");
		store.declare("BRT", "GRP");
		store.declare("LGRP", "GRP");
		store.declare("RGRP", "GRP");
		store.declare("LPAR", "PAR", "LGRP");
		store.declare("RPAR", "PAR", "RGRP");
		store.declare("LBRC", "BRC", "LGRP");
		store.declare("RBRC", "BRC", "RGRP");
		store.declare("LBRT", "BRT", "LGRP");
		store.declare("RBRT", "BRT", "RGRP");

		store.declare("PUNCT", "LITERAL");
		store.declare("COMMA", "PUNCT");
		store.declare("SEMI", "PUNCT");
		store.declare("COLON", "PUNCT");
		store.declare("DOT", "PUNCT");

	}


	protected void addDefaultLiterals() {
		addLitString("(", "LPAR");
		addLitString(")", "RPAR");
		addLitString("[", "LBRT");
		addLitString("]", "RBRT");
		addLitString("{", "LBRC");
		addLitString("}", "RBRC");
		addLitString(",", "COMMA");
		addLitString(".", "DOT");
		addLitString(";", "SEMI");
		addLitString(":", "COLON");
		String regex = cfgKeywordRegex();
		if(regex != null) {
			addLitRegex(regex, "KEYWORD");
		}
		regex = cfgIdentifierRegex();
		if(regex != null) {
			addLexRegex(regex, "ID");
		}
	}


	protected void addLexRegex(String regex, String category) {
		Category cat = store.category(category);
		lexRegexMap.put(Pattern.compile(regex), cat);
	}


	protected void addLexString(String literal, String category) {
		Category cat = store.category(category);
		lexStringMap.put(literal, cat);
	}


	protected void addLitRegex(String regex, String category) {
		Category cat = store.category(category);
		litRegexMap.put(Pattern.compile(regex), cat);
	}


	protected void addLitString(String literal, String category) {
		Category cat = store.category(category);
		litStringMap.put(literal, cat);
	}


	/**
	 * @return Regular expression for identifiers, or null for no automatic
	 *         classification of identifiers
	 * @see {@link #DEFAULT_ID_REGEX}
	 */
	protected abstract String cfgIdentifierRegex();


	protected abstract CatSet cfgInitialCategorySet();


	/**
	 * @return Regular expression for keywords, or null for no automatic
	 *         classification of keywords
	 * 
	 * @see {@link #DEFAULT_KW_REGEX}
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
	 * "(":LPAR, ")":RPAR, "[":LBRT, "]":RBRT, "{":LBRC,
	 * "}":RBRC as individual subcategories of GRP
	 * 
	 * ",":COMMA, ".":DOT, ";":SEMICOLON, ":":COLON as individual subcategories
	 * of PUNCT
	 * 
	 * cfgKeywordRegex() as keywords.
	 * 
	 * @return True if default literals should be automatically classified.
	 */
	protected abstract boolean cfgUseDefaultLiteralClassification();


	protected abstract void moreCategories(CategoryStore store);


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
		 * GRP and categories for individual parens/grouping, punctations
		 * and
		 * PREOP/BINOP/POSTOP.
		 */
		CATSET_DEFAULT;
	}

}
