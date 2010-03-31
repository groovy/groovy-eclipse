package p;
interface A{
   void m(A a);
}

class Sub implements A {
    void m(A a){}
}