package p

class A {
    public static def g
    static def s() {
        g = A.g
    }
}

class B extends A {
    public def f
    void m() {
        f = A.g
        f = super.g
    }
    static def s2() {
        f = A.g
    }
}
