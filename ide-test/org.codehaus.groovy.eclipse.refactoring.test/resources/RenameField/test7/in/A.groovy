package p
class A {
    def f = 7
    def c = { f ->
        f = this.f
    }
}