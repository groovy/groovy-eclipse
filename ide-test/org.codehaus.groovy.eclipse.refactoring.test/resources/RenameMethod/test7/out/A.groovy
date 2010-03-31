package p;

interface A{
    def k()
}

class impl implements A{
    def k(){
        k
        k()
        def g = new A()
        g.k
        g.k()
        g = new randomClass()
        g.m
        g.m()
    }
}

class Impl2 implements A{
    A a
    Impl2 b
    def k(){
        k
        k()
        a.k
        b.k
    }
}

class randomClass{
    def m(){
        m
        m()
    }
}
