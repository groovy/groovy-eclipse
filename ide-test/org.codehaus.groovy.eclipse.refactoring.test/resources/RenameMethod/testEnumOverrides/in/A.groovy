package p;

enum A {

	ONE() {
		@Override
		String getFoo() {
		}
	},
	TWO() {
		@Override
		String getFoo() {
			"bar"
		}
	}

	String getFoo() {
	}
}
