package ssca

import java.util.{Calendar, Date, GregorianCalendar}

import codeAnalysis.STimer
import codeAnalysis.metrics._
import dispatch.Http
import ssca.validator._


/**
  * Created by Erik on 13-4-2017.
  */
object Main {

  def main(args: Array[String]): Unit = {
    analyseAkka()
    analyseGitbucket()
    analyseShadowsocks()
    Http.shutdown()
  }

  def analyseAkka(): Unit = analyse(
    "..\\akkaNew2",
    "akka",
    "akka",
    "bb7727dee44364a6dff31ee99cad9ae3e6fe9830",
    List("bug"),
    new GregorianCalendar(2017, Calendar.JUNE, 13, 23, 59).getTime
  )

  def analyseGitbucket(): Unit = analyse(
    "gitbucket",
    "gitbucket",
    "..\\gitbucketNew2",
    "53ae59271a3b5b832e3a7045e2b58205ca300d2a",
    List("bug"),
    new GregorianCalendar(2017, Calendar.JULY, 27, 23, 59).getTime
  )

  def analyseShadowsocks(): Unit = analyse(
    "shadowsocks",
    "shadowsocks-android",
    "..\\shadowsockNew",
    "398db4f40716cd91f86f8c07a57625af9ce2c696",
    List("bug"),
    new GregorianCalendar(2017, Calendar.JULY, 19, 23, 59).getTime
  )

  def analyse(repoPath: String, repoUser: String, repoName: String, branch: String, labels: List[String], collectDate: Date): Unit = {
    val metrics = List(new FunctionalMetrics)

    val validatorN = new ValidatorNObject(repoPath, repoUser, repoName, branch, labels, 3, 5, metrics, "fullOutput" + "New", collectDate)
    STimer.time("Analysis", validatorN.run())

    val validatorO = new ValidatorOObject(repoPath, repoUser, repoName, branch, labels, 3, 5, metrics, "fullOutput" + "Old", collectDate)
    STimer.time("Analysis", validatorO.run())
  }
}
