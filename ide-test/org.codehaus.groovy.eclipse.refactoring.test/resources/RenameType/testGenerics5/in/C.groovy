package p

class C {
  List<A> list1
  List<? super A> list2
  List<? extends A> list3
  List<Collection<A>> list4

  static class D implements Comparable<A> {
      int compareTo(A that) {
          0
      }
  }
}
