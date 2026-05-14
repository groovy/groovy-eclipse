package p

import p.B as X

class B {
    B a = new B()
    X x = new X()
    X m(B a, X x) {
        a = new X()
        x = new X()
    }
    def closure = { B a, X x ->
        a = new X()
        x = new X()
    }
}

@groovy.transform.CompileStatic
class C {
    B a = new B()
    X x = new X()
    X m(B a, X x) {
        a = new X()
        x = new X()
    }
    def closure = { B a, X x ->
        a = new X()
        x = new X()
    }
}
