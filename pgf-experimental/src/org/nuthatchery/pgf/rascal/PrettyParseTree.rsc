module org::nuthatchery::pgf::rascal::PrettyParseTree

import ParseTree;
import String;
import org::nuthatchery::pgf::rascal::Token;
import org::nuthatchery::pgf::rascal::Tseq;
import org::nuthatchery::pgf::rascal::TableBuilder;

 public bool makeCategories = false;

public Tseq prettyParseTree(Tseq outStream, Tree t) {
	void addLex(Tree a) { outStream = put(Text(unparse(a)), outStream); }
	void addLit(Tree l) { outStream = put(catLit(Text(unparse(l))), outStream); }
	void addLayouts(Tree l) {
		s = unparse(l);
		if(s != "")
			outStream = put(Space(unparse(l)), outStream);
	}
	void nest(str gr, list[Tree] as) {
		outStream = put(Nest(LvInc(0)), outStream);
		for(a <- as) {
			outStream = prettyParseTree(outStream, a);
		}
		outStream = put(Nest(LvPop()), outStream);
	}
		
	top-down-break visit(t) {
		case appl(prod(sort("Expr"),_,_), as):
			nest("Expr", as);
		case a:appl(prod(lex(_),_,_), _):
			addLex(a);
		case a:appl(prod(label(_, lex(_)),_,_), _):
			addLex(a);
		case l:appl(prod(layouts(_),_,_), _):
			addLayouts(l); 
		case l:appl(prod(label(_, layouts(_)),_,_), _):
			addLayouts(l);
		case l:appl(prod(lit(_),_,_), _):
			addLit(l);
		case l:appl(prod(label(_,lit(_)),_,_), _):
			addLit(l);
		case l:appl(prod(cilit(_),_,_), _):
			addLit(l);
		case l:appl(prod(label(_,cilit(_)),_,_), _):
			addLit(l);
		case c:char(_):
			outStream = put(Text("\'<unparse(c)>\'"), outStream);
	}
	return outStream;
}

public Token catLit(t:Text(s)) {
	switch(s) {
		case "(":	return t[@cat="LPAREN"];
		case ")":	return t[@cat="RPAREN"];
		case "[":	return t[@cat="LBRACKET"];
		case "]":	return t[@cat="RBRACKET"];
		case "{":	return t[@cat="LBRACE"];
		case "}":	return t[@cat="RBRACE"];
		case ",":	return t[@cat="COMMA"];
		case ";":	return t[@cat="SEMICOLON"];
		case ":":	return t[@cat="COLON"];
		case ".":	return t[@cat="DOT"];
	}
	if(s in ["+","-","*","/","%","=","\<=","\>=","\<","\>","&&","||","^^","&","|","^","\<\<","\>\>"])
		return t[@cat="BINOP"];
	else if(s in ["!","~"])
		return t[@cat="PREOP"];
	else if(/^[a-zA-Z]+$/ := s) {
		if(makeCategories)
			return t[@cat=toUpperCase(s)];
		else		
			return t[@cat="KEYWORD"];
	}
	return t;
}

