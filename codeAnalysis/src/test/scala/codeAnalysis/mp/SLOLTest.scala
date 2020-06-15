package codeAnalysis.mp

import codeAnalysis.TestSpecs.UnitSpec
import codeAnalysis.analyser.result.ResultUnit
import codeAnalysis.metrics.mp.SLOL
import main.scala.analyser.Analyser
import main.scala.analyser.metric.Metric

class SLOLTest extends UnitSpec{
  var metrics: List[Metric] = List(new SLOL)
  var analyser: Analyser = new Analyser(metrics, testRoot, 1)
  var result: ResultUnit = analyser.analyse(testRoot + "TestFileSLOL.scala")

  def getClassValue(name: String, metric: String): Int = {
    result.getClassByName(name).get.getMetricByName(metric).get.value.toInt
  }

  test("Single lambda") {
    val className = "SLOLTest1"
    val SLOL = getClassValue(className, "SourceLinesOfLambda")
    assert(SLOL == 3)
  }
}
