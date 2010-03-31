package p;
class B<T> {
    public B() {}
    public B(T t, X x) {}

    void m(B a) {
        new B<T>();
        new B<T>(null, "y");
    };
}

class X { 
    void x(B a) {
        new B<Integer>();
        new B<B>(new B(), "x");
        new B<B<B>>(null);
    };
}
