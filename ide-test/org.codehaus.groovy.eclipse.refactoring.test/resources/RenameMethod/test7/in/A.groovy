package p;

interface A{
    def m()
}

class impl implements A{
    def m(){
        m
        m()
        def g = new A()
        g.m
        g.m()
        g = new randomClass()
        g.m
        g.m()
    }
}

class Impl2 implements A{
    A a
    Impl2 b
    def m(){
        m
        m()
        a.m
        b.m
    }
}

class randomClass{
    def m(){
        m
        m()
    }
}
