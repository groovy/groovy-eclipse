package p;

class B {
    Class<? extends B> class1= this.getClass();
    Class<? extends B> class2= B.class;
    Class<B> class3= (Class<B>) this.getClass();
    X<B> getX() {
        X<B> x= new X<B>();
        x.t= (new ArrayList<B>() as B[]);
        return x;
    }
}

class X<T extends B> {
    def t
}
