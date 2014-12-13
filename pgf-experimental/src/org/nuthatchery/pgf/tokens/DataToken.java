package org.nuthatchery.pgf.tokens;

public class DataToken implements Token {
	String data;
	Category cat;


	public DataToken(String data, Category cat) {
		super();
		this.data = data;
		this.cat = cat;
	}


	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj == null) {
			return false;
		}
		if(getClass() != obj.getClass()) {
			return false;
		}
		DataToken other = (DataToken) obj;
		if(cat == null) {
			if(other.cat != null) {
				return false;
			}
		}
		else if(!cat.equals(other.cat)) {
			return false;
		}
		if(data == null) {
			if(other.data != null) {
				return false;
			}
		}
		else if(!data.equals(other.data)) {
			return false;
		}
		return true;
	}


	@Override
	public Category getCategory() {
		return cat;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((cat == null) ? 0 : cat.hashCode());
		result = prime * result + ((data == null) ? 0 : data.hashCode());
		return result;
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
