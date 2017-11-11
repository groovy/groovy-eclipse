package p

interface I {
  int run()
}

class A {
  private I f = new I() {
    @Override
    int run() { }
  }
  I p = new I() {
    @Override
    int run() { }
  }
  def m() {
    I local = new I() {
      @Override
      int run() { }
    }
  }
}
