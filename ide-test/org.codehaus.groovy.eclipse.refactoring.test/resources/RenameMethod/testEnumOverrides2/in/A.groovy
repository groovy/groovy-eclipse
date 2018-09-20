package p;

import groovy.transform.CompileStatic;

@CompileStatic
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

  String getFoo() {
  }
}
