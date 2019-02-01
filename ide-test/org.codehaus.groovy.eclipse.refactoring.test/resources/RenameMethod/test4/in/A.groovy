package p

class A {
    void m() {
        def a = new A()
        a.m()
        a.m
        a = new D()
        a.m()
        a.m
        m()
        m
    }
}

class D {
    void m() {
        def x = new A()
        x.m()
        x.m
        x = new D()
        x.m()
        x.m
        m()
        m
    }
}
