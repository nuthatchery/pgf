// run with rascal-src
module org::nuthatchery::pgf::rascal::FmtUtilC

import org::nuthatchery::pgf::rascal::engines::FmtEngine;
import org::nuthatchery::pgf::rascal::FmtUtil;
import org::nuthatchery::pgf::rascal::Token;
import org::nuthatchery::pgf::rascal::Tseq;

import IO;
import List;

/** Separates passed elements by commas.
    Useful for pretty-printing function argument lists and such
    E.g. ["x", "y", "z"] -> "x, y, z".
*/
public Tseq commaSep(list[value] elems) {
  return sepBy(tseq(",", sp), elems);
}

/** Stacks declarations vertically, putting empty lines in between. 
 */
public Tseq stackDecls(list[value] elems) {
  return sepBy(tseq(br, br), elems);
}

/** Groups a list so that either all elements are on the same line, or
    each is on a separate line.

    ["x", "y", "z"] => "{ x, y, z }"
    ["long_x", "long_y", "long_z"] => "{ long_x, 
                                         long_y, 
                                         long_z }"
*/
public Tseq groupList(list[value] elems) {
  return group(tseq("{ ", align, sepBy(tseq(",", br), elems), dedent, " }"));
}

/** Formats argument list, aligned after open paren.
    
    ["x + y", "z", "true"] => "(x + y, z,
                                true)"
*/
public Tseq argList(list[value] elems) {
  return tseq("(", align, commaSep(elems), dedent, ")");
}

/** A block of something, indented.

    block("int x;") => "{
                          int x;
                        }"
    block(tseqNull) => "{
                        }"
 */
public Tseq block(value x) {
  ts = toTokenStream(x);
  if (atEof(ts))
    return tseq("{", br, "}");
  return tseq("{", indent(2), br, ts, dedent, br, "}");
}

/** A block of declarations. Supports special Label token for exdented
    labels such as "public:" and "private:". (See 'stmtBlock' for a
    similar example.)
 */
public Tseq declBlock(list[value] elems) {
  r = tseq("{", indent(2));
  bool wasLabel = true;
  for (x <- elems) {
    if (Label(s) := x) {
      if (!wasLabel)
        r = put(br, r);
      r = tseq(r, tseq(exdent(2), br, Text(s), dedent));
      wasLabel = true;
    } else {
      if (wasLabel)
        r = tseq(r, tseq(br, x));
      else
        r = tseq(r, tseq(br, br, x));
      wasLabel = false;
    }
  }
  r = tseq(r, tseq(dedent, br, "}"));
  return r;
}

/** A block of statements. Supports special Label token for exdented
    labels.

    stmtBlock(["int foo = 0;", Label("inc:"), "foo += 1;", 
               "goto inc;", "return foo;"]) 
    =>
             "{
                int foo = 0;
              inc:
                foo += 1;
                goto inc;
                return foo;
              }"
 */
public Tseq stmtBlock(list[value] elems) {
  r = tseq("{", indent(2));
  for (x <- elems) {
    if (Label(s) := x) {
      r = tseq(r, tseq(exdent(2), br, Text(s), dedent));
    } else {
      r = tseq(r, tseq(br, x));
    }
  }
  r = tseq(r, tseq(dedent, br, "}"));
  return r;
}

/** Unary operation.

    ppUnaryOp("-", "x") => "-x"
*/
public Tseq ppUnaryOp(value op, value x) {
  return tseq(op, x);
}

/** Binary operation.

    ppBinaryOp("-", "x", "y") => "x - y"
*/
public Tseq ppBinaryOp(value op, value x, value y) {
  return tseq(x, nbsp, op, sp, y);
}

/** Puts parentheses around an expression.
 */
public Tseq inParens(value x) {
  return tseq("(", x, ")");
}

public void main(list[str] args) {
  println(commaSep(["x", "y", "z"]));
}
