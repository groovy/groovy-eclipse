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
	def f = new MyBean()
	void main() {
		MyBean b2 = new MyBean(foo: f.foo)
	}
}