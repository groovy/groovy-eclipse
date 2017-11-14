package p

import p.Outer.B
import p.Outer.AZ

class Outer {
  class B {
  }

  class AZ {
  }

  Outer.B f
        B g
}

Outer.AZ az
      AZ
Outer.B f
      B g
Outer.B
      B
