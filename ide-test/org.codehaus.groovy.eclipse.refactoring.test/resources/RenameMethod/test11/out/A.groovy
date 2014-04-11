package b;

enum R {

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