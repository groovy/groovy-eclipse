package p

import static p.A.getQ as getX
import static p.A.sm as m

class A {
    static q
    static sm(... args) {
        q = A.q = this.q
    }
    void proc() {
        m(q)
        m(x)
    }
}

class B extends A {
    def p
    void m() {
        p = A.q = super.q
    }
    static m2() {
        p = A.q
        p = x
        x = p
    }
}

@groovy.transform.CompileStatic
class C extends A {
    def p
    void m() {
        p = A.q = super.q
    }
    static m2() {
        p = A.q
        p = x
        x = p
    }
}
