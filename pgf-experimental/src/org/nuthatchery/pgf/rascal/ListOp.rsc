module org::nuthatchery::pgf::rascal::ListOp

// Wanted to use the module name pgf::List, but broke here due to
// "Module name pgf::List does not match List"
import List;

//
// stream-like
//

public list[&T] unget(list[&T] s, &T t) {
  return insertAt(s, 0, t);
}

public list[&T] write(list[&T] s, &T t) {
  return s + [t];
}

//
// stack-like
//

// List module defines a different 'push'.
public list[&T] spush(list[&T] s, &T t) {
  return insertAt(s, 0, t);
}

// List module defines a different 'pop'.
public tuple[list[&T], &T] spop(list[&T] s) {
  return <tail(s), head(s)>;
}
