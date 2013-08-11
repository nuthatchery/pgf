package org.nuthatchery.pgf.processors;

import java.io.PrintWriter;
import org.nuthatchery.pgf.plumbing.PipeConnector;

/**
 * This printer copies its input to its output, while printing each item to a
 * PrintWriter (via toString()).
 * 
 * 
 * @param <T>
 */
public class Printer<T> extends ProcessorBase<T, T> {
	private final PrintWriter pw;
	private final String sep;
	private boolean first = true;


	public Printer(PrintWriter pw, String separator) {
		super();
		this.pw = pw;
		this.sep = separator;
	}


	@Override
	public boolean cfgUseEndMarker() {
		return true;
	}


	@Override
	public boolean process(PipeConnector<T, T> io) {
		if(!io.isAtEnd()) {
			T obj = io.get();
			if(obj == cfgEndMarker()) {
				pw.println();
				pw.flush();
				return true;
			}
			if(!first) {
				pw.print(sep);
			}
			else {
				first = false;
			}
			pw.print(obj.toString());
			io.put(obj);
		}
		if(io.isAtEnd()) {
			pw.println();
			pw.flush();
		}
		return true;
	}

}
