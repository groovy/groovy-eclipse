package p
class B extends A{
    void s() {
        f = f
        this.f = super.f
    }
}

class A extends C {
    def f = 7
    void s() {
        this.f = super.f
        super.f = f
    }
}

class C {
    def f = 7
}
