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
         i.k(a,b,c)
         I2 i 
         j.m()
         j.m(a)
         j.m(a,b)
         j.m(a,b,c)
     }
}

interface I1 {
    def k(a)
    def m(a,b)
}

interface I2 {
    def m(a)
    def m(a,b,c)
}
