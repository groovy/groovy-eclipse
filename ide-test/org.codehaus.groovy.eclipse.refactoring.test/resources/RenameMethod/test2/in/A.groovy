package p;
class A{
    void m(){
    }
    void f(){
        m();
    }
    {
        m();
        new A().m();
    }
    static {
        new A().m();
    }
}
class D{
    static void m(){
        new A().m();
        m();
    }
    static {
        new A().m();
        m();
    }
    {
        new A().m();
        m();
    }
}