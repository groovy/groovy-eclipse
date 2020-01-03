package p

class MyBean {
	Integer fooBar = 0

	Integer getFoo() {
		return fooBar
	}
}

void meth(MyBean bean) {
	bean.fooBar += 1
	bean.with { fooBar += 1 }
}
