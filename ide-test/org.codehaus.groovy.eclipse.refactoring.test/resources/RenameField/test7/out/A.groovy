package p
class A {
    def g = 7
    def c = { f ->
        f = this.g
    }
}