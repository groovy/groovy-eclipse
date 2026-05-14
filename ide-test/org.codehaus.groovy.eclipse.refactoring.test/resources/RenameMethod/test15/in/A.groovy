package p

class A {
	Object getFoo() {
	}
	void setFoo(value) {
	}
	@groovy.transform.CompileStatic
	String toString() {"foo: $foo"}
}
