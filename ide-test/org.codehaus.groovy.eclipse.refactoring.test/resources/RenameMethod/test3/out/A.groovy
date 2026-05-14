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
            new A().k()
            def x = new A()
            x.m
            x.k()
        }
        A.&k
        A.k()
    }
}
