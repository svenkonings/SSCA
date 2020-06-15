class LCTest1 {
  val lambda: () => String = () => "Hello"
}

class LCTest2 {
  def passLambda(list: List[Any]): List[Any] = list.filter(value => value != null)
}

class LCTest3 {
  def sugarLambda(list: List[String]): List[String] = list.filter(_.isEmpty)
}

class LCTest4 {
  val lambda: () => String = () => "Hello"
  def passLambda(list: List[Any]): List[Any] = list.filter(value => value != null)
}

class LCTest4 {
  val lambda: () => String = () => "Hello"
  def passLambda(list: List[?]): List[Any] = list.filter(value => value != null)
}

class LCTest5 {
  val something: String = "Hello"
}
