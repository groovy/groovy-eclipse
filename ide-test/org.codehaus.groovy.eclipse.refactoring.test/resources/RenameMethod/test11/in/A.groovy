package p;

enum A {

	A() {
		@Override
		String getFoo() {
		}
	},
	B() {
		@Override
		String getFoo() {
			"bar"
		}
	}

	String getFoo() {
	}
}