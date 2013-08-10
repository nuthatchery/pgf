package org.nuthatchery.pgf.tokens;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

public class CategoryStore {
	private Map<String, Category> cats = new HashMap<String, Category>();
	private Map<Category, Set<Category>> superMap = new IdentityHashMap<Category, Set<Category>>();


	public Category category(String name) {
		Category category = cats.get(name);
		if(category == null) {
			category = new CategoryImpl(name, this);
			cats.put(name, category);
		}
		return category;

	}


	public Category declare(String name, String... supers) {
		Category category = category(name);
		for(String s : supers) {
			declareSuper(name, s);
		}
		return category;
	}


	public void declareSuper(String subCat, String superCat) {
		declareSuper(category(subCat), category(superCat));
	}


	private void declareSuper(Category subCat, Category superCat) {
		if(subCat.getStore() != this || superCat.getStore() != this) {
			throw new IllegalArgumentException("Category does not belong to this store");
		}
		Set<Category> supers = superMap.get(subCat);

		if(supers == null) {
			supers = new HashSet<Category>();
			superMap.put(subCat, supers);
		}

		supers.add(superCat);
	}


	protected Collection<Category> supersOf(Category cat) {
		Set<Category> supers = superMap.get(cat);
		if(supers == null) {
			return Collections.EMPTY_SET;
		}
		else {
			return Collections.unmodifiableCollection(supers);
		}
	}


	static class CategoryImpl implements Category {
		private final String name;
		private final CategoryStore store;


		public CategoryImpl(String name, CategoryStore categoryStore) {
			this.name = name;
			this.store = categoryStore;
		}


		@Override
		public boolean equals(Object obj) {
			return this == obj;
		}


		@Override
		public String getName() {
			return name;
		}


		@Override
		public CategoryStore getStore() {
			return store;
		}


		@Override
		public int hashCode() {
			return System.identityHashCode(this);
		}


		@Override
		public boolean isSubCatOf(Category category) {
			if(this == category) {
				return true;
			}

			for(Category sup : supers()) {
				if(sup.isSubCatOf(category)) {
					return true;
				}
			}
			return false;
		}


		@Override
		public Iterable<Category> supers() {
			return store.supersOf(this);
		}

	}
}
