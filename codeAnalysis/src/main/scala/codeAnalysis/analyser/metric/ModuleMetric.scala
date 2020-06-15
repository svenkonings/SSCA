package main.scala.analyser.metric

import codeAnalysis.analyser.AST._
import main.scala.analyser.result.MetricResult

trait ModuleMetric extends ObjectMetric{
  /**
    * Function to execute the module metrics
    *
    * @param tree the ast tree of the module
    * @param code the code of the module
    * @return
    */
  def metric(tree: Module, code: List[String]): List[MetricResult]

  /**
    * Function to execute the object metrics
    *
    * @param tree the ast tree of the object
    * @param code the code of the object
    * @return
    */
  override def run(tree: ObjectDefinition, code: List[String]): List[MetricResult] = metric(tree, code)

  /**
    * Function to execute the class metrics
    *
    * @param tree the ast tree of the class
    * @param code the code of the class
    * @return
    */
  override def run(tree: ClassDefinition, code: List[String]): List[MetricResult] = metric(tree, code)

  /**
    * Function to execute the trait metrics
    *
    * @param tree the ast tree of the trait
    * @param code the code of the trait
    * @return
    */
  override def run(tree: TraitDefinition, code: List[String]): List[MetricResult] = metric(tree, code)
}
