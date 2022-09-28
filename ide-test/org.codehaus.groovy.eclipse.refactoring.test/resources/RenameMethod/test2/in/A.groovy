package p

class A {
	void m(){
	}
	void p(){
		m()
		this.&m
	}
	{
		m()
		this.&m
		new A().m()
		new A().with { m(); it.&m }
	}
	static {
		new A().m()
		new A().with { m(); it.&m }
	}
}

class B {
	static void m(){
		m()
		this.&m
		new A().m()
		new A().with { m(); it.&m }
	}
	static {
		m()
		this.&m
		new A().m()
		new A().with { m(); it.&m }
	}
	{
		m()
		this.&m
		new A().m()
		new A().with { m(); it.&m }
	}
}

@groovy.transform.CompileStatic
class C {
	static void m(){
		m()
		this.&m
		new A().m()
		new A().with { m(); it.&m }
	}
	static {
		m()
		this.&m
		new A().m()
		new A().with { m(); it.&m }
	}
	{
		m()
		this.&m
		new A().m()
		new A().with { m(); it.&m }
	}
}
