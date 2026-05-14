package p

class A {
	static void k(){
	}
	void p(){
		this.&k
		k()
	}
	static foo(){
		this.&k
		k()
	}
	{
		{ -> A.k(); A.&k; k() }
		new A().k()
		A.k()
		A.&k
		k()
	}
	static {
		{ -> A.k(); A.&k; k() }
		new A().k()
		A.k()
		A.&k
		k()
	}
}

class B {
	static void m(){
		{ -> A.k(); A.&k; m() }
		new A().k()
		A.k()
		A.&k
		m()
	}
	static {
		{ -> A.k(); A.&k; m() }
		new A().k()
		A.k()
		A.&k
		m()
	}
	{
		{ -> A.k(); A.&k; m() }
		new A().k()
		A.k()
		A.&k
		m()
	}
}

@groovy.transform.CompileStatic
class C {
	static void m(){
		{ -> A.k(); A.&k; m() }
		new A().k()
		A.k()
		A.&k
		m()
	}
	static {
		{ -> A.k(); A.&k; m() }
		new A().k()
		A.k()
		A.&k
		m()
	}
	{
		{ -> A.k(); A.&k; m() }
		new A().k()
		A.k()
		A.&k
		m()
	}
}
