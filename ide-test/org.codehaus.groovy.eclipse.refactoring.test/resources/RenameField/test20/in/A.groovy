package p

import static p.A.CONST as thing
import static p.A.CONST as value

enum A {
    CONST;
    def m() {
        this.CONST
        A.CONST
        CONST
        value
    }
    static sm() {
        this.CONST
        A.CONST
        CONST
        value
    }
}

class B {
    static value
    void m() {
        value = A.CONST
        value = thing
    }
    static sm() {
        value = A.CONST
        value = thing
    }
}

@groovy.transform.CompileStatic
class C {
    static value
    void m() {
        value = A.CONST
        value = thing
    }
    static sm() {
        value = A.CONST
        value = thing
    }
}
