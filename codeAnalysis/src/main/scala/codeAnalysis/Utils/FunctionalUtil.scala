package codeAnalysis.Utils

import codeAnalysis.analyser.AST._

/**
  * Created by Erik on 14-4-2017.
  */
trait FunctionalUtil {
  private val functionalFuncs = List("foldLeft", "foldRight", "fold", "map", "filter", "count", "exist", "find")
  private val impFuncs = List("foreach")

  /**
    * Checks whether the function is recursive or not
    *
    * @param tree the ast
    * @return
    */
  def isRecursive(tree: AST): Boolean = {
    def recursive(tree: AST, functionName: String): Boolean = tree match {
      case node: FunctionCall =>
        if (node.owner + "." + node.name == functionName)
          true
        else
          tree.children.exists(x => recursive(x, functionName))
      case _ =>
        tree.children.exists(x => recursive(x, functionName))
    }

    tree match {
      case x: MethodDef =>
        recursive(x, x.owner + "." + x.name)
      case _ =>
        false
    }
  }

  /**
    * Counts the amount of side effects in a tree (var's)
    *
    * @param tree the ast
    * @return
    */
  def countSideEffects(tree: AST): Int = tree match {
    case (_: Var) | (_: VarAssignment) | (_: VarDefinition) =>
      1
    case _ =>
      tree.children.foldLeft(0)((a, b) => a + countSideEffects(b))
  }


  /**
    * Checks if a function is nested
    *
    * @param tree
    * @return
    */
  def isNested(tree: MethodDef): Boolean = {
    tree.nested
  }

  /**
    * Counts the number of functional function calls
    *
    * @param tree
    * @return (functional, imperative)
    */
  def countFuncCalls(tree: AST): (Int, Int) = {
    implicit class TupleAdd[A: Numeric, B: Numeric](t: (A, B)) {

      import Numeric.Implicits._

      def +(p: (A, B)): (A, B) = (p._1 + t._1, p._2 + t._2)
    }
    def score(x: AST): (Int, Int) = x match {
      case x: FunctionCall =>
        val funcPoints = if (x.higher || x.params.exists(_.higher)) 1 else 0
        val impPoints = if (x.typeString.contains("Unit") || x.params.exists(_.typeString.contains("Unit"))) 1 else 0
        (funcPoints, impPoints)
      case x: FunctionDef =>
        val funcPoints = if (x.higher || x.params.exists(_.higher)) 2 else 1 // Functions get 1 funcPoint by default
        val impPoints = if (x.typeString.contains("Unit") || x.params.exists(_.typeString.contains("Unit"))) 1 else 0
        (funcPoints, impPoints)
      case x: Value if x.isLazy => (1, 0)
      case _: MatchCase => (1, 0)
      case _: For => (0, 1)
      case _: While => (0, 1)
      case _: DoWhile => (0, 1)
      case _ => (0, 0)
    }

    def recursive(x: AST): (Int, Int) = score(x) + x.children.foldLeft((0, 0))((cur, tree) => cur + recursive(tree))

    recursive(tree)
  }

  /**
    * Count the amount of higher order functions in the params or return type of a function
    *
    * @param tree the function
    * @return
    */
  def countHigherOrderParams(tree: MethodDef): Int = {
    def recursive(params: List[Param]): Int = params match {
      case Nil =>
        0
      case x :: tail =>
        if (x.higher)
          1 + recursive(tail)
        else
          0 + recursive(tail)
      case _ =>
        0
    }

    recursive(tree.params)
  }

  def paradigmScore(tree: MethodDef): ParadigmScore = {
    // Functional
    val recursive = if (isRecursive(tree)) 1 else 0
    val nested = if (isNested(tree)) 1 else 0
    val higherOrderParams = countHigherOrderParams(tree)

    // Imperative
    val sideEffects = countSideEffects(tree)

    // Both
    val (funcCalls, impCalls) = countFuncCalls(tree)

    ParadigmScore(recursive, nested, higherOrderParams, sideEffects, funcCalls, impCalls)
  }
}

case class ParadigmScore(
                          recursive: Int,
                          nested: Int,
                          higherOrderParams: Int,
                          sideEffects: Int,
                          funcCalls: Int,
                          impCalls: Int
                        ) {
  val funcPoints: Int = recursive + nested + higherOrderParams + funcCalls
  val impPoints: Int = sideEffects + impCalls
  val paradigmScore: Double = if (funcPoints + impPoints != 0)
    (funcPoints - impPoints).toDouble / (impPoints + funcPoints).toDouble
  else
    0
}