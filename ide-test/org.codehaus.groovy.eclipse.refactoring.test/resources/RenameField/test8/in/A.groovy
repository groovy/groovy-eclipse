package p
class B extends A {
    def f
    void s() {
        f = A.f
        f = super.f
    }
}

class A extends C {
    static f = 7
    void s() {
        f = this.f
        A.f = super.f
        super.f = f
    }
}

class C {
    def f = 7
}
