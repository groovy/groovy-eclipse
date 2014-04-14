package p;

enum A {

	A() {
		@Override
		String foo() {
		}
	},
	B() {
		@Override
		String foo() {
			"bar"
		}
	}

	String foo() {
	}
}