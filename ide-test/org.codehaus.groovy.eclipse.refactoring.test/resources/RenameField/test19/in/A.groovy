package p

import static p.A.*
import static p.A.sm as m

class A {
    static p
    static sm(... args) {
        p = A.p = this.p
    }
    void proc() {
        m(p)
        getP()
        setP('')
        A.getP()
        A.setP('')
        this.getP()
        this.setP('')
    }
}

class B {
    def p
    void m() {
        p = A.p
    }
    static m2() {
        p = A.p
    }
}

@groovy.transform.CompileStatic
class C {
    def p
    void m() {
        p = A.p
    }
    static m2() {
        p = A.p
    }
}
