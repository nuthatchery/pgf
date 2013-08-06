package org.nuthatchery.pgf.test.util;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.Before;
import org.junit.Test;
import org.nuthatchery.pgf.util.HistoryArrayList;
import org.nuthatchery.pgf.util.HistoryList;
import org.nuthatchery.pgf.util.HistoryList.Axioms;

public class HistoryListTest {
	static final Axioms<String> axioms = new Axioms<String>();
	static final String[] strings = new String[] { "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "" };
	private List<HistoryList<String>> lists;
	private Random random = new Random();


	@Test
	public void clearSize() {
		for(HistoryList<String> l : lists) {
			assertTrue(axioms.clearSize(l));
		}
	}


	@Test
	public void putGet() {
		for(HistoryList<String> l : lists) {
			assertTrue(axioms.putGet(l, randomString()));
		}
	}


	@Test
	public void putGetI() {
		for(HistoryList<String> l : lists) {
			assertTrue(axioms.putGetI(l, randomString(), random.nextInt(Integer.MAX_VALUE)));
		}
	}


	@Test
	public void putSize() {
		for(HistoryList<String> l : lists) {
			assertTrue(axioms.putSize(l, randomString()));
		}
	}


	public HistoryList<String> randomHistoryList() {
		HistoryList<String> list = new HistoryArrayList<>(random.nextInt(10) + 1);
		int n = random.nextInt(100);
		for(int i = 0; i < n; i++) {
			list.put(randomString());
		}
		return list;
	}


	@Before
	public void setup() {
		lists = new ArrayList<>();
		for(int i = 0; i < 10000; i++) {
			lists.add(randomHistoryList());
		}
	}


	private String randomString() {
		return strings[random.nextInt(strings.length)];
	}
}
