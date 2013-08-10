package org.nuthatchery.pgf.examples;

public class Respacer {
	/*
	  define respacer = {
	 
			  ^ @ * => nop;
			  * @ $ => nop;
			  * @ SPC => delete;
			  * @ * => insert " ":SPC;
			}
			
	  ^ . e => e;
	  e . $ => e;
	  
	  
	  SPC => delete;
	  
	*/
	Foo definition = new Foo[]{
			addRule(START, nop);
			_.at(End).then(nop),
			at(SPC).then(delete),
			otherwise(insert(SPC))
			
			addRule(after(START).at(any).then(copy);
			addRule(at(END).then(copy);
			addRule(at(SPACE).then(delete);
			addRule(at(any).then(insert(" ", "SPC")));
	}
		
}
