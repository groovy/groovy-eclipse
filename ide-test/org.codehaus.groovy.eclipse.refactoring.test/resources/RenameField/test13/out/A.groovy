package p;

class MyBean {
	Class foo
	public String setBar(Class bar) {
		foo = bar
	}
}

@groovy.transform.CompileStatic
class A {
	def g = new MyBean()
	void main() {
		MyBean b2 = new MyBean(foo: g.foo)
	}
}