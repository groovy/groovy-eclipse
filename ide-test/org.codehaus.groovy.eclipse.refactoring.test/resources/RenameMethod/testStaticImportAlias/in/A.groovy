package p

import static p.A.m as x

class A {
	static void m(... args) {
	}
	static foo() {
		this.&m
		m()
		x()
	}
	def bar() {
		this.&m
		m()
		x()
	}
	def baz(m) {
		A.m(m)
		x(m)
	}
	static {
		{ -> A.m(); A.&m; m(); x() }
		new A().m()
		A.m()
		A.&m
		m()
		x()
	}
	{
		{ -> A.m(); A.&m; m(); x() }
		new A().m()
		A.m()
		A.&m
		m()
		x()
	}
}

class B {
	static void m() {
		{ -> A.m(); A.&m; m(); x() }
		new A().m()
		A.m()
		A.&m
		m()
		x()
	}
	static {
		{ -> A.m(); A.&m; m(); x() }
		new A().m()
		A.m()
		A.&m
		m()
		x()
	}
	{
		{ -> A.m(); A.&m; m(); x() }
		new A().m()
		A.m()
		A.&m
		m()
		x()
	}
}

@groovy.transform.CompileStatic
class C {
	static void m() {
		{ -> A.m(); A.&m; m(); x() }
		new A().m()
		A.m()
		A.&m
		m()
		x()
	}
	static {
		{ -> A.m(); A.&m; m(); x() }
		new A().m()
		A.m()
		A.&m
		m()
		x()
	}
	{
		{ -> A.m(); A.&m; m(); x() }
		new A().m()
		A.m()
		A.&m
		m()
		x()
	}
}
