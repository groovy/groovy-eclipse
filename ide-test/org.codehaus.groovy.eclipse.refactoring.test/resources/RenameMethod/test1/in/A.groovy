package p

class A {
	static void m(){
	}
	void p(){
		this.&m
		m()
	}
	static foo(){
		this.&m
		m()
	}
	{
		{ -> A.m(); A.&m; m() }
		new A().m()
		A.m()
		A.&m
		m()
	}
	static {
		{ -> A.m(); A.&m; m() }
		new A().m()
		A.m()
		A.&m
		m()
	}
}

class B {
	static void m(){
		{ -> A.m(); A.&m; m() }
		new A().m()
		A.m()
		A.&m
		m()
	}
	static {
		{ -> A.m(); A.&m; m() }
		new A().m()
		A.m()
		A.&m
		m()
	}
	{
		{ -> A.m(); A.&m; m() }
		new A().m()
		A.m()
		A.&m
		m()
	}
}

@groovy.transform.CompileStatic
class C {
	static void m(){
		{ -> A.m(); A.&m; m() }
		new A().m()
		A.m()
		A.&m
		m()
	}
	static {
		{ -> A.m(); A.&m; m() }
		new A().m()
		A.m()
		A.&m
		m()
	}
	{
		{ -> A.m(); A.&m; m() }
		new A().m()
		A.m()
		A.&m
		m()
	}
}
