package org.nuthatchery.pgf.tokens;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nuthatchery.pgf.plumbing.ForwardStream;
import org.nuthatchery.pgf.rascal.uptr.TokenizerConfig;

public class TokenizerHelper {
	public static final Pattern patLayout = Pattern.compile("^(\\s*)(.*\\S)(\\s*)$", Pattern.DOTALL);


	public static void splitComment(String s, ForwardStream<Token> output, TokenizerConfig config) {
		Matcher matcher = patLayout.matcher(s);
		if(matcher.matches()) {
			String t = matcher.group(1);
			if(!t.equals("")) {
				splitLines(t, config.cfgCatHorizSpace(), output, config);
			}
			t = matcher.group(2);
			if(!t.equals("")) {
				splitLines(t, config.cfgCatComment(), output, config);
			}
			t = matcher.group(3);
			if(!t.equals("")) {
				splitLines(t, config.cfgCatHorizSpace(), output, config);
			}
		}
		else if(!s.equals("")) {
			splitLines(s, config.cfgCatHorizSpace(), output, config);
		}
	}


	public static void splitLines(String str, Category cat, ForwardStream<Token> output, TokenizerConfig config) {
		String[] split = str.split("\n|\f", -1);
		boolean first = true;
		for(String s : split) {
			if(!first) {
				output.put(new DataToken("\n", config.cfgCatVertSpace()));
			}
			if(!s.equals("")) {
				output.put(new DataToken(s, cat));
			}
			first = false;
		}
	}
}
