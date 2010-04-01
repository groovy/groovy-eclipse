package p
class B extends A{
    void s() {
        g = g
        this.g = super.g
    }
}

class A extends C {
    def g = 7
    void s() {
        this.g = super.f
        super.f = g
    }
}

class C {
    def f = 7
}
