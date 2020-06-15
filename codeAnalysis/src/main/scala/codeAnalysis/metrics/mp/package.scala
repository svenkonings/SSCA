package codeAnalysis.metrics

import codeAnalysis.analyser.AST.AST

package object mp {
  def countRecursive(tree: AST, countFunc: AST => Int): Int =
    tree.children.foldLeft(countFunc(tree))((prev, child) => prev + countRecursive(child, countFunc))
}
