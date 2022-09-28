package p

import static p.A.getP as getX
import static p.A.sm as m

class A {
    static p
    static sm(... args) {
        p = A.p = this.p
    }
    void proc() {
        m(p)
        m(x)
    }
}

class B extends A {
    def p
    void m() {
        p = A.p = super.p
    }
    static m2() {
        p = A.p
        p = x
        x = p
    }
}

@groovy.transform.CompileStatic
class C extends A {
    def p
    void m() {
        p = A.p = super.p
    }
    static m2() {
        p = A.p
        p = x
        x = p
    }
}
