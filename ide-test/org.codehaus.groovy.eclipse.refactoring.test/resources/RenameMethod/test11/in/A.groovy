package b;

enum R {

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