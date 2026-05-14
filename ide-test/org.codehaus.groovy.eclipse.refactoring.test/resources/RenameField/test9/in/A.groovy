package p

import static p.A.f as x
import static p.A.sm as m

class A {
    public static f
    static sm(... args) {
        f = A.f = this.f
    }
    void proc() {
        m(f)
        m(x)
    }
}

class B extends A {
    public f
    void m() {
        f = A.f = super.f
    }
    static m2() {
        f = A.f
        f = x
        x = f
    }
}

@groovy.transform.CompileStatic
class C extends A {
    public f
    void m() {
        f = A.f = super.f
    }
    static m2() {
        f = A.f
        f = x
        x = f
    }
}
