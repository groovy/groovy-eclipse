package p;
class B {
    A c = new A()
    void s() {
        c.f = 3
        def f =  {f -> println(f)}
        c.f()
    }
}

class A {
    def f = 7
    def s() {
        f()
    }
}
