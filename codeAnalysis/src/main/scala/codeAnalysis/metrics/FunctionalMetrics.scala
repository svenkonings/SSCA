package codeAnalysis.metrics

import codeAnalysis.Utils.FunctionalUtil
import codeAnalysis.analyser.AST.MethodDef
import main.scala.analyser.metric.FunctionMetric
import main.scala.analyser.result.MetricResult

/**
  * Created by erikl on 6/1/2017.
  */
class FunctionalMetrics extends FunctionMetric with FunctionalUtil {
  override def functionHeader: List[String] = List(/*"Recursive", "Nested", "HigherOrderParams", "SideEffects", "FunctionalCalls", "ImperativeCalls", "FunctionalPoints", "ImperativePoints", */"ParadigmScore")

  /**
    * Function that should be called to run a function metric
    *
    * @param tree the ast of the function
    * @param code the code of the function
    * @return
    */
  override def run(tree: MethodDef, code: List[String]): List[MetricResult] = {
    val result = paradigmScore(tree)
    List(
//      new MetricResult(tree.pos, tree.name, "Recursive", result.recursive),
//      new MetricResult(tree.pos, tree.name, "Nested", result.nested),
//      new MetricResult(tree.pos, tree.name, "HigherOrderParams", result.higherOrderParams),
//      new MetricResult(tree.pos, tree.name, "SideEffects", result.sideEffects),
//      new MetricResult(tree.pos, tree.name, "FunctionalCalls", result.funcCalls),
//      new MetricResult(tree.pos, tree.name, "ImperativeCalls", result.impCalls),
//      new MetricResult(tree.pos, tree.name, "FunctionalPoints", result.funcPoints),
//      new MetricResult(tree.pos, tree.name, "ImperativePoints", result.impPoints),
      new MetricResult(tree.pos, tree.name, "ParadigmScore", result.funcPercent)
    )
  }
}
