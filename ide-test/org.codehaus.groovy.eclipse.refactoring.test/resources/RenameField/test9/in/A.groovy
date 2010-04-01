package p
class B extends A {
    def f
    void s() {
        f = A.f
        f = super.f
    }
    static s2() {
        f = A.f
    }
}

class A {
    static f = 7
    static s() {
        f = A.f
    }
}