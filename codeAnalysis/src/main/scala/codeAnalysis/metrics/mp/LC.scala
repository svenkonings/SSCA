package codeAnalysis.metrics.mp

import codeAnalysis.analyser.AST._
import main.scala.analyser.metric.ModuleMetric
import main.scala.analyser.result.MetricResult

/**
  * Number of Lambda Functions Used in a Class
  */
class LC extends ModuleMetric {
  override def objectHeader: List[String] = List("LambdasPerClass")

  def countLambdas(tree: AST): Int = countRecursive(tree, {
    case _: FunctionDef => 1
    case _ => 0
  })

  override def metric(tree: Module, code: List[String]): List[MetricResult] =
    List(new MetricResult(tree.pos, tree.name, "LambdasPerClass", countLambdas(tree)))
}
