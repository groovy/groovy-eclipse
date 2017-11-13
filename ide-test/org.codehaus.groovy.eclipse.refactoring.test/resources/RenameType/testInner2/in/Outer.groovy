package p

import static p.Outer.Inner.*
import p.Outer.*

class Outer {
  class Inner {
    final String value = ''
  }
}

def val = p.Outer.Inner.value // FIXME
val = Outer.Inner.value // FIXME
val = Inner.value
val = value
