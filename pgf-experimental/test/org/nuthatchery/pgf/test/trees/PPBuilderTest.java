package org.nuthatchery.pgf.test.trees;

import java.util.Collection;

import nuthatch.examples.ExampleExpr;
import nuthatch.examples.xmpllang.Expr;
import nuthatch.examples.xmpllang.Stat;
import nuthatch.examples.xmpllang.Type;
import nuthatch.examples.xmpllang.XmplNode;
import nuthatch.examples.xmpllang.full.XmplActionFactory;
import nuthatch.examples.xmpllang.full.XmplCursor;
import nuthatch.examples.xmpllang.full.XmplPatterns;
import nuthatch.examples.xmpllang.full.XmplWalker;
import static org.junit.Assert.*;
import static nuthatch.examples.xmpllang.expronly.ExprPatterns.Add;
import static nuthatch.examples.xmpllang.expronly.ExprPatterns.Int;
import static nuthatch.examples.xmpllang.expronly.ExprPatterns.Let;
import static nuthatch.examples.xmpllang.expronly.ExprPatterns.Mul;
import static nuthatch.examples.xmpllang.expronly.ExprPatterns.Var;
import static nuthatch.examples.xmpllang.full.XmplPatterns.*;

import org.junit.Before;
import org.junit.Test;
import org.nuthatchery.pgf.config.TokenizerConfig;
import org.nuthatchery.pgf.config.TokenizerConfigBase;
import org.nuthatchery.pgf.tokens.CategoryStore;
import org.nuthatchery.pgf.trees.PPBuilder;
import org.nuthatchery.pgf.trees.PrettyPrinter;

public class PPBuilderTest {
	PPBuilder<XmplNode, Type, XmplCursor, XmplWalker> builder1;
	PPBuilder<XmplNode, Type, XmplCursor, XmplWalker> builder2;
	PrettyPrinter<XmplNode, Type, XmplCursor, XmplWalker> printer;

	static final String x = "x";
	static final String y = "y";
	static final Expr expr1 = Add(Int(5), Mul(Add(Int(7), Int(3)), Int(4)));
	static final Expr expr2 = Let(Var(x), Int(4), Add(Var(x), Var(x)));
	static final Expr expr3 = Let(Var(x), Int(3), Let(Var(y), Add(Var(x), Mul(Add(Int(7), Int(3)), Int(4))), Add(Var(x), Var(y))));
	static final Expr expr4 = Let(Var(y), Int(0), Let(Var(x), Int(3), Let(Var(y), Int(4), Add(Var(x), Var(y)))));
	static final Expr expr5 = Let(Var(y), Int(0), Var(y));
	static final Expr expr6 = Let(Var(x), Int(1), Let(Var(x), Var(x), Var(x)));
	static final Stat stat1 = Nop();
	static final Stat stat2 = Seq(Assign(Var("x"), Int(0)), Nop(), Nop());
	static final Stat stat3 = If(Int(1), Assign(Var("x"), Int(2)), Assign(Var("x"), Int(3)));
	static final Stat stat4 = Declare(Var("i"), Int(10), //
			While(Var("i"), Assign(Var("i"), Add(Var("i"), Int(-1)))));
	static final TokenizerConfig config = new TokenizerConfigBase() {

		@Override
		protected String cfgIdentifierRegex() {
			// TODO Auto-generated method stub
			return null;
		}


		@Override
		protected CatSet cfgInitialCategorySet() {
			return CatSet.CATSET_DEFAULT;
		}


		@Override
		protected String cfgKeywordRegex() {
			// TODO Auto-generated method stub
			return null;
		}


		@Override
		protected Collection<String> cfgKeywords() {
			// TODO Auto-generated method stub
			return null;
		}


		@Override
		protected boolean cfgUseDefaultLiteralClassification() {
			return true;
		}


		@Override
		protected void moreCategories(CategoryStore store) {
			// TODO Auto-generated method stub

		}
	};


	public PrettyPrinter<XmplNode, Type, XmplCursor, XmplWalker> exprPP() {
		PPBuilder<XmplNode, Type, XmplCursor, XmplWalker> builder = new PPBuilder<>(config, XmplActionFactory.exprBuildContext);
		builder.addTmpl(Add(_, _), "+ ( <1> , <2> )");
		builder.addTmpl(Assign(_, _), "<1>  =  <2>");
		builder.addTmpl(Declare(_, _, _), "let  <1>  =  <2>  in  <3>  endlet");
		builder.addTmpl(If(_, _, _), "if  <1>  then  <2>  else  <3>  endif");
		builder.addTmpl(Int(_), "<str(0,TXT)>");
		builder.addTmpl(Let(_, _, _), "let  <1>  =  <2>  in  <3>  endlet");
		builder.addTmpl(Mul(_, _), "* ( <1> , <2> )");
		builder.addTmpl(tree(Nop()), ";");
		builder.addTmpl(Seq(_, _), "<1> ; <2>");
		builder.addTmpl(While(_, _), "while  <1>  do  <2>  end");
		builder.addTmpl(Var(_), "<str(0,TXT)>");
		builder.compile();

		return builder.compile();

	}


	@Before
	public void setup() {
		builder1 = new PPBuilder<>(config, XmplActionFactory.exprBuildContext);
		builder2 = new PPBuilder<>(config, XmplActionFactory.exprBuildContext);
		printer = exprPP();
	}


	@Test
	public void testDecoding1() {
		builder1.addList(_, "if", 0, "then", 1, "else", 2, "end");
		builder2.addTmpl(_, "if <0> then <1> else <2> end");

		assertEquals(builder1, builder2);
	}


	@Test
	public void testDecoding2() {
		builder1.addList(_, "foo", PPBuilder.space(" "), "bar");
		builder2.addTmpl(_, "foo  bar");

		assertEquals(builder1, builder2);
	}


	@Test
	public void testDecoding3() {
		builder1.addList(_, "f", "(", PPBuilder.sepBy(0, ","), ")");
		builder2.addTmpl(_, "f ( <sepBy(0, ',')> )");

		assertEquals(builder1, builder2);
	}


	@Test
	public void testDecoding4() {
		builder1.addList(_, "f", "(", PPBuilder.sepBy(0, ","), ")");
		builder2.addTmpl(_, "f ( <commaSep(0)> )");

		assertEquals(builder1, builder2);
	}


	@Test
	public void xmplTest() {
		PrettyPrinter<XmplNode, Type, XmplCursor, XmplWalker> exprPP = exprPP();
		exprPP.print(new XmplCursor(ExampleExpr.expr1), System.out);
		assertTrue(true);

	}


	@Test
	public void xmplTestExpr1() {
		assertEquals("+(5,*(+(7,3),4))", printer.toString(new XmplCursor(expr1)));
	}


	@Test
	public void xmplTestExpr2() {
		assertEquals("let x = 4 in +(x,x) endlet", printer.toString(new XmplCursor(expr2)));
	}


	@Test
	public void xmplTestExpr3() {
		assertEquals("let x = 3 in let y = +(x,*(+(7,3),4)) in +(x,y) endlet endlet", printer.toString(new XmplCursor(expr3)));
	}


	@Test
	public void xmplTestExpr4() {
		assertEquals("let y = 0 in let x = 3 in let y = 4 in +(x,y) endlet endlet endlet", printer.toString(new XmplCursor(expr4)));
	}


	@Test
	public void xmplTestExpr5() {
		assertEquals("let y = 0 in y endlet", printer.toString(new XmplCursor(expr5)));
	}


	@Test
	public void xmplTestExpr6() {
		assertEquals("let x = 1 in let x = x in x endlet endlet", printer.toString(new XmplCursor(expr6)));
	}


	@Test
	public void xmplTestStat1() {
		assertEquals(";", printer.toString(new XmplCursor(stat1)));
	}


	@Test
	public void xmplTestStat2() {
		assertEquals("x = 0;;", printer.toString(new XmplCursor(stat2)));
	}


	@Test
	public void xmplTestStat3() {
		assertEquals("if 1 then x = 2 else x = 3 endif", printer.toString(new XmplCursor(stat3)));
	}


	@Test
	public void xmplTestStat4() {
		assertEquals("let i = 10 in while i do i = +(i,-1) end endlet", printer.toString(new XmplCursor(stat4)));
	}


}
