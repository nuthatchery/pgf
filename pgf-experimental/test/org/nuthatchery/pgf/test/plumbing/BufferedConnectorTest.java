package org.nuthatchery.pgf.test.plumbing;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.Before;
import org.junit.Test;
import org.nuthatchery.pgf.plumbing.ForwardPipe;
import org.nuthatchery.pgf.plumbing.PipeConnector;
import org.nuthatchery.pgf.plumbing.PipeConnector.Axioms;
import org.nuthatchery.pgf.plumbing.impl.BufferedConnector;
import org.nuthatchery.pgf.plumbing.impl.NullPipe;

public class BufferedConnectorTest {
	static final Axioms<String, String> axioms = new Axioms<String, String>();
	static final String[] strings = new String[] { "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "" };
	private List<PipeConnector<String, String>> conns;
	private Random random = new Random();


	@Test
	public void checkPut() {
		for(PipeConnector<String, String> c : conns) {
			// TODOD
		}
	}


	@Test
	public void emptySizeAhead() {
		for(PipeConnector<String, String> c : conns) {
			assertTrue(axioms.emptySizeAhead(c));
		}
	}


	@Test
	public void endEmpty() {
		for(PipeConnector<String, String> c : conns) {
			assertTrue(axioms.endEmpty(c));
		}
	}


	@Test
	public void getLookAhead1() {
		for(PipeConnector<String, String> c : conns) {
			assertTrue(axioms.getLookAhead1(c));
		}
	}


	@Test
	public void getLookAhead2() {
		for(PipeConnector<String, String> c : conns) {
			assertTrue(axioms.getLookAhead2(c, random.nextInt(Integer.MAX_VALUE)));
		}
	}


	@Test
	public void getSizeAhead() {
		for(PipeConnector<String, String> c : conns) {
			assertTrue(axioms.getSizeAhead(c));
		}
	}


	@Test
	public void putLookBehind1() {
		for(PipeConnector<String, String> c : conns) {
			assertTrue(axioms.putLookBehind1(c, randomString()));
		}
	}


	@Test
	public void putLookBehind2() {
		for(PipeConnector<String, String> c : conns) {
			assertTrue(axioms.putLookBehind2(c, randomString(), random.nextInt(Integer.MAX_VALUE)));
		}
	}


	@Test
	public void putSizeBehind() {
		for(PipeConnector<String, String> c : conns) {
			assertTrue(axioms.putSizeBehind(c, randomString()));
		}
	}


	public PipeConnector<String, String> randomPipeConnector() {
		BufferedConnector<String, String> conn = new BufferedConnector<String, String>(random.nextInt(16), Math.max(0, random.nextInt(10) - 2));

		if(random.nextInt(4) == 0) {
			conn.connect(new NullPipe<String>());
		}

		int n = random.nextInt(100);
		for(int i = 0; i < n; i++) {
			conn.add(randomString());
		}
		if(conn.sizeAhead() > 0) {
			n = random.nextInt(conn.sizeAhead());
			for(int i = 0; i < n; i++) {
				conn.get();
			}
		}

		n = Math.max(0, random.nextInt(10) - 5);
		for(int i = 0; i < n; i++) {
			conn.put(randomString());
		}

		return conn;
	}


	@Before
	public void setup() {
		conns = new ArrayList<>();
		for(int i = 0; i < 10000; i++) {
			conns.add(randomPipeConnector());
		}
	}


	private String randomString() {
		return strings[random.nextInt(strings.length)];
	}
}
