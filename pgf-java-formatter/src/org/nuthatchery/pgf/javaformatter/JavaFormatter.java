package org.nuthatchery.pgf.javaformatter;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import nuthatch.javafront.JavaParser;
import nuthatch.stratego.adapter.STermCursor;

import org.nuthatchery.pgf.examples.SensibleSpacing;
import org.nuthatchery.pgf.plumbing.ForwardPipe;
import org.nuthatchery.pgf.plumbing.impl.BufferedSyncPipeComponent;
import org.nuthatchery.pgf.processors.Printer;
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
		// TODO Auto-generated method stub

		JavaParser.init();
		InputStream input = JavaFormatter.class.getResourceAsStream("JavaFormatter.j");
		STermCursor cursor = JavaParser.parseStreamToAsfix(input, "JavaFormatter.java");

		Config config = new Config();
		AsFix2Tokenizer tokenizer = new AsFix2Tokenizer(config);
		final ForwardPipe<Token, Token> output = new BufferedSyncPipeComponent<>(new Printer<Token>(new PrintWriter(System.out), " "));
		ForwardPipe<Token, Token> next = output;

		next = next.connect(new BufferedSyncPipeComponent<>(new SensibleSpacing(config.cfgCategories())));
		//next = next.connect(new BufferedSyncPipeComponent<>(new Indenter(config.cfgCategories())));
		TokensToString tokensToString = new TokensToString();
		next = next.connect(new BufferedSyncPipeComponent<>(new Printer<Token>(new PrintWriter(System.err), " ")));
		next = next.connect(new BufferedSyncPipeComponent<>(tokensToString));

		// output.connect(new BufferedSyncPipeComponent<>(respacer)).connect(new BufferedSyncPipeComponent<>(indenter)).connect(new BufferedSyncPipeComponent<>(new Printer<Token>(new PrintWriter(System.err), " "))).connect(new BufferedSyncPipeComponent<>(tokensToString));
		tokenizer.tokenize(cursor, output);
		output.end();
		System.out.println();
		System.out.println("RESULT: ");
		System.out.println(tokensToString.toString());
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
