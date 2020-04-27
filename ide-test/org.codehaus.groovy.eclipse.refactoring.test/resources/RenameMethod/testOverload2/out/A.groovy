package p

class A implements J {
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
         i.m(a,b,c)
         J j
         j.m()
         j.k(a)
         j.m(a,b)
         j.m(a,b,c)
     }
}

interface I {
    def k(a)
    def m(a,b)
}

interface J extends I {
    def k(a)
    def m(a,b,c)
}
