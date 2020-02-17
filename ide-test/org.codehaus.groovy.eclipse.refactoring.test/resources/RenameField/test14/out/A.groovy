package p

class MyBean {
	Class fooBar

	public String setBar(Class bar) {
		fooBar = bar
	}
}

@groovy.transform.CompileStatic
class A {
	def one = new MyBean()

	void main() {
		def two = new MyBean(fooBar: (new MyBean()).fooBar)
	}
}
