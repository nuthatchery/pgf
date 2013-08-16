package org.nuthatchery.pgf.javaformatter;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import nuthatch.javafront.JavaParser;
import nuthatch.stratego.adapter.STermCursor;

import org.nuthatchery.pgf.plumbing.ForwardPipe;
import org.nuthatchery.pgf.plumbing.impl.BufferedSyncPipeComponent;
import org.nuthatchery.pgf.processors.CopyProcessor;
import org.nuthatchery.pgf.rascal.uptr.TokenizerConfigBase;
import org.nuthatchery.pgf.tokens.CategoryStore;
import org.nuthatchery.pgf.tokens.Token;
import org.nuthatchery.pgf.tokens.TokensToString;
import org.spoofax.jsglr.client.InvalidParseTableException;
import org.spoofax.jsglr.shared.SGLRException;
import org.spoofax.terms.ParseError;

public class JavaFormatter {

	/**
	 * @param args
	 * @throws IOException
	 * @throws SGLRException
	 * @throws InvalidParseTableException
	 * @throws ParseError
	 */
	public static void main(String[] args) throws SGLRException, IOException, ParseError, InvalidParseTableException {
		boolean profile = false;
		long t = System.currentTimeMillis();
		JavaParser.init();
		STermCursor smallCursor = JavaParser.parseStreamToAsfix(JavaFormatter.class.getResourceAsStream("JavaFormatter.j"), "JavaFormatter.java");
		System.out.println("Parsing done, in " + (System.currentTimeMillis() - t) + "ms");

		t = System.currentTimeMillis();
		JavaParser.init();
		STermCursor bigCursor = null;
		if(profile) {
			bigCursor = JavaParser.parseStreamToAsfix(JavaFormatter.class.getResourceAsStream("RascalParser.java.ex"), "RascalParser.java.ex");
		}
		System.out.println("Parsing done, in " + (System.currentTimeMillis() - t) + "ms");
		System.out.print("Press ENTER when ready...");
		if(profile) {
			System.in.read();
		}

		System.out.println("Starting the real stuff!");
		System.out.println("");
		System.out.println("");
		format(smallCursor);
		if(profile) {
			format(bigCursor);
		}

	}


	static void format(STermCursor input) throws FileNotFoundException {
		// TODO Auto-generated method stub

		Config config = new Config();

		AsFix2Tokenizer tokenizer = new AsFix2Tokenizer(config);

		final ForwardPipe<Token, Token> output = new BufferedSyncPipeComponent<>(new CopyProcessor<Token>()); //new Printer<Token>(new PrintWriter(System.out), " "));
		ForwardPipe<Token, Token> next = output;

		next = next.connect(new BufferedSyncPipeComponent<>(new JavaSpacing(config.cfgCategories())));
		//next = next.connect(new BufferedSyncPipeComponent<>(new Indenter(config.cfgCategories())));
		TokensToString tokensToString = new TokensToString();
		//next = next.connect(new BufferedSyncPipeComponent<>(new Printer<Token>(new PrintWriter(System.err), " ")));
		next = next.connect(new BufferedSyncPipeComponent<>(tokensToString));

		// output.connect(new BufferedSyncPipeComponent<>(respacer)).connect(new BufferedSyncPipeComponent<>(indenter)).connect(new BufferedSyncPipeComponent<>(new Printer<Token>(new PrintWriter(System.err), " "))).connect(new BufferedSyncPipeComponent<>(tokensToString));
		long t = System.currentTimeMillis();
		tokenizer.tokenize(input, output); // new NullPipe<Token>());
		output.end();
		String result = tokensToString.toString();
		System.out.println("Formatting done, in " + (System.currentTimeMillis() - t) + "ms");
		System.out.println("RESULT: ");
		if(result.length() > 4096) {
			try (PrintWriter writer = new PrintWriter(new FileOutputStream("/tmp/result.java"))) {
				writer.print(result);
			}
			System.out.println("...in /tmp/result.java");
		}
		else {
			System.out.println(result);
		}
		System.out.println("***");
	}


	static class Config extends TokenizerConfigBase {
		Set<String> nestSorts = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList("Expr", "Stat", "Decl*")));


		@Override
		public Set<String> cfgNestSorts() {
			return nestSorts;
		}


		@Override
		protected String cfgIdentifierRegex() {
			return DEFAULT_ID_REGEX;
		}


		@Override
		protected CatSet cfgInitialCategorySet() {
			return CatSet.CATSET_DEFAULT;
		}


		@Override
		protected String cfgKeywordRegex() {
			return DEFAULT_KW_REGEX;
		}


		@Override
		protected Collection<String> cfgKeywords() {
			return Arrays.asList();
		}


		@Override
		protected boolean cfgUseDefaultLiteralClassification() {
			return true;
		}


		@Override
		protected void moreCategories(CategoryStore store) {
			store.declare("EQ", "BINOP", "PUNCT");
			store.declare("DQT", "PUNCT");
			store.declare("SQT", "PUNCT");
			addLitString("=", "EQ");
			addLitString("\"", "DQT");
			addLitString("\'", "SQT");
		}

	}
}
