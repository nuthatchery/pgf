package org.nuthatchery.pgf.rascal.uptr;

import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.IList;
import org.eclipse.imp.pdb.facts.IValue;
import org.nuthatchery.pgf.plumbing.ForwardStream;
import org.nuthatchery.pgf.tokens.TextToken;
import org.nuthatchery.pgf.tokens.Token;
import org.rascalmpl.values.uptr.ProductionAdapter;
import org.rascalmpl.values.uptr.SymbolAdapter;
import org.rascalmpl.values.uptr.TreeAdapter;
import org.rascalmpl.values.uptr.visitors.TreeVisitor;

public class UPTRTokenizer {

	class Visitor extends TreeVisitor<Throwable> {
		ForwardStream<Token> output;


		@Override
		public IConstructor visitTreeAmb(IConstructor arg) throws Throwable {
			((IConstructor) TreeAdapter.getAlternatives(arg).iterator().next()).accept(this);

			return null;
		}


		@Override
		public IConstructor visitTreeAppl(IConstructor arg) throws Throwable {
			IConstructor prod = TreeAdapter.getProduction(arg);
			IConstructor sym = ProductionAdapter.getType(prod);
			String sort = ProductionAdapter.getSortName(prod);

			sym = SymbolAdapter.delabel(sym);

			if(SymbolAdapter.isLex(sym)) {
				output.put(new TextToken(TreeAdapter.yield(arg), null));
			}
			else if(SymbolAdapter.isLiteral(sym) || SymbolAdapter.isCILiteral(sym)) {
				output.put(new TextToken(TreeAdapter.yield(arg), null));
			}
			else if(SymbolAdapter.isKeyword(sym)) {
				output.put(new TextToken(TreeAdapter.yield(arg), null));
			}
			else if(SymbolAdapter.isLayouts(sym)) {
				output.put(new TextToken(TreeAdapter.yield(arg), null));
			}
			else if(TreeAdapter.isComment(arg)) {
				;
			}

			IList children = (IList) arg.get("args");
			for(IValue child : children) {
				child.accept(this);
			}
			return null;
		}


		@Override
		public IConstructor visitTreeChar(IConstructor arg) throws Throwable {
			output.put(new TextToken(TreeAdapter.yield(arg), null));
			return null;
		}


		@Override
		public IConstructor visitTreeCycle(IConstructor arg) throws Throwable {
			return null;
		}

	}
}
