package p

class A {
    void k() {
        def a = new A()
        a.k()
        a.m
        a = new D()
        a.m()
        a.m
        k()
        m
    }
}

class D {
    void m() {
        def x = new A()
        x.k()
        x.m
        x = new D()
        x.m()
        x.m
        m()
        m
    }
}
