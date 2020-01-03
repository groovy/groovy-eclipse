package p;

class A {
	Integer foo

	Integer getFooBar() {
		return foo
	}
}

class B {
	void m(A a) {
		a.foo += 1
	}
}
