package codeAnalysis.analyser.util

import codeAnalysis.analyser.AST._

import scala.reflect.internal.util.RangePosition
import scala.reflect.runtime.universe._

object ASTPrinter {
  def ast2String(ast: Any, indent: Int = 0): String = {
    val astClass = ast.getClass
    val astType = getAstType(ast)
    val builder = new StringBuilder
    builder ++= " " * indent
    builder ++= astClass.getSimpleName
    builder ++= " {"
    builder ++= "\n"
    val members = astType.members.collect {
      case m: MethodSymbol if m.isAccessor => m
    }.toList
    for (member <- members) {
      val memberName = member.name.toString
      val method = astClass.getMethod(memberName)
      try {
        val memberValue = method.invoke(ast)
        builder ++= any2String(memberName, memberValue, indent + 2)
        builder ++= "\n"
      } catch {
        case e: Exception => e.printStackTrace()
      }
    }
    builder ++= " " * indent
    builder ++= "}\n"
    builder.toString
  }

  def astList2String(list: List[_], indent: Int = 0): String = {
    val builder = new StringBuilder
    if (list.nonEmpty) {
      builder ++= "[\n"
      for (ast <- list) {
        builder ++= ast2String(ast, indent + 2)
      }
      builder ++= " " * indent
      builder ++= "]"
    } else {
      builder ++= "[]"
    }
    builder.toString
  }

  def any2String(name: String, any: Any, indent: Int = 0): String = {
    " " * indent + name + ": " + (
      any match {
        case list: List[_] => astList2String(list, indent)
        case ast@(_: AST | _: Parent | _: Param) => ast2String(ast, indent)
        case range: RangePosition=> s"${range.start}, ${range.point}, ${range.end}"
        case "" => "\"\""
        case null => "null"
        case _ => any.toString
      })
  }

  def getAstType(ast: Any): Type = ast match {
    case _: PackageDefinition => typeOf[PackageDefinition]
    case _: TraitDefinition => typeOf[TraitDefinition]
    case _: ClassDefinition => typeOf[ClassDefinition]
    case _: ObjectDefinition => typeOf[ObjectDefinition]
    case _: FunctionDef => typeOf[FunctionDef]
    case _: ValAssignment => typeOf[ValAssignment]
    case _: VarAssignment => typeOf[VarAssignment]
    case _: ValDefinition => typeOf[ValDefinition]
    case _: VarDefinition => typeOf[VarDefinition]
    case _: Var => typeOf[Var]
    case _: Val => typeOf[Val]
    case _: NewClass => typeOf[NewClass]
    case _: For => typeOf[For]
    case _: While => typeOf[While]
    case _: DoWhile => typeOf[DoWhile]
    case _: MatchCase => typeOf[MatchCase]
    case _: Case => typeOf[Case]
    case _: CaseAlternative => typeOf[CaseAlternative]
    case _: IfStatement => typeOf[IfStatement]
    case _: FunctionCall => typeOf[FunctionCall]
    case _: AST => typeOf[AST]
    case _: ClassParent => typeOf[ClassParent]
    case _: TraitParent => typeOf[TraitParent]
    case _: Parent => typeOf[Parent]
    case _: Param => typeOf[Param]
  }
}
