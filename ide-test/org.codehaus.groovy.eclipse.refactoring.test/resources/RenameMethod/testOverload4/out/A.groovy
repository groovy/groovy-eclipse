package p

class A {
    def m() {
    }
    def x(i, j = 1, k = 2) {
    }
    def n(A a) {
        a.m()
        a.x(0)
        a.x(0,0)
        a.x(0,0,0)
        a.with {
            m()
            x(0)
            x(0,0)
            x(0,0,0)
        }
        def f = a.&x
    }
}
