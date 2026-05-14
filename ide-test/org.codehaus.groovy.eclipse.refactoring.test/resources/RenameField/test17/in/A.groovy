package p

class Foo {
	private bar

	Object getBar() {
		return bar
	}
	void setBar(value) {
		this.bar = value
	}
	@groovy.transform.CompileStatic
	String toString() {"bar: $bar"}
}

@groovy.transform.CompileStatic
def one() {
	def foo = new Foo()
	def bar = foo.bar
	foo.bar = null
}

@groovy.transform.CompileStatic
def two() {
	def foo = new Foo()
	foo.with {
		print(bar)
		bar = null
	}
}

@groovy.transform.CompileStatic
class C extends Foo {
	def three() {
		print(bar)
		bar = null
	}
}
