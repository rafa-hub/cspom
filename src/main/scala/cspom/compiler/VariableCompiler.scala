package cspom.compiler

import cspom.CSPOM
import cspom.CSPOMConstraint
import cspom.variable.CSPOMExpression
import cspom.UNSATException
import cspom.variable.CSPOMConstant

abstract class VariableCompiler(
  val function: Symbol) extends ConstraintCompiler {

  def compiler(c: CSPOMConstraint[_]): Map[CSPOMExpression[_], CSPOMExpression[_]]

  type A = Map[CSPOMExpression[_], CSPOMExpression[_]]

  override def mtch(c: CSPOMConstraint[_], problem: CSPOM) = {
    if (c.function == function) {
      val m = try {
        compiler(c).filter { case (k, v) => k != v }
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
      require(m.forall(e => c.flattenedScope.contains(e._1)), s"$c must involve all $m")

      if (m.nonEmpty) {
        logger.info(s"$c: $m")
        Some(m)

      } else {
        None
      }
    } else {
      None
    }
  }

  def compile(c: CSPOMConstraint[_], problem: CSPOM, data: A) =
    data.map { case (k, v) => replace(k, v, problem) }.reduce(_ ++ _)
  //}
  //    var d = Delta()
  //    for ((k, v) <- data) {
  //      d ++= replace(k, v, problem)
  //    }
  //    d
  //  }

  def selfPropagation = true

}
