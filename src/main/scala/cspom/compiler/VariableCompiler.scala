package cspom.compiler

import cspom.CSPOM
import cspom.CSPOMConstraint
import cspom.variable.CSPOMExpression
import cspom.UNSATException
import cspom.variable.CSPOMConstant

abstract class VariableCompiler(
    val function: Symbol) extends ConstraintCompiler {

  def compiler(c: CSPOMConstraint[_]): Map[CSPOMExpression[_], CSPOMExpression[_]]

  def compilerWEntail(c: CSPOMConstraint[_]): (Map[CSPOMExpression[_], CSPOMExpression[_]], Boolean) = {
    (compiler(c), false)
  }

  type A = (Map[CSPOMExpression[_], CSPOMExpression[_]], Boolean)

  override def mtch(c: CSPOMConstraint[_], problem: CSPOM) = {
    if (c.function == function) {
      val (reductions, entail) = try {
        compilerWEntail(c)

      } catch {
        case e: UNSATException =>
          for (
            v <- c.flattenedScope;
            if (!v.isInstanceOf[CSPOMConstant[_]]);
            n <- problem.deepConstraints(v)
          ) {
            logger.debug(n.toString)
          }

          throw new UNSATException(s"$c is inconsistent", e)
      }

      val m = reductions.filter { case (k, v) => k != v }

      require(m.forall(e => c.flattenedScope.contains(e._1)), s"$c must involve all $m")

      if (m.nonEmpty || entail) {
        logger.info(s"$c: $m")
        Some((m, entail))

      } else {
        None
      }
    } else {
      None
    }
  }

  def compile(c: CSPOMConstraint[_], problem: CSPOM, data: A) = {
    val (reductions, entail) = data
    (if (entail) {
      removeCtr(c, problem)
    } else {
      Delta.empty
    }) ++
      reductions.map { case (k, v) => replace(k, v, problem) }.reduce(_ ++ _)

  }

  //}
  //    var d = Delta()
  //    for ((k, v) <- data) {
  //      d ++= replace(k, v, problem)
  //    }
  //    d
  //  }

  def selfPropagation = true

}
