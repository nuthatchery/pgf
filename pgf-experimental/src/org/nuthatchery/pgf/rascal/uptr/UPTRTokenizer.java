package org.nuthatchery.pgf.rascal.uptr;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.IList;
import org.eclipse.imp.pdb.facts.ISourceLocation;
import org.eclipse.imp.pdb.facts.IValue;
import org.nuthatchery.pgf.config.TokenizerConfig;
import org.nuthatchery.pgf.plumbing.ForwardStream;
import org.nuthatchery.pgf.tokens.Category;
import org.nuthatchery.pgf.tokens.CategoryStore;
import org.nuthatchery.pgf.tokens.CtrlToken;
import org.nuthatchery.pgf.tokens.DataToken;
import org.nuthatchery.pgf.tokens.Token;
import org.nuthatchery.pgf.tokens.TokenizerHelper;
import org.rascalmpl.values.uptr.ITree;
import org.rascalmpl.values.uptr.ProductionAdapter;
import org.rascalmpl.values.uptr.SymbolAdapter;
import org.rascalmpl.values.uptr.TreeAdapter;
import org.rascalmpl.values.uptr.visitors.TreeVisitor;

public class UPTRTokenizer {

	class Visitor extends TreeVisitor<RuntimeException> {
		ForwardStream<Token> output;


		@Override
		public ITree visitTreeAmb(ITree arg) throws RuntimeException {
			((IConstructor) TreeAdapter.getAlternatives(arg).iterator().next()).accept(this);

			return null;
		}


		@Override
		public ITree visitTreeAppl(ITree arg) throws RuntimeException {
			IConstructor prod = TreeAdapter.getProduction(arg);
			IConstructor sym = ProductionAdapter.getType(prod);
			String sort = ProductionAdapter.getSortName(prod);
			if(SymbolAdapter.isAnyList(sym)) {
				IConstructor listSym = SymbolAdapter.getSymbol(sym);
				if(SymbolAdapter.isSort(listSym) || SymbolAdapter.isLex(listSym) || SymbolAdapter.isLayouts(listSym) || SymbolAdapter.isParameterizedSort(listSym) || SymbolAdapter.isKeyword(listSym)) {
					sort = SymbolAdapter.getName(listSym) + "*";
				}
			}
			ISourceLocation loc = TreeAdapter.getLocation(arg);

			sym = SymbolAdapter.delabel(sym);

			if(SymbolAdapter.isKeyword(sym)) {
				output.put(new DataToken(TreeAdapter.yield(arg), TXT));
				return null;
			}
			else if(SymbolAdapter.isLiteral(sym) || SymbolAdapter.isCILiteral(sym)) {
				String s = TreeAdapter.yield(arg);
				output.put(new DataToken(s, config.getCatForLiteral(s)));
				return null;
			}
			else if(SymbolAdapter.isLex(sym)) {
				String s = TreeAdapter.yield(arg);
				output.put(new DataToken(s, config.getCatForLexical(s)));
				return null;
			}
			else if(TreeAdapter.isComment(arg)) {
				String s = TreeAdapter.yield(arg);
				output.put(new DataToken(s, TXT));
				return null;
			}
			else if(SymbolAdapter.isLayouts(sym)) {
				TokenizerHelper.splitComment(TreeAdapter.yield(arg), output, config);
				return null;
			}

			boolean nest = config.cfgNestSorts().contains(sort);
			if(nest) {
				output.put(new CtrlToken(config.cfgCatForCtrlBegin()));
			}

			IList children = (IList) arg.get("args");
			for(IValue child : children) {
				child.accept(this);
			}
			if(nest) {
				output.put(new CtrlToken(config.cfgCatForCtrlEnd()));
			}

			return null;
		}


		@Override
		public ITree visitTreeChar(ITree arg) throws RuntimeException {
			output.put(new DataToken(TreeAdapter.yield(arg), null));
			return null;
		}


		@Override
		public ITree visitTreeCycle(ITree arg) throws RuntimeException {
			return null;
		}


	}

	protected final CategoryStore categories;
	protected final Category HSPC;
	protected final Category VSPC;
	protected final Category TXT;


	protected final TokenizerConfig config;


	public UPTRTokenizer(TokenizerConfig config) {
		this.config = config;
		this.categories = config.cfgCategories();
		this.HSPC = config.cfgCatHorizSpace();
		this.VSPC = config.cfgCatVertSpace();
		this.TXT = config.cfgCatText();
	}


	public void tokenize(IConstructor parseTree, ForwardStream<Token> output) {
		Visitor visitor = new Visitor();
		visitor.output = output;
		parseTree.accept(visitor);
	}
}
