package p;

enum A {

	ONE() {
		@Override
		String foo() {
		}
	},
	TWO() {
		@Override
		String foo() {
			"bar"
		}
	}

	String foo() {
	}
}
