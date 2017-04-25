package gitCrawler

import java.io.File
import java.nio.file.Paths

import dispatch.github.{GhCommit, GhIssue}
import org.eclipse.jgit.api.{Git}
import org.eclipse.jgit.diff.DiffEntry
import org.eclipse.jgit.treewalk.CanonicalTreeParser

/**
  * Created by ErikL on 4/11/2017.
  */
class Repo(userName: String, repoName: String, token: String, labels: List[String], repoPath: String) {

  val debug = true
  val debugTreshhold = 15

  val git: Git = initGitRepo
  val repoInfo = Map("user" -> userName, "repo" -> repoName, "token" -> token, "repoPath" -> repoPath)

  val commits: List[Commit] = getCommits
  val issues: List[Issue] = getIssues
  val faults: List[Fault] = getFaults

  /**
    * Initializes the repository
    *
    * @return
    */
  private def initGitRepo: Git = {
    val file = new File(repoPath)
    if (!file.exists())
      GitR.runCommand(Paths.get(file.getParent), "git", "clone", "git@github.com:" + userName + "/" + repoName + ".git", repoPath)
    Git.open(file)
  }


  /**
    * Gets a list of all the commits from the repository
    *
    * @return
    */
  private def getCommits: List[Commit] = {
    def recursive(page: Int) : List[Commit] = {
      val commitsRes = GhCommit.get_commits(userName, repoName,  Map("page" -> page.toString, "access_token" -> token, "per_page" -> "100"))()
      val commits = commitsRes.foldLeft(List[Commit]())((a, b) => a ::: List(new Commit(b, repoInfo, null)))
      if (commits.isEmpty || (debug && page > debugTreshhold))
        commits
      else
        commits ::: recursive(page + 1)
    }
    recursive(1)
  }


  /**
    * Gets a list of all the issues from the repository
    *
    * @return
    */
  private def getIssues: List[Issue] = {
    def recursive(page: Int, label: String) : List[Issue] = {
      val issuesRes = GhIssue.get_issues(userName, repoName,  Map("page" -> page.toString, "per_page" -> "100", "access_token" -> token, "state" -> "all", "labels" -> label))()
      val issues = issuesRes.foldLeft(List[Issue]())((a, b) => a ::: List(new Issue(b)))
      if (issues.isEmpty || (debug && page > debugTreshhold))
        issues
      else
        issues ::: recursive(page + 1, label)
    }

    labels.foldLeft(List[Issue]())((a,b) => a ::: recursive(1, b))
  }


  /**
    * Gets a list of the fauls in the repository (combination of a commit and an issue)
    *
    * @return
    */
  private def getFaults: List[Fault] = {
    commits.filter(isIssue).foldLeft(List[Fault]())((a, b) => a ::: List(new Fault(b, getIssues(b))))
  }


  /**
    * Checks if a commit fixes a issue or not
    *
    * @param commit the commit
    * @return
    */
  private def isIssue(commit: Commit) : Boolean = {
    val pattern = """(?i)(clos(e[sd]?|ing)|fix(e[sd]|ing)?|resolv(e[sd]?)|#(\d+))""".r

    if ((pattern findAllIn commit.message).isEmpty)
      return false

    if (getIssues(commit).isEmpty)
      return false
    true
  }


  /**
    * Gets a list of issues that are mentioned as fixed in the commit
    *
    * @param commit the commit
    * @return
    */
  private def getIssues(commit: Commit) : List[Issue] = {
    val pattern = """#(\d+)""".r
    val possibleNumbers = pattern findAllIn commit.message

    if (possibleNumbers.isEmpty)
      return List[Issue]()

    val numbers = possibleNumbers.matchData.map(x => x.group(1).toInt).toList

    issues.filter(x => numbers contains x.number)
  }

  /**
    * Checkout to the commit
    *
    * @param commit the commit
    */
  def checkoutCommit(commit: Commit): Unit = {
    GitR.runCommand(Paths.get(repoPath), "git", "reset", "--hard")
    GitR.runCommand(Paths.get(repoPath), "git", "checkout", "-f", commit.sha)
  }


  /**
    * Checkout to the previous commit
    *
    * @param commit the commit
    */
  def checkoutPreviousCommit(commit: Commit): Unit = {
    GitR.runCommand(Paths.get(repoPath), "git", "reset", "--hard")
    GitR.runCommand(Paths.get(repoPath), "git", "checkout", "-f", getPreviousCommitSha(commit))
  }

  /**
    * Get The previous commit SHA
    *
    * @param commit the commit
    */
  def getPreviousCommitSha(commit: Commit): String = {
    commit.commitData.parents.head.sha
  }


  /**
    * Gets the file names that differ between two commits
    *
    * @param commit1 First commit
    * @param commit2 Second Commit
    * @return
    */
  def changedFiles(commit1: Commit, commit2: Commit) : List[String] = {
    try {
      val repository = git.getRepository

      val oldHead = repository.resolve(commit1.commitData.commit.tree.sha)
      val head = repository.resolve(commits.find(x => x.sha == getPreviousCommitSha(commit2)).get.commitData.commit.tree.sha)

      try {
        val reader = repository.newObjectReader()

        val oldTreeIter = new CanonicalTreeParser()
        oldTreeIter.reset(reader, oldHead)

        val newTreeIter = new CanonicalTreeParser()
        newTreeIter.reset(reader, head)

        try {
          val diffs = git.diff()
            .setNewTree(newTreeIter)
            .setOldTree(oldTreeIter)
            .call()
          return diffs.toArray[DiffEntry](Array[DiffEntry]()).filter(x => x.getChangeType != DiffEntry.ChangeType.DELETE)
            .foldLeft(List[String]())((a, b) => a ::: List(b.getNewPath)).filter(f => """.*\.scala$""".r.findFirstIn(f).isDefined)

        }catch {
          case _: Throwable =>
        }
      }
    }catch {
      case _: Throwable =>
    }
    List()
  }

}