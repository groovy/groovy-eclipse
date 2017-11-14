package p

import static p.OuterAgain.InnerAgain.*
import static p.Wrapper.Inner.*
import p.OuterAgain.InnerAgain
import p.Wrapper.Inner
import p.OuterAgain.*
import p.Wrapper.*

class Wrapper {
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
println p.Wrapper.Inner.value
println Wrapper.Inner.value
println Inner.value
println value
