package p
class A {
    def f = 7
    def s() {
        "${f}"
        "${this.f}"
    }
}