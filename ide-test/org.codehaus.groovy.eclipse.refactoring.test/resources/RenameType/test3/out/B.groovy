package p;
interface B{
   void m(B a);
}

class Sub implements B {
    void m(B a){}
}