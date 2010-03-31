package p;
class A{
    static void m(D self) { }
}
class D{
    void m(){
        m()
        m
        this.m
        this.m()
        use(A) {
            m()
            this.m()
            this.m
            new A().m
            new A().m()
            def x = new A()
            x.m
            x.m()
        }
        this.m
        this.m()
        m()
        m
    }
}