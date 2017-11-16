package p;

import groovy.transform.CompileStatic;

@CompileStatic
enum A {

  A() {
    @Override
    String foo() {
      'bar'
    }
  },

  B() {
    @Override
    String foo() {
      'baz'
    }
  }

  String foo() {
  }
}
