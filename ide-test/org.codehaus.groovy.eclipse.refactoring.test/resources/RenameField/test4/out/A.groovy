package p
class B extends A{
    def f
    void s() {
        f = f
        this.f = super.g
    }
}

class A {
    def g = 7
}