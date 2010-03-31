package p;
@interface A { }

@A
class Client {
    @Deprecated @A() void bad() { }
}