module org::nuthatchery::pgf::rascal::tests::SimpleExprLang

lexical Whitespace = [\ \t\n\f\r]; 

lexical Comment = "//" ![\n]* $;

layout Standard 
  = WhitespaceOrComment* !>> [\ \t\n\f\r] !>> "//";
  
syntax WhitespaceOrComment 
  = whitespace: Whitespace
  | comment: Comment
  ; 
  
lexical Id = @TokenCat="ID" Id: [a-z A-Z 0-9 _] !<< [a-z A-Z][a-z A-Z 0-9 _]* !>> [a-z A-Z 0-9 _]
          ;

lexical Num = [0-9] !<< [0-9]+ !>> [0-9];

syntax Expr
	= Id
	| Num
	| "(" Expr ")"
	| Id "(" { Expr "," }* ")"
	| "{" Expr* "}"
	| "!" Expr
	> left (
		Expr "*" Expr
	  | Expr "/" Expr
	  )
	> left (
		Expr "+" Expr
	  | Expr "-" Expr
	  )
	;

syntax Stmt
	= "if" "(" Expr ")" Stmt
	| "if" "(" Expr ")" Stmt "else" Stmt
	| Id "=" Expr ";"
	| "{" Stmt* "}"
	;