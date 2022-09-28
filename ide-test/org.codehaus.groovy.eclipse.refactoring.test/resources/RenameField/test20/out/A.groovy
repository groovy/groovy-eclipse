package p

import static p.A.VALUE as thing
import static p.A.VALUE as value

enum A {
    VALUE;
    def m() {
        this.VALUE
        A.VALUE
        VALUE
        value
    }
    static sm() {
        this.VALUE
        A.VALUE
        VALUE
        value
    }
}

class B {
    static value
    void m() {
        value = A.VALUE
        value = thing
    }
    static sm() {
        value = A.VALUE
        value = thing
    }
}

@groovy.transform.CompileStatic
class C {
    static value
    void m() {
        value = A.VALUE
        value = thing
    }
    static sm() {
        value = A.VALUE
        value = thing
    }
}
