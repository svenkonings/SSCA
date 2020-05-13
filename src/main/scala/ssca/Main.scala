package ssca

import codeAnalysis.STimer
import codeAnalysis.metrics._
import ssca.validator._


/**
  * Created by Erik on 13-4-2017.
  */
object Main {
  def main(args: Array[String]): Unit = {
    val repoUser = "akka"
    val repoName = "akka"
    val repoPath = "..\\akka00test"
    val project = "HTTP"
    val version = "Final"
    val branch = "bb7727dee44364a6dff31ee99cad9ae3e6fe9830"
    val labels = List("bug")
//    val repoUser = "gitbucket"
//    val repoName = "gitbucket"
//    val repoPath = "..\\gitbucket00test"
//    val project = "GitBucket"
//    val version = "Landkroon"
//    val branch = "53ae59271a3b5b832e3a7045e2b58205ca300d2a"
//    val labels = List("bug")
//    val repoUser = "shadowsocks"
//    val repoName = "shadowsocks-android"
//    val repoPath = "..\\shadowsock0"
//    val project = "Shadowsocks"
//    val version = "Landkroon"
//    val branch = "398db4f40716cd91f86f8c07a57625af9ce2c696"
//    val labels = List("bug")

    val metrics = List(new FunctionalMetrics)

    val validator = new ValidatorNObject(repoPath, repoUser, repoName, branch, labels, 3, 5, metrics)

    //STimer.time("Analysis", validator.run(validator.writeHeaders, validator.objectOutput))
    STimer.time("Analysis", validator.run())
  }
}
