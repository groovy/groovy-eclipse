package p
class B extends A{
    def f
    void s() {
        f = f
        this.f = super.f
    }
}

class A {
    def f = 7
}