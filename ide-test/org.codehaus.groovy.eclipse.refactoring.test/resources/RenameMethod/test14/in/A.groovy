package p;

class A {
	Integer foo

	Integer getFoo() {
		return foo
	}
}

class B {
	void m(A a) {
		a.with { foo += 1 }
	}
}
