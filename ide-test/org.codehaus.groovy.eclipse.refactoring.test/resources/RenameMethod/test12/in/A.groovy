package p;

class A {
	Integer foo

	void setFoo(Integer foo) {
		this.foo = foo
	}
}

class B {
	void m(A a) {
		a.with { foo += 1 }
	}
}
