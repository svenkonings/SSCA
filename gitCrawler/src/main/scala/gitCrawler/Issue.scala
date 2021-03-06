package gitCrawler

import java.util.Date

import dispatch.github.GhIssue
/**
  * Created by ErikL on 4/11/2017.
  */
class Issue(issue: GhIssue) {
  def state: String = issue.state
  def number: Int = issue.number
  def title: String = issue.title
  def date: Date = issue.created_at
}
