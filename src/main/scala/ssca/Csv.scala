package ssca

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path, Paths}

import codeAnalysis.analyser.result.FileResult
import codeAnalysis.metrics._
import gitCrawler.Repo
import main.scala.analyser.Analyser

object Csv {

  def main(args: Array[String]): Unit = {
    if (args.length != 3 || args(0).count('/'.==) != 1) {
      println("Usage: username/reponame branch path")
      sys.exit(1)
    }
    val Array(username, reponame) = args(0).split('/')
    val branch = args(1)
    val path = args(2)
    analyse(username, reponame, path, branch)
  }

  def analyse(userName: String, repoName: String, repoPath: String, branch: String): Unit = {
    val path = Paths.get(repoPath)

    println("Cloning repo...")
    val repo = new Repo(userName, repoName, repoPath, branch, null)
    repo.checkoutHead()

    println("Analysing...")
    val metrics = List(new FunctionalMetricsFiltered)
    val an = new Analyser(metrics, repoPath, 8, includeTest = false)

    val fileResults = an.analyse().map(_.asInstanceOf[FileResult])

    println("Calculating Function Scores...")
    functionScores(repoName, fileResults, path)
    println("Calculating Object Scores...")
    objectScores(repoName, fileResults, path)
  }

  def functionScores(repoName: String, fileResults: List[FileResult], path: Path): Unit = {
    val header = "Function,Score"
    val body = for (
      fileResult <- fileResults;
      functionResult <- fileResult.allFunctions;
      metricResult <- functionResult.flatten()
    ) yield {
      val name = functionResult.name
      val score = metricResult.value
      s"$name,$score"
    }
    val csv = (header :: body).mkString("\n")

    write(path.resolve(s"${repoName}Functions.csv"), csv)
  }

  def objectScores(repoName: String, fileResults: List[FileResult], path: Path): Unit = {
    val header = "Object,Average"
    val body = for (
      fileResult <- fileResults;
      objectResult <- fileResult.allObjects
      if objectResult.functions.flatMap(_.flatten()).nonEmpty
    ) yield {
      val name = objectResult.name
      // Only look at functions of current object and not nested objects
      val scores = objectResult.functions.flatMap(_.flatten()).map(_.value)
      val average = scores.sum / scores.size
      s"$name,$average"
    }
    val csv = (header :: body).mkString("\n")

    write(path.resolve(s"${repoName}Objects.csv"), csv)
  }

  def write(path: Path, contents: String): Unit =
    Files.write(path, contents.getBytes(StandardCharsets.UTF_8))
}
