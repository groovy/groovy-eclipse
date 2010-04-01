package p
class B {
    def f
}

def val = new B()
val.f

def val2 = ""
val2.f
val2 = val
val2.f

val = ""
val.f

