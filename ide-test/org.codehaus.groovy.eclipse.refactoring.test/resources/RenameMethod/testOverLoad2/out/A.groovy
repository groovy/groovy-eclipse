package p

class A implements I2 {
     def k(a) {
         k(a)
         m(a,b)
         m(a,b,c)
     }
     def m(a,b) {
         I1 i 
         i.k()
         i.k(a)
         i.m(a,b)
         i.k(a,b,c)
         I2 j 
         j.k()
         j.k(a)
         j.m(a,b)
         j.m(a,b,c)
     }
}

interface I1 {
    def k(a)
    def m(a,b)
}

interface I2 extends I1 {
    def k(a)
    def m(a,b,c)
}
