package p;
class A{
    void k(){
    }
    void f(){
        k();
    }
    {
        k();
        new A().k();
    }
    static {
        new A().k();
    }
}
class D{
    static void m(){
        new A().k();
        m();
    }
    static {
        new A().k();
        m();
    }
    {
        new A().k();
        m();
    }
}