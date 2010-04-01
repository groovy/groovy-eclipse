package p
class B extends A {
    def f
    void s() {
        f = A.g
        f = super.g
    }
    static s2() {
        f = A.g
    }
}

class A {
    static g = 7
    static s() {
        g = A.g
    }
}