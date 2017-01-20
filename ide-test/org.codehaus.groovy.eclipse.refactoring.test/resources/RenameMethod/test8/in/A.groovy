package p;

class B{
    def m(){
    }
}
class myOtherClass{
    def m(){
    }
}
def x = new B()
x.m()
x = new myOtherClass()
x.m()
