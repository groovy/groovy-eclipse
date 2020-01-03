package p

class MyBean {
	Integer foo = 0

	Integer getFoo() {
		return foo
	}
}

void meth(MyBean bean) {
	bean.foo += 1
	bean.with { foo += 1 }
}
