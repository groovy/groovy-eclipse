package p
class A {
    def f = 7
    {
        f = f
        while (true) {
            def f = this.f
            f++
        }
        f++
    }
}