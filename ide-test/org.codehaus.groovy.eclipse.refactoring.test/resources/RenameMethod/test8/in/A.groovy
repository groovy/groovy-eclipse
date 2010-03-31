package p;

class B{
    def m(){
    }
}
class myOtherClass{
    def m(){
    }
}
B a = new B()
a.m()
a = new myOtherClass()
a.m()
