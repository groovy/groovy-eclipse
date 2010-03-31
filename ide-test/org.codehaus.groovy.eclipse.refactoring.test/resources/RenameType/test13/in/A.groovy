package p;
public class A {
    A(A a){}
    A a(A a){
        a= new A(new A(a));
        return a;
    }
}