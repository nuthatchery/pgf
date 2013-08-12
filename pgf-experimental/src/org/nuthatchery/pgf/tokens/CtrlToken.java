package org.nuthatchery.pgf.tokens;

public class CtrlToken implements Token {
	String data;
	Category cat;


	public CtrlToken(Category cat) {
		super();
		this.cat = cat;
	}


	@Override
	public Category getCategory() {
		return cat;
	}


	@Override
	public boolean hasSubCatOf(Category category) {
		return cat.isSubCatOf(category);
	}


	@Override
	public String toString() {
		return cat.toString();
	}


	@Override
	public String toText() {
		return "";
	}
}
