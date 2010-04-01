package p
class B extends A {
    def f
    void s() {
        f = A.g
        f = super.g
    }
}

class A extends C {
    static g = 7
    void s() {
        g = this.g
        A.g = super.f
        super.f = g
    }
}

class C {
    def f = 7
}
