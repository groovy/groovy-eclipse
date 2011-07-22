package p

class A implements I1 {
     def m(a) {
         m(a)
         m(a,b)
         m(a,b,c)
     }
     def m(a,b) {
         I1 i 
         i.m()
         i.m(a)
         i.m(a,b)
         i.m(a,b,c)
         I2 i 
         j.m()
         j.m(a)
         j.m(a,b)
         j.m(a,b,c)
     }
}

interface I1 {
    def m(a)
    def m(a,b)
}

interface I2 {
    def m(a)
    def m(a,b,c)
}
