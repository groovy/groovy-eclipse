package p;

class A {
	String foo

	void setFoo(String foo) {
		this.foo = foo
	}
}

@groovy.transform.CompileStatic
class B {
	void m(A a) {
		a.foo = 'bar'
	}
}
