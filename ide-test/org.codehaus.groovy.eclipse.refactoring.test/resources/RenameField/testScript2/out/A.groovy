package p
class B {
    def g
}

class C extends B {
    
}
def val = new C()
val.g

def val2 = ""
val2.f
val2 = val
val2.g

val = ""
val.f