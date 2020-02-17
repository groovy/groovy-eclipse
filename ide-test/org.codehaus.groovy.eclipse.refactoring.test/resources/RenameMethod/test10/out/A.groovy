package p;

class A {
	String foo

	void setFooBar(String foo) {
		this.foo = foo
	}
}

@groovy.transform.CompileStatic
class B {
	void m(A a) {
		a.fooBar = 'bar'
	}
}
