package p;

enum A {

  ONE() {
    @Override
    String getFoo() {
      'bar'
    }
  },

  TWO() {
    @Override
    String getFoo() {
      'baz'
    }
  }

  abstract String getFoo()
}
