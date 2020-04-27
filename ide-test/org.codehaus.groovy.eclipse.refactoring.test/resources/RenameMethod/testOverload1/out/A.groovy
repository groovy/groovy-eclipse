package p

class A implements I {
     def k(a) {
         k(a)
         m(a,b)
         m(a,b,c)
     }
     def m(a,b) {
         I i
         i.m()
         i.k(a)
         i.m(a,b)
     }
}

interface I {
    def k(a)
    def m(a,b)
}
