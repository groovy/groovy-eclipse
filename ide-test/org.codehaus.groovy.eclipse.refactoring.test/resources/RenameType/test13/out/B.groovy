package p;
public class B {
    B(B a){}
    B a(B a){
        a= new B(new B(a));
        return a;
    }
}