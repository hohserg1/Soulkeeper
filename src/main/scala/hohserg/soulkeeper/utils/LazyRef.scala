package hohserg.soulkeeper.utils

class LazyRef[A](v: => A) {
  lazy val value: A = v
}