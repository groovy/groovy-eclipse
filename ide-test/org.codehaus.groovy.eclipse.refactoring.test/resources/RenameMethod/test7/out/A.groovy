package p

interface A {
    def k()
}

class B implements A {
    def k() {
        k()
        m
        def x = new A()
        x.k()
        x.m
        x = new randomClass()
        x.m()
        x.m
    }
}

class C implements A {
    A a
    C c
    def k() {
        k()
        m
        a.m
        c.m
    }
}

class D {
    def m() {
        m()
        m
    }
}
