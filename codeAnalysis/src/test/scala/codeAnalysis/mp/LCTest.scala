package codeAnalysis.mp

import codeAnalysis.TestSpecs.UnitSpec
import codeAnalysis.analyser.result.ResultUnit
import codeAnalysis.metrics.mp.LC
import main.scala.analyser.Analyser
import main.scala.analyser.metric.Metric

class LCTest extends UnitSpec{
  var metrics: List[Metric] = List(new LC)
  var analyser: Analyser = new Analyser(metrics, testRoot, 1)
  var result: ResultUnit = analyser.analyse(testRoot + "TestFileLC.scala")

  def getClassValue(name: String, metric: String): Int = {
    result.getClassByName(name).get.getMetricByName(metric).get.value.toInt
  }

  test("Single lambda") {
    val className = "LCTest1"
    val lc = getClassValue(className, "LambdasPerClass")
    assert(lc == 1)
  }

  test("Passed Lambda") {
    val className = "LCTest2"
    val lc = getClassValue(className, "LambdasPerClass")
    assert(lc == 1)
  }

  test("Sugar lambda") {
    val className = "LCTest3"
    val lc = getClassValue(className, "LambdasPerClass")
    assert(lc == 1)
  }

  test("Double lambda") {
    val className = "LCTest4"
    val lc = getClassValue(className, "LambdasPerClass")
    assert(lc == 2)
  }

  test("No lambda") {
    val className = "LCTest5"
    val lc = getClassValue(className, "LambdasPerClass")
    assert(lc == 0)
  }
}
