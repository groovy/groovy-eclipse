package p
class B {
    def f
}

class C extends B {
    
}
def val = new C()
val.f

def val2 = ""
val2.f
val2 = val
val2.f

val = ""
val.f