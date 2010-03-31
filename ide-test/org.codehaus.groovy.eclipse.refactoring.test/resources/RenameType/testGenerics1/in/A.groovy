package p;
class A<T> {
    public A() {}
    public A(T t, X x) {}

    void m(A a) {
        new A<T>();
        new A<T>(null, "y");
    };
}

class X { 
    void x(A a) {
        new A<Integer>();
        new A<A>(new A(), "x");
        new A<A<A>>(null);
    };
}
