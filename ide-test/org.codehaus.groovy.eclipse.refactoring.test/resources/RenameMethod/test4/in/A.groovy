package p;
class A{
    void m(){
        def a = new A()
        a.m()
        a.m
        a = new D()
        a.m()
        a.m
        m
        m()
    }
}
class D{
    void m(){
        def a = new A()
        a.m()
        a.m
        a = new D()
        a.m()
        a.m
        m
        m()
    }
}