package p

class MyBean {
	Class foo

	public String setBar(Class bar) {
		foo = bar
	}
}

@groovy.transform.CompileStatic
class A {
	def one = new MyBean()

	void main() {
		def two = new MyBean(foo: (new MyBean()).foo)
	}
}
