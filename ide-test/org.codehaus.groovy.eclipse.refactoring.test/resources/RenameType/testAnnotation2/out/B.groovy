package p;


@interface B {
    String value() default "";
}

@interface Main {
   B child() default @B("Void");
}

@Main(child=@/*test*/B(""))
@B("")
class Client {
    @Deprecated
    @Main(child=@/*test*/B(""))
    @B("")
    void bad() {
        final @B int local= 0;
    }
}