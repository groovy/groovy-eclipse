package p

import static p.A.g as x
import static p.A.sm as m

class A {
    public static g
    static sm(... args) {
        g = A.g = this.g
    }
    void proc() {
        m(g)
        m(x)
    }
}

class B extends A {
    public f
    void m() {
        f = A.g = super.g
    }
    static m2() {
        f = A.g
        f = x
        x = f
    }
}

@groovy.transform.CompileStatic
class C extends A {
    public f
    void m() {
        f = A.g = super.g
    }
    static m2() {
        f = A.g
        f = x
        x = f
    }
}
