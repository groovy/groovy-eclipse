package p;
class A{ }

class Other {
    A x() {
        def x = { A a ->
                a
        }
    }
}