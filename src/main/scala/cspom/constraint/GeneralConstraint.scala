package cspom.constraint

import javax.script.ScriptException
import cspom.variable.CSPOMVariable
import cspom.Evaluator

class GeneralConstraint(
  val predicate: Predicate,
  scope: Seq[CSPOMVariable])
  extends CSPOMConstraint(predicate.function, scope) {

  //  def this(description: String, parameters: String, scope: CSPOMVariable[_]*) =
  //    this(description = description, parameters = parameters,
  //      scope = scope.toList)

  def this(func: String, params: String, scope: CSPOMVariable*) =
    this(Predicate(func, Some(params)), scope)

  def this(func: String, scope: CSPOMVariable*) =
    this(Predicate(func, None), scope)

  override def toString = {
    val stb = new StringBuilder
    stb append description
    stb.append(predicate.optParameters)
    scope.addString(stb, "(", ", ", ")").toString

  }

  override def evaluate(tuple: Seq[_]): Boolean = {
    val stb = new StringBuilder();
    if (predicate.parameters.isDefined) {
      stb append "p_"
    }
    stb append description append '('

    tuple.addString(stb, ", ")

    if (predicate.parameters.isDefined) {
      stb append ", " append predicate.parameters.get
    }

    try {
      Evaluator.evaluate((stb append ')').toString);
    } catch {
      case e: ScriptException =>
        throw new IllegalStateException(e);
    }

  }

  override def replacedVar(which: CSPOMVariable, by: CSPOMVariable) = {
    new GeneralConstraint(predicate,
      scope map { v => if (v == which) by else v })
  }

}