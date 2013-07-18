module org::nuthatchery::pgf::rascal::AstToStream

import ParseTree;
import org::nuthatchery::pgf::rascal::Token;
import org::nuthatchery::pgf::rascal::Tseq;
import org::nuthatchery::pica::terms::Terms;
data AST;

alias PpFun = Tseq(AST, Tseq);

public Tseq ast2stream(Tseq stream, PpFun pp, value trees...) {
	for(tree <- trees) {
		switch(tree) {
			case AST a:
				stream = pp(a, stream);
			case Token t:
                        	stream = tseqPushR(stream, t);
			case Tseq(Tseq, PpFun) f:
				stream = f(stream, pp);
			default:
				throw "ast2Stream: unknown tree type: <tree>";
		}
	}
	return stream;
}


public Tseq(Tseq, PpFun) SepBy(AST tree, Token sepToks...) {
	return Tseq(Tseq stream, Tseq(AST, Tseq) ppf) {	
		bool first = true;
		for(AST t <- tree.args) {
			if(first)
				first = false;
			else
				for(tok <- sepToks)
                                	stream = tseqPushR(stream, tok);
			stream = ppf(t, stream);
		}
		return stream;
	};
}