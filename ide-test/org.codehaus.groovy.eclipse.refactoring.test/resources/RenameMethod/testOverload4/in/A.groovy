package p

class A {
    def m() {
    }
    def m(i, j = 1, k = 2) {
    }
    def n(A a) {
        a.m()
        a.m(0)
        a.m(0,0)
        a.m(0,0,0)
        a.with {
            m()
            m(0)
            m(0,0)
            m(0,0,0)
        }
        def f = a.&m
    }
}
