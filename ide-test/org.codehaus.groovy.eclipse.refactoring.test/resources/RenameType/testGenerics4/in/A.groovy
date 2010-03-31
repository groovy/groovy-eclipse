package p;

class A {
    Class<? extends A> class1= this.getClass();
    Class<? extends A> class2= A.class;
    Class<A> class3= (Class<A>) this.getClass();
    X<A> getX() {
        X<A> x= new X<A>();
        x.t= (new ArrayList<A>() as A[]);
        return x;
    }
}

class X<T extends A> {
    def t
}
