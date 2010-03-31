package p;


@interface A {
    String value() default "";
}

@interface Main {
   A child() default @A("Void");
}

@Main(child=@/*test*/A(""))
@A("")
class Client {
    @Deprecated
    @Main(child=@/*test*/A(""))
    @A("")
    void bad() {
        final @A int local= 0;
    }
}