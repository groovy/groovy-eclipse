package p;

class B{
    def k(){
    }
}
class myOtherClass{
    def m(){
    }
}
def x = new B()
x.k()
x = new myOtherClass()
x.m()
