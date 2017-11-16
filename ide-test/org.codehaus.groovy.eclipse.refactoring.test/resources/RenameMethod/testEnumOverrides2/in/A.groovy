package p;

import groovy.transform.CompileStatic;

@CompileStatic
enum A {

  A() {
    @Override
    String getFoo() {
      'bar'
    }
  },

  B() {
    @Override
    String getFoo() {
      'baz'
    }
  }

  String getFoo() {
  }
}
