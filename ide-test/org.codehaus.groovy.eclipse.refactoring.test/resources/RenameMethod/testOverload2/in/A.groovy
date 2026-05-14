package p

class A implements J {
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
         i.m(a,b,c)
         J j
         j.m()
         j.m(a)
         j.m(a,b)
         j.m(a,b,c)
     }
}

interface I {
    def m(a)
    def m(a,b)
}

interface J extends I {
    def m(a)
    def m(a,b,c)
}
