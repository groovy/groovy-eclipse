package p;
import groovy.transform.CompileStatic

class MyBean {
	Class baz
	public String setBar(Class bar) {
		baz = bar
	}
}

@CompileStatic
class A {
	def f = new MyBean()
	void main() {
		MyBean b2 = new MyBean(baz: (new MyBean()).baz)
	}
}