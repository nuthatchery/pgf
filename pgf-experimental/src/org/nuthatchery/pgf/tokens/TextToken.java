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
	public boolean hasSubCatOf(Category category) {
		return cat.isSubCatOf(category);
	}


	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder(data.length() * 2);
		buf.append("\"");
		buf.append(data.replace("\\", "\\\\").replace("\n", "\\n").replace("\"", "\\"));
		buf.append("\"");

		if(cat != null) {
			buf.append(":");
			buf.append(cat.getName());
		}

		return buf.toString();
	}


	@Override
	public String toText() {
		return data;
	}
}
