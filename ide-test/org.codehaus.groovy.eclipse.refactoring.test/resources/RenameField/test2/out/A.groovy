package p;
class B {
    A c = new A()
    void s() {
        c.g = 3
        def f =  {f -> println(f)}
        c.g()
    }
}

class A {
    def g = 7
    def s() {
        g()
    }
}
