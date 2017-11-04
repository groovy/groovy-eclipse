package p

class A {
    public static def f
    static def s() {
        f = A.f
    }
}

class B extends A {
    public def f
    void m() {
        f = A.f
        f = super.f
    }
    static def s2() {
        f = A.f
    }
}
