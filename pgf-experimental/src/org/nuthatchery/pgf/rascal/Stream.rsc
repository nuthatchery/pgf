// run with rascal-src
module org::nuthatchery::pgf::rascal::Stream

import List;
import IO;

data Stream[&T] = ListStream(list[&T] lst);

public Stream[&T] put(&T elt, ListStream(lst)) {
	return ListStream(lst + elt);
}

public tuple[&T, Stream[&T]] get(ListStream(list[&T] lst)) {
	return <head(lst), ListStream(tail(lst))>;
}

public bool eof(ListStream(list[&T] lst)) {
	return isEmpty(lst);
}

public test bool eofTest() {
    return eof(ListStream([]));
}

public bool putGetTest(&T elt) {
	assert get(put(elt, ListStream([]))) == <elt, ListStream([])>;
	return true;
}

public bool putGetTest(&T elt, Stream[&T] stream) {
	stream2 = put(elt, stream);
	&T t;

	do {
		<t, stream2> = get(stream2);
	} while(!eof(stream2));
	
	assert t == elt;
	return true;
}

public test bool putGetTest() {
	Stream[str] stream = ListStream([]);
	for(s <- ["a", "b", "c", "d"]) {
		stream = put(s, stream);
		putGetTest(s);
		putGetTest(s, stream);
	}
	return true;
}
