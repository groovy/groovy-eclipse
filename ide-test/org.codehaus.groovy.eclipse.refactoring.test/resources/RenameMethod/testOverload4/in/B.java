package p;

class B {
    void test(A a) {
        a.m();
        a.m(0);
        a.m(0,0);
        a.m(0,0,0);
        java.util.function.Function f = a::m;
    }
}
