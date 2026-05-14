package p

class A implements I {
     def m(a) {
         m(a)
         m(a,b)
         m(a,b,c)
     }
     def m(a,b) {
         I i
         i.m()
         i.m(a)
         i.m(a,b)
     }
}

interface I {
    def m(a)
    def m(a,b)
}
