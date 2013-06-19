module org::nuthatchery::pgf::rascal::TableBuilder

import org::nuthatchery::pgf::rascal::ListOp;
import org::nuthatchery::pgf::rascal::Token;
import org::nuthatchery::pgf::rascal::Tseq;

import IO;
import List;
import String;

data Decision 
	= Nothing()
	| Sequence(list[Decision] todo)
	| Skip()
	;
	
alias DecisionTable = map[tuple[TokenCat current, TokenCat lastSeen], Decision];

data TableBuilder
	= TB(
		set[TokenCat] cats,
		map[TokenCat, Decision] noLookTable,
		map[TokenCat, Decision] lookTable,
		DecisionTable table,
		Decision defaultDecision
	)
	;
	

public TableBuilder newTable() {
	return TB({}, (), (), (), Nothing());
}

public TableBuilder addCategory(TokenCat cat, TableBuilder tbl) {
	return tbl[cats = tbl.cats + cat];
}

public TableBuilder addDecision(TokenCat current, TokenCat lastSeen, Decision decision, TableBuilder tbl) {
	if(current == "*" && lastSeen == "*")
		return setDefault(decision, tbl);
	else if(current == "*")
		return addDecisionLookBehindOnly(lastSeen, decision, tbl);
	else if(lastSeen == "*")
		return addDecisionNoLookBehind(current, decision, tbl);
	else
        return addDecisionLookAtBoth(current, lastSeen, decision, tbl);
}

public TableBuilder addDecisionLookAtBoth(TokenCat current, TokenCat lastSeen, Decision decision, TableBuilder tbl) {
  tbl = tbl[cats = tbl.cats + {current, lastSeen}];
  return tbl[table = tbl.table + (<current, lastSeen> : decision)];
}

public TableBuilder addDecisionNoLookBehind(TokenCat current, Decision decision, TableBuilder tbl) {
	tbl = tbl[cats = tbl.cats + current];
	return tbl[noLookTable = tbl.noLookTable + (current : decision)];
}

public TableBuilder addDecisionLookBehindOnly(TokenCat current, Decision decision, TableBuilder tbl) {
	tbl = tbl[cats = tbl.cats + current];
	return tbl[lookTable = tbl.lookTable + (current : decision)];
}

public TableBuilder setDefault(Decision decision, TableBuilder tbl) {
	return tbl[defaultDecision = decision];
}

public DecisionTable compileTable(TableBuilder tbl) {
	println(tbl.defaultDecision);
	DecisionTable newTbl = ();
	
    // look behind only entries	
	for(k <- tbl.lookTable) {
		for(c <- tbl.cats) {
			newTbl[<c,k>] = tbl.lookTable[k];
		}
	}

    // look at current only entries
	for(k <- tbl.noLookTable) {
        // "" is the initial value for lastSeen
		newTbl[<k,"">] = tbl.noLookTable[k];
		for(c <- tbl.cats) {
			if(<k,c> in newTbl)
				newTbl[<k,c>] = combine(newTbl[<k,c>], tbl.noLookTable[k]);
			else
				newTbl[<k,c>] = tbl.noLookTable[k];
		}
	}

    // specific entries
    newTbl += tbl.table;

    // fill in the rest with defaults
	for(c1 <- tbl.cats) {
		if(<c1,""> notin newTbl)
			newTbl[<c1,"">] = tbl.defaultDecision;
		for(c2 <- tbl.cats) {
			if(<c1,c2> notin newTbl)
				newTbl[<c1,c2>] = tbl.defaultDecision;
		}
	}
	
	return newTbl;
}

// Note that a Skip() stays as such even if combined with
// something else.
Decision combine(Decision old, Decision new) {
	switch(<old,new>) {
		case <Nothing(),_>:
			return new;
		case <_, Nothing()>:
			return old;
		case <Skip(),_>:
            return old;
		case <_, Skip()>:
            return new;
		case <Sequence(ds1), Sequence(ds2)>:
			return Sequence(ds1 + ds2);
		case <Sequence(ds1), _>:
			return Sequence(ds1 + new);
		case <_, Sequence(ds2)>:
			return Sequence(old + ds2);
		default:
			return Sequence([old, new]);
	}
}

public DecisionTable makeTable(map[tuple[TokenCat current, TokenCat lastSeen], Decision] decisions) {
	return makeTable(decisions, {});
}

public DecisionTable makeTable(map[tuple[TokenCat current, TokenCat lastSeen], Decision] decisions, set[TokenCat] cats) {
	tbl = newTable();
	for(c <- cats)
		tbl = addCategory(c, tbl);
	for(<c1,c2> <- decisions)
		tbl = addDecision(c1, c2, decisions[<c1,c2>], tbl);
	return compileTable(tbl);
}
