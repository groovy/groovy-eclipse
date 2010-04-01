package p
class A {
    def g = 7
    def s() {
        "${g}"
        "${this.g}"
    }
}