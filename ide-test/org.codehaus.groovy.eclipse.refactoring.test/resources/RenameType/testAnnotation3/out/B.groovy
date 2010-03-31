package p;

import java.util.ArrayList;

@interface B {
    String value() default "NULL";
}
@B("")
class Test {
    
    @B("A and p.Test.A and p.A and q.Test.A")
    void test () {
        ArrayList<String> list= new ArrayList<String>() {
            void sort() {
                @B
                int current= 0;
                current++;
            }
        };
    }
}
