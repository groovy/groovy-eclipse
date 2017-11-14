package p

import static p.OuterAgain.InnerAgain.*
import static p.Outer.Inner.*
import p.OuterAgain.InnerAgain
import p.Outer.Inner
import p.OuterAgain.*
import p.Outer.*

class Outer {
  class Inner {
    final String value = ''
  }
}

class OuterAgain {
    class InnerAgain {
        final String thing = ''
    }
}

println OuterAgain.InnerAgain.thing
println p.Outer.Inner.value
println Outer.Inner.value
println Inner.value
println value
