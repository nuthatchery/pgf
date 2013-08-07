package org.nuthatchery.pgf.tokens;

public class TextToken implements Token {
	String data;
	Category cat;


	public TextToken(String data, Category cat) {
		super();
		this.data = data;
		this.cat = cat;
	}


	@Override
	public Category getCategory() {
		return cat;
	}


	@Override
	public String toString() {
		return data;
	}


	@Override
	public String toText() {
		return data;
	}
}
