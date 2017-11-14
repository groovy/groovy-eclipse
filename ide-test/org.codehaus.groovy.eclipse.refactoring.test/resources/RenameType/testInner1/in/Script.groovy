package p

import p.Outer.A
import p.Outer.AZ

class Outer {
  class A {
  }

  class AZ {
  }

  Outer.A f
        A g
}

Outer.AZ az
      AZ
Outer.A f
      A g
Outer.A
      A
