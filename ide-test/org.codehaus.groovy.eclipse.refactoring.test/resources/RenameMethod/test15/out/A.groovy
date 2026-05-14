package p

class A {
	Object getBar() {
	}
	void setFoo(value) {
	}
	@groovy.transform.CompileStatic
	String toString() {"foo: $bar"}
}
