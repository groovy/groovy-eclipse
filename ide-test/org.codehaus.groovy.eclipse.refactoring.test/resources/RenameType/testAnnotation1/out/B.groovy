package p;
@interface B { }

@B
class Client {
    @Deprecated @B() void bad() { }
}