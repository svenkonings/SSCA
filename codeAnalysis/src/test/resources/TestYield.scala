class TestYield {
  def myYield: Int = for (i <- Range(0,10)) yield i

  def myFor: Unit = for (i <- Range(0,10)) println(i)
}