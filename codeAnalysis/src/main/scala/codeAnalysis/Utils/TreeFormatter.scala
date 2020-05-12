import scala.collection.mutable.ArrayBuffer

object TreeFormatter {
  def main(args: Array[String]): Unit = println(format(args.mkString))

  def format(tree: String, indent: Int = 0): String = {
    val firstRound = tree.indexOf('(')
    val firstSquare = tree.indexOf('[')
    if (firstRound == -1 && firstSquare == -1)
      " " * indent + tree
    else if (firstSquare == -1 || firstRound < firstSquare)
      formatBraces(tree, firstRound, indent)
    else
      formatBraces(tree, firstSquare, indent)
  }

  private def formatBraces(tree: String, start: Int, indent: Int): String = {
    val braces = tree.substring(start + 1, tree.length - 1)
    if (braces.contains(',')) {
      val header = " " * indent + tree.substring(0, start + 1) + "\n"
      val body = split(braces, ',').map(e => format(e, indent + 4)).mkString(",\n")
      val footer = "\n" + " " * indent + tree.substring(tree.length - 1)
      header + body + footer
    } else {
      " " * indent + tree
    }
  }

  private def split(tree: String, split: Char): List[String] = {
    var braceCount = 0
    val result = new ArrayBuffer[String]()
    val currentSplit = new StringBuilder
    for (char <- tree) {
      if (char == '(' || char == '[') braceCount += 1
      if (char == ')' || char == ']') braceCount -= 1
      if (char == split && braceCount == 0) {
        result += currentSplit.mkString
        currentSplit.clear()
      } else {
        currentSplit += char
      }
    }
    result += currentSplit.mkString
    result.toList
  }
}
