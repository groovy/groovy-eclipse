package p

interface A {
    def m()
}

class B implements A {
    def m() {
        m()
        m
        def x = new A()
        x.m()
        x.m
        x = new randomClass()
        x.m()
        x.m
    }
}

class C implements A {
    A a
    C c
    def m() {
        m()
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
