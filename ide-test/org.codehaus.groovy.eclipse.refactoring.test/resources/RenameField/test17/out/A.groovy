package p

class Foo {
	private baz

	Object getBaz() {
		return baz
	}
	void setBaz(value) {
		this.baz = value
	}
	@groovy.transform.CompileStatic
	String toString() {"bar: $baz"}
}

@groovy.transform.CompileStatic
def one() {
	def foo = new Foo()
	def bar = foo.baz
	foo.baz = null
}

@groovy.transform.CompileStatic
def two() {
	def foo = new Foo()
	foo.with {
		print(baz)
		baz = null
	}
}

@groovy.transform.CompileStatic
class C extends Foo {
	def three() {
		print(baz)
		baz = null
	}
}
