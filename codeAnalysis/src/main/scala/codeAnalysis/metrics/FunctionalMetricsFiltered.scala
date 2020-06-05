package codeAnalysis.metrics

import codeAnalysis.Utils.FunctionalUtil
import codeAnalysis.analyser.AST.MethodDef
import main.scala.analyser.metric.FunctionMetric
import main.scala.analyser.result.MetricResult

/**
  * Created by erikl on 6/1/2017.
  */
class FunctionalMetricsFiltered extends FunctionMetric with FunctionalUtil {
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
    if (result.funcPoints == 0 && result.impPoints == 0)
      List()
    else
      List(new MetricResult(tree.pos, tree.name, "ParadigmScore", result.paradigmScore))
  }
}
