package p

class A {
    static void k(D self) {
    }
}

class D {
    void m() {
        m
        m()
        this.m
        this.m()
        use(A) {
            m
            k()
            this.m
            this.k()
            new A().m
            new A().m()
            def x = new A()
            x.m
            x.m()
        }
        m
        m()
        this.m
        this.m()
    }
}
