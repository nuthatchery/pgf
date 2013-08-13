package org    . nuthatchery.pgf.javaformatter;

import java.io.IOException;
import java.io.InputStream;

import org.spoofax.jsglr.client.InvalidParseTableException;
import org.spoofax.jsglr.shared.SGLRException;
import org.spoofax.terms.ParseError;

import nuthatch.javafront.JavaParser;
import nuthatch.stratego.adapter.STermCursor;

public class JavaFormatter {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws SGLRException 
	 * @throws InvalidParseTableException 
	 * @throws ParseError 
	 */
	public static void main(String[] args) throws SGLRException, IOException, ParseError, InvalidParseTableException {
		// TODO Auto-generated method stub
		
		JavaParser.init();
		InputStream input=JavaFormatter.class.getResourceAsStream("JavaFormatter.java");
		STermCursor cursor=JavaParser.parseStreamToAsfix(input, "JavaFormatter.java");
		new AsfixTokenizer(   null ) . tokenize(cursor,null);
		
	}

}
