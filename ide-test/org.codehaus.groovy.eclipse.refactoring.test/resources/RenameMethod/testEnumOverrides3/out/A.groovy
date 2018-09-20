package p;

enum A {

  ONE() {
    @Override
    String foo() {
      'bar'
    }
  },

  TWO() {
    @Override
    String foo() {
      'baz'
    }
  }

  abstract String foo()
}
