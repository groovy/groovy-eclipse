package p

class C {
  List<B> list1
  List<? super B> list2
  List<? extends B> list3
  List<Collection<B>> list4

  static class D implements Comparable<B> {
      int compareTo(B that) {
          0
      }
  }
}
