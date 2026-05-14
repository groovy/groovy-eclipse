package p

class A {
	void k(){
	}
	void p(){
		k()
		this.&k
	}
	{
		k()
		this.&k
		new A().k()
		new A().with { k(); it.&k }
	}
	static {
		new A().k()
		new A().with { k(); it.&k }
	}
}

class B {
	static void m(){
		m()
		this.&m
		new A().k()
		new A().with { k(); it.&k }
	}
	static {
		m()
		this.&m
		new A().k()
		new A().with { k(); it.&k }
	}
	{
		m()
		this.&m
		new A().k()
		new A().with { k(); it.&k }
	}
}

@groovy.transform.CompileStatic
class C {
	static void m(){
		m()
		this.&m
		new A().k()
		new A().with { k(); it.&k }
	}
	static {
		m()
		this.&m
		new A().k()
		new A().with { k(); it.&k }
	}
	{
		m()
		this.&m
		new A().k()
		new A().with { k(); it.&k }
	}
}
