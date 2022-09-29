package p

import p.A as X

class A {
    A a = new A()
    X x = new X()
    X m(A a, X x) {
        a = new X()
        x = new X()
    }
    def closure = { A a, X x ->
        a = new X()
        x = new X()
    }
}

@groovy.transform.CompileStatic
class C {
    A a = new A()
    X x = new X()
    X m(A a, X x) {
        a = new X()
        x = new X()
    }
    def closure = { A a, X x ->
        a = new X()
        x = new X()
    }
}
