package hohserg.soulkeeper.capability

import java.util.concurrent.Callable

object DummyFactory{
  def apply[A](f: () => A): Callable[A] = new Callable[A] {
    override def call(): A = f()
  }
}
