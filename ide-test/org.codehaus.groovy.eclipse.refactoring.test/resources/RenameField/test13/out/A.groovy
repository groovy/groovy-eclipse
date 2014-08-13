package p;
import groovy.transform.CompileStatic

class MyBean {
	Class foo
	public String setBar(Class bar) {
		foo = bar
	}
}

@CompileStatic
class A {
	def g = new MyBean()
	void main() {
		MyBean b2 = new MyBean(foo: g.foo)
	}
}