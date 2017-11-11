package p

interface I {
  int sam()
}

class A {
  private I f = new I() {
    @Override
    int sam() { }
  }
  I p = new I() {
    @Override
    int sam() { }
  }
  def m() {
    I local = new I() {
      @Override
      int sam() { }
    }
  }
}
