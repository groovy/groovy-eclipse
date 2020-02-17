package p;

class A {
	Integer foo

	void setFooBar(Integer foo) {
		this.foo = foo
	}
}

class B {
	void m(A a) {
		a.fooBar += 1
	}
}
