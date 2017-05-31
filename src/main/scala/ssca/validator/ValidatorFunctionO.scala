package ssca.validator

import codeAnalysis.analyser.result.{FunctionResult, ObjectResult, ResultUnit}
import dispatch.Http
import gitCrawler.{Commit, Fault, Repo, RepoInfo}
import main.scala.analyser.Analyser
import main.scala.analyser.metric.Metric

/**
  * Created by erikl on 4/25/2017.
  */
class ValidatorFunctionO(repoUser: String, repoName: String, repoPath: String, instances: Int,
                         instanceThreads: Int, metrics: List[Metric], labels: List[String], branch: String)
  extends Validator(repoPath, metrics){

  private val instanceIds: List[Int] = List.range(0, instances)
  private var totalCount = 0

  def run(): Unit = {
    /* Writes the headers to the file */
    writeFunctionHeaders()

    val repoInfo = new RepoInfo(repoUser, repoName, token, labels, "master", repoPath)

    val faultyClasses = instanceIds.par.map(x => runInstance(x, repoInfo)).foldLeft(List[String]())((a, b) => a ::: b)

    val repo = new Repo(repoUser, repoName, repoPath + "0", branch, repoInfo)
    println("Done loading repo")

    val an = new Analyser(createMetrics(), repoPath + "0", instanceThreads)
    println("Done init analyser")

    repo.checkoutHead()
    an.refresh()

    val results = an.analyse()


    val tmpOutput = getOutput(results, faultyClasses)

    val output = tmpOutput.map(x => x.replaceAll(repoPath.replace("\\", "\\\\") + """\d""", repoPath))

    writeFullOutput(output)

    closeOutputs()
    Http.shutdown()
  }

  private def runInstance(id: Int, repoInfo: RepoInfo): List[String] = {
    val instancePath = repoPath + id

    /* Init the repo for the instance */
    val repo = new Repo(repoUser, repoName, instancePath, branch, repoInfo)
    println("Done loading repo: " + id)

    /* Init the analyser for the instance */
    val an = new Analyser(createMetrics(), instancePath, instanceThreads)
    println("Done init codeAnalysis.analyser: " + id)

    /* Get the faults and select the correct chunk. */
    val faults = repoInfo.faults
    val chunk = faults.grouped(math.ceil(faults.length.toDouble / instances).toInt).toList(id)

    var count = 0
    var prevCommit: Commit = null

    println("Start => " + id)
    /* Analyse each fault. */
    val res = chunk.foldLeft(List[String]()) {
      (r, x) =>
        /* Commit to previous commit. */
        repo.checkoutPreviousCommit(x.commit)
        an.refresh()

        /* Get the result. */
        val results = an.analyse(x.commit.files.map(instancePath + "\\" + _))

        /* Run output function. */
        val output = getFaultyFunctions(instancePath, x, results).map(x => x.replaceAll(repoPath.replace("\\", "\\\\") + """\d""", repoPath))

        count += 1

        outputLock.acquire()
        totalCount += 1
        outputLock.release()

        val nextSha = {
          val index = chunk.indexOf(x) + 1
          if (index < chunk.length)
            chunk(index).commit.sha
          else
            "Last"
        }

        println(id + ":\t" + count + "/" + chunk.length + "(" + (count * 100) / chunk.length + "%)\t\tTotal: "
          + totalCount + "/" + faults.length + "(" + (totalCount * 100) / faults.length + "%)\t\t"
          + results.length + "\t\t=>\t" + x.commit.sha + "\t->\t" + nextSha)

        prevCommit = x.commit
        x.unload()
        r ::: output
    }
    println(id + " Done!")
    an.close()
    res
  }


  def getFaultyFunctions(instancePath: String, fault: Fault, results: List[ResultUnit]): List[String] = {

    def recursive(results: List[ResultUnit]) : List[String] = results match {
      case Nil =>
        List()
      case x::tail =>
        val lines = fault.commit.getPatchData(x.position.source.path.substring(instancePath.length + 1).replace("\\", "/"))
        x match {
          case func: FunctionResult =>
            lines match {
              case Some(patch) =>
                if (func.includes(patch._1, patch._2) || func.includes(patch._3, patch._4)) {
                  func.functionPath :: recursive(func.functions) ::: recursive(func.objects) ::: recursive(tail)
                } else {
                  recursive(func.functions) ::: recursive(func.objects) ::: recursive(tail)
                }
              case _ =>
                recursive(func.functions) ::: recursive(func.objects) ::: recursive(tail)
            }
          case y: ResultUnit =>
            recursive(y.functions) ::: recursive(y.objects) ::: recursive(tail)
          case _ =>
            recursive(tail)
        }

    }
    recursive(results)
  }

  def getOutput(results: List[ResultUnit], faultyClasses: List[String]): List[String] = {
    var counter = 0
    def recursive(results: List[ResultUnit]) : List[String] = results match {
      case Nil =>
        List()
      case x::tail =>
        x match {
          case func: FunctionResult =>
            val count = faultyClasses.count(x => x == func.functionPath.replaceAll(repoPath.replace("\\", "\\\\") + """\d""", repoPath))
            if (count > 0) counter += 1
            "HEAD," + count + "," + func.toCSV(headerLength) :: recursive(tail)
          case y: ResultUnit =>
            recursive(y.functions) ::: recursive(y.objects) ::: recursive(tail)
          case _ =>
            recursive(tail)
        }
    }

    val output = recursive(results)
    println("Functions found: " + counter + "/" + faultyClasses.distinct.length + "(" + counter.toDouble / faultyClasses.distinct.length.toDouble + ")")
    println("Fault percentage: " + counter + "/" + output.length + "(" + counter.toDouble / output.length.toDouble + ")")
    output
  }

  override def headerLength: Int = {
    functionHeaders.length
  }
}