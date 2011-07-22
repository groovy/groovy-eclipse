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
     }
}

interface I1 {
    def m(a)
    def m(a,b)
}
