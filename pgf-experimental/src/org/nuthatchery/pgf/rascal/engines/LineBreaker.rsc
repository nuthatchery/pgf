module org::nuthatchery::pgf::rascal::engines::LineBreaker

import org::nuthatchery::pgf::rascal::ListOp;
import org::nuthatchery::pgf::rascal::Token;
import org::nuthatchery::pgf::rascal::Tseq;
import org::nuthatchery::pgf::rascal::TableBuilder;
import org::nuthatchery::pgf::rascal::engines::FmtEngine;

import IO;
import List;
import String;

bool debug = false;
public real initialBreakability = 1.0;
public real nestFactor = 0.75;

public Tseq breakLines(int width, Tseq inToks, Tseq outToks) {
	return breakLines(width, inToks, outToks, true);
}

public Tseq breakLines(int width, Tseq inToks, Tseq outToks, bool useBreakability) {
	real breakability = initialBreakability;
	list[int] indentStack = [0];
	Tseq queue = tseqNull;
	real bestBreak = 0.0;
	Token bestBreakSpace = Space("");
	int hpos = 0, qwidth = 0;  // horiz pos, and width of queue 	
	while (true) {
    	<tok, inToks> = tseqGet(inToks);
		switch(tok) {
			case Text(t): {
				qwidth = qwidth + size(t);
				queue = put(tok, queue);
			}
			case Space(s): {
				bool brk;
				lastBestBreak = bestBreak;
				breakiness = checkBreakiness((1.0*(hpos+qwidth))/width, breakability);
				if(debug) println("space: \"<s>\" at <hpos+qwidth+1>, value <bestBreak> (breakablity <breakability>)");
				if(breakiness >= bestBreak || !useBreakability) { // good place to break, let's commit
					bestBreak = breakiness;
					if(bestBreakSpace != Space(""))
						outToks = put(bestBreakSpace, outToks);
					if(debug) println("Committing <queue>");
					outToks = tseqAppend(outToks, queue);
					hpos = hpos + qwidth;
					queue = tseqNull;
					qwidth = 0;
					bestBreakSpace = tok; // this space should only be output if we don't break here
				}
				else {
					queue = put(tok, queue);
				}
				qwidth = qwidth + size(s);
			}
			case Line(): {
				if(bestBreakSpace != Space(""))
					outToks = put(bestBreakSpace, outToks);
				outToks = tseqAppend(outToks, queue);
				queue = tseqNull;
				qwidth = 0;
				bestBreak = 0.0;
				<outToks, hpos> = doBreak(indentStack[0], outToks);
				continue;
			}
			case Nest(LvPop()): {
				if(useBreakability)
					breakability = breakability / nestFactor;
				indentStack = indentStack[1..];
			}
			case Nest(lv): {
				switch(lv) {
					case LvInc(n): indentStack = [indentStack[0]+n, *indentStack];
					case LvRel(n): indentStack = [hpos+n, *indentStack];
					case LvAbs(n): indentStack = [n, *indentStack];
				}
				if(useBreakability)
					breakability = breakability * nestFactor;
			}
            case Eof():
                break;
			default:
				print(" [<tok>]:<getCategory(tok)> ");
		}
		if(hpos + qwidth >= width) { // time to break
			if(debug) println("breaking, hpos: <hpos+1>, qwidth: <qwidth>");
			bestBreakSpace = Space("");
			bestBreak = 0.0;
			<outToks, hpos> = doBreak(indentStack[0], outToks);
			if(debug) println(queue);
			outToks = tseqAppend(outToks, queue);
			hpos = hpos + qwidth;
			queue = tseqNull;
			qwidth = 0;
			// this leaves us with a queue of stuff for the next line
		}
	}
	if(bestBreakSpace != Space(""))
		outToks = put(bestBreakSpace, outToks);
	outToks = tseqAppend(outToks, queue);
	return outToks;
}

@doc{
Check the breakiness of this position.
The result indicates how desirable it is to break here. Higher is better.} 
real checkBreakiness(real posFrac, real breakability) {
	return posFrac * breakability * breakability;
}
tuple[Tseq, int] doBreak(int indent, Tseq outToks) {
	outToks = put(Line(), outToks);
	s = "";
	for(i <- [0 .. indent]) {
		s = s + "  ";
	}
	outToks = put(Space(s), outToks);
	return <outToks, indent * 2>;
}