package org.nuthatchery.pgf.rules;

import java.util.Arrays;

import org.nuthatchery.pgf.plumbing.PipeConnector;
import org.nuthatchery.pgf.rules.actions.Action;
import org.nuthatchery.pgf.rules.pattern.Pattern;
import org.nuthatchery.pgf.tokens.Category;
import org.nuthatchery.pgf.tokens.DataToken;
import org.nuthatchery.pgf.tokens.Token;

public class PatternFactory {
	public static final Action move = new MoveAction();

	public static final Action nop = new NopAction();

	public static final Action delete = new DeleteAction();


	public static final Pattern after(Category... tokens) {
		Pattern pat = new PatternImpl();
		return pat.after(tokens);

	}


	public static final Pattern at(Category... tokens) {
		Pattern pat = new PatternImpl();
		return pat.at(Arrays.copyOf(tokens, tokens.length));
	}


	public static final Action insert(String data, Category cat) {
		return new InsertAction(new DataToken(data, cat));
	}


	public static final Action insert(Token tok) {
		return new InsertAction(tok);
	}


	public static final Action seq(Action... acts) {
		return new SeqAction(acts).simplify();
	}


	private static class DeleteAction implements Action {

		@Override
		public String getCmdName() {
			return "delete";
		}


		@Override
		public String getDescription() {
			return "Delete incoming token";
		}


		@Override
		public boolean isReader() {
			return true;
		}


		@Override
		public void perform(PipeConnector<Token, Token> conn) {
			conn.get();
		}


		@Override
		public String toString() {
			return "delete;";
		}

	}


	private static class InsertAction implements Action {

		private final Token tok;


		InsertAction(Token tok) {
			this.tok = tok;
		}


		@Override
		public String getCmdName() {
			return "insert";
		}


		@Override
		public String getDescription() {
			return "Insert a token";
		}


		@Override
		public boolean isReader() {
			return false;
		}


		@Override
		public void perform(PipeConnector<Token, Token> conn) {
			conn.put(tok);
		}


		@Override
		public String toString() {
			return "insert " + tok.toString() + ";";
		}

	}


	private static class MoveAction implements Action {

		@Override
		public String getCmdName() {
			return "move";
		}


		@Override
		public String getDescription() {
			return "Move incoming token to output";
		}


		@Override
		public boolean isReader() {
			return true;
		}


		@Override
		public void perform(PipeConnector<Token, Token> conn) {
			conn.put(conn.get());
		}


		@Override
		public String toString() {
			return "move;";
		}

	}


	private static class NopAction implements Action {
		@Override
		public String getCmdName() {
			return "nop";
		}


		@Override
		public String getDescription() {
			return "Do nothing";
		}


		@Override
		public boolean isReader() {
			return false;
		}


		@Override
		public void perform(PipeConnector<Token, Token> conn) {
		}


		@Override
		public String toString() {
			return "nop;";
		}
	}


	private static class SeqAction implements Action {

		private final Action[] actions;


		SeqAction(Action... acts) {
			Action[] tmp = new Action[acts.length];
			int i = 0;
			for(Action a : acts) {
				if(a != nop) {
					tmp[i++] = a;
				}
			}

			if(i != tmp.length) {
				actions = Arrays.copyOf(tmp, i);
			}
			else {
				actions = tmp;
			}
		}


		@Override
		public String getCmdName() {
			return "seq";
		}


		@Override
		public String getDescription() {
			return "Perform a sequence of actions";
		}


		@Override
		public boolean isReader() {
			int readers = 0;
			for(Action a : actions) {
				if(a.isReader()) {
					readers++;
				}
			}

			if(readers > 1) {
				throw new RuntimeException("Action reads multiple inputs");
			}

			return readers == 1;
		}


		@Override
		public void perform(PipeConnector<Token, Token> conn) {
			for(Action a : actions) {
				a.perform(conn);
			}
		}


		public Action simplify() {
			if(actions.length == 0) {
				return new NopAction();
			}
			else if(actions.length == 1) {
				return actions[0];
			}
			else {
				return this;
			}
		}


		@Override
		public String toString() {
			StringBuilder buf = new StringBuilder();
			buf.append("{ ");
			for(Action a : actions) {
				buf.append(a.toString());
				buf.append(" ");
			}
			buf.append("}");
			return buf.toString();
		}
	}


	static class PatternImpl implements Pattern {
		private Category[] ahead;
		private Category[] behind;


		@Override
		public Pattern after(Category... tokens) {
			if(behind != null) {
				throw new IllegalArgumentException("This pattern already has an after-pattern");
			}
			behind = Arrays.copyOf(tokens, tokens.length);
			return this;
		}


		@Override
		public Pattern at(Category... tokens) {
			if(ahead != null) {
				throw new IllegalArgumentException("This pattern already has an at-pattern");
			}
			ahead = Arrays.copyOf(tokens, tokens.length);

			return this;
		}


		@Override
		public Category[] getLookAhead() {
			return ahead;
		}


		@Override
		public Category[] getLookBehind() {
			return behind;
		}


		@Override
		public int numLookAhead() {
			return ahead != null ? ahead.length : 0;
		}


		@Override
		public int numLookBehind() {
			return behind != null ? behind.length : 0;
		}

	}

}
