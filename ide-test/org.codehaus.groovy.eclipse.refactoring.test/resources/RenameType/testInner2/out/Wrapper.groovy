package p

import static p.Wrapper.Inner.*
import p.Wrapper.*

class Wrapper {
  class Inner {
    final String value = ''
  }
}

def val = p.Outer.Inner.value // FIXME
val = Outer.Inner.value // FIXME
val = Inner.value
val = value
