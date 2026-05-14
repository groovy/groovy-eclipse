package p

import static p.A.*
import static p.A.sm as m

class A {
    static q
    static sm(... args) {
        q = A.q = this.q
    }
    void proc() {
        m(q)
        getQ()
        setQ('')
        A.getQ()
        A.setQ('')
        this.getQ()
        this.setQ('')
    }
}

class B {
    def p
    void m() {
        p = A.q
    }
    static m2() {
        q = A.q
    }
}

@groovy.transform.CompileStatic
class C {
    def p
    void m() {
        p = A.q
    }
    static m2() {
        q = A.q
    }
}
