package p;

class B {
    void test(A a) {
        a.m();
        a.x(0);
        a.x(0,0);
        a.x(0,0,0);
        java.util.function.Function f = a::x;
    }
}
