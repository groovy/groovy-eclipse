package p;
class A{
    void k(){
        def a = new A()
        a.k()
        a.k
        a = new D()
        a.m()
        a.m
        k
        k()
    }
}
class D{
    void m(){
        def a = new A()
        a.k()
        a.k
        a = new D()
        a.m()
        a.m
        m
        m()
    }
}