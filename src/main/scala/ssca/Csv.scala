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
    val metrics = List(new FunctionalMetrics)
    val an = new Analyser(metrics, repoPath, 8, includeTest = false)

    val fileResults = an.analyse().map(_.asInstanceOf[FileResult])

    println("Calculating Object Scores...")
    objectScores(fileResults, path)
  }

  def objectScores(fileResults: List[FileResult], path: Path): Unit = {
    val objectResults = fileResults.flatMap(_.allObjects).filter(_.functions.flatMap(_.flatten()).nonEmpty)
    val header = "Object,Average,Sum,Max"
    val body = for (objectResult <- objectResults) yield {
      val name = objectResult.name
      // Only look at functions of current object and not nested objects
      val scores = objectResult.functions.flatMap(_.flatten()).map(_.value)
      val sum = scores.sum
      val average = sum / scores.size
      val max = scores.max
      s"$name,$average,$sum,$max"
    }
    val csv = (header :: body).mkString("\n")

    write(path.resolve("objects.csv"), csv)
  }

  def write(path: Path, contents: String): Unit =
    Files.write(path, contents.getBytes(StandardCharsets.UTF_8))
}
