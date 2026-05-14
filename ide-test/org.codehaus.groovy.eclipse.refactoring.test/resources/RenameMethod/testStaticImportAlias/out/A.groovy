package p

import static p.A.k as x

class A {
	static void k(... args) {
	}
	static foo() {
		this.&k
		k()
		x()
	}
	def bar() {
		this.&k
		k()
		x()
	}
	def baz(m) {
		A.k(m)
		x(m)
	}
	static {
		{ -> A.k(); A.&k; k(); x() }
		new A().k()
		A.k()
		A.&k
		k()
		x()
	}
	{
		{ -> A.k(); A.&k; k(); x() }
		new A().k()
		A.k()
		A.&k
		k()
		x()
	}
}

class B {
	static void m() {
		{ -> A.k(); A.&k; m(); x() }
		new A().k()
		A.k()
		A.&k
		m()
		x()
	}
	static {
		{ -> A.k(); A.&k; m(); x() }
		new A().k()
		A.k()
		A.&k
		m()
		x()
	}
	{
		{ -> A.k(); A.&k; m(); x() }
		new A().k()
		A.k()
		A.&k
		m()
		x()
	}
}

@groovy.transform.CompileStatic
class C {
	static void m() {
		{ -> A.k(); A.&k; m(); x() }
		new A().k()
		A.k()
		A.&k
		m()
		x()
	}
	static {
		{ -> A.k(); A.&k; m(); x() }
		new A().k()
		A.k()
		A.&k
		m()
		x()
	}
	{
		{ -> A.k(); A.&k; m(); x() }
		new A().k()
		A.k()
		A.&k
		m()
		x()
	}
}
