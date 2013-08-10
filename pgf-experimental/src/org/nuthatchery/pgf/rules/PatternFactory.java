package org.nuthatchery.pgf.rules;

import java.util.Arrays;

import org.nuthatchery.pgf.plumbing.PipeConnector;
import org.nuthatchery.pgf.rules.pattern.Pattern;
import org.nuthatchery.pgf.tokens.Category;
import org.nuthatchery.pgf.tokens.Token;

public class PatternFactory {
	public static Pattern after(Category... tokens) {
		return new AfterPattern(tokens);

	}


	public static Pattern at(Category... tokens) {
		return new AtPattern(Arrays.copyOf(tokens, tokens.length));
	}


	static class AfterPattern extends PatternBase {
		private Category[] cats;


		AfterPattern(Category[] cats) {
			this.cats = cats;
		}


		@Override
		public int lookAhead() {
			return 0;
		}


		@Override
		public int lookBehind() {
			return cats.length;
		}


		@Override
		public boolean match(PipeConnector<Token, Token> conn) {
			if(conn.sizeBehind() < cats.length) {
				return false;
			}
			for(int i = 0; i < cats.length; i++) {
				if(!conn.lookBehind(i).hasSubCatOf(cats[i])) {
					return false;
				}
			}
			return true;
		}
	}


	static class AndPattern extends PatternBase {

		private Pattern pat1;
		private Pattern pat2;


		public AndPattern(PatternBase pat1, PatternBase pat2) {
			this.pat1 = pat1;
			this.pat2 = pat2;
		}


		@Override
		public int lookAhead() {
			return Math.max(pat1.lookAhead(), pat2.lookAhead());
		}


		@Override
		public int lookBehind() {
			return Math.max(pat1.lookBehind(), pat2.lookBehind());
		}


		@Override
		public boolean match(PipeConnector<Token, Token> conn) {
			return pat1.match(conn) && pat2.match(conn);
		}
	}


	static class AtPattern extends PatternBase {
		private Category[] cats;


		AtPattern(Category[] cats) {
			this.cats = Arrays.copyOf(cats, cats.length);
		}


		@Override
		public int lookAhead() {
			return cats.length;
		}


		@Override
		public int lookBehind() {
			return 0;
		}


		@Override
		public boolean match(PipeConnector<Token, Token> conn) {
			if(conn.sizeAhead() < cats.length) {
				return false;
			}
			for(int i = 0; i < cats.length; i++) {
				if(!conn.lookAhead(i).hasSubCatOf(cats[i])) {
					return false;
				}
			}
			return true;
		}

	}


	static abstract class PatternBase implements Pattern {
		@Override
		public Pattern after(Category... tokens) {
			return new AndPattern(this, new AfterPattern(tokens));
		}


		@Override
		public Pattern at(Category... tokens) {
			return new AndPattern(this, new AtPattern(tokens));
		}
	}

}
