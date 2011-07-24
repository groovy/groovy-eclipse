package p

class A implements I1 {
     def k(a) {
         k(a)
         m(a,b)
         k(a,b,c)
     }
     def m(a,b) {
         I1 i 
         i.k()
         i.k(a)
         i.m(a,b)
     }
}

interface I1 {
    def k(a)
    def m(a,b)
}
