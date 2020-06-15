package codeAnalysis.metrics.mp

import codeAnalysis.analyser.AST._
import main.scala.analyser.metric.ModuleMetric
import main.scala.analyser.result.MetricResult

/**
  * Source Lines of Lambda
  */
class SLOL extends ModuleMetric {
  override def objectHeader: List[String] = List("SourceLinesOfLambda")

  def countLambdaLines(tree: AST): Int = countRecursive(tree, {
    case f: FunctionDef => f.children.reverse.head.pos.line - f.pos.line
    case _ => 0
  })

  override def metric(tree: Module, code: List[String]): List[MetricResult] =
    List(new MetricResult(tree.pos, tree.name, "SourceLinesOfLambda", countLambdaLines(tree)))
}
