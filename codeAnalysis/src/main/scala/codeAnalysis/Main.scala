package codeAnalysis

import codeAnalysis.metrics._
import main.scala.analyser.Analyser

/**
  * Created by Erik on 5-4-2017.
  */
object Main {

  def main(args: Array[String]): Unit = {
    val metrics = List(new FunctionalMetrics, new FunctionalMetricsLandkroon)
    val an = new Analyser(metrics, "C:\\Users\\SvenK\\Documents\\akka", 6)
    val results = an.analyse()
    val groupedResults = results.flatMap(_.flatten()).groupBy(_.metricName)
    for ((metricName, metricResults) <- groupedResults) {
      printStats(metricName, metricResults.map(_.value))
    }
  }

  def printStats(name: String, values: List[Double]): Unit = {
    val average = values.sum / values.length
    val min = values.min
    val max = values.max
    println(f"Name: $name,\taverage: $average,\tmin: $min,\tmax: $max")
  }

}
