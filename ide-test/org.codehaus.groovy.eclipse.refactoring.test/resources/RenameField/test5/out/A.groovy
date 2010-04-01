package p
class A {
    def g = 7
    {
        g = g
        while (true) {
            def f = this.g
            f++
        }
        g++
    }
}