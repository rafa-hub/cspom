package cspom.compiler;

import scala.collection.mutable.LinkedHashMap
import cspom.CSPOM
import cspom.CSPOMConstraint
import cspom.Statistic
import cspom.variable.CSPOMExpression
import scala.collection.mutable.HashMap
import cspom.StatisticsManager
import cspom.TimedException
import cspom.VariableNames
import com.typesafe.scalalogging.LazyLogging
import scala.collection.mutable.HashSet
import scala.collection.mutable.LinkedHashSet
import scala.util.Try

/**
 * This class implements some known useful reformulation rules.
 *
 * @author vion
 *
 */
final class CSPOMCompiler(
    private val problem: CSPOM,
    private val constraintCompilers: IndexedSeq[ConstraintCompiler]) extends LazyLogging {

  val vn = new VariableNames(problem)

  private def compile(): CSPOM = {
    val toCompile = Array.ofDim[QueueSet](
      constraintCompilers.size)

    val constraints = new HashMap[Int, CSPOMConstraint[_]]

    for (c <- problem.constraints) {
      constraints.put(c.id, c)
    }

    var changed = true
    var first = true

    while (changed) {
      changed = false
      for (i <- toCompile.indices) {

        val compiler = constraintCompilers(i)
        logger.info(compiler.toString)
        // println(compiler)
        if (first) {
          toCompile(i) = new QueueSet(constraints.keys)
        }

        while (toCompile(i).nonEmpty) {

          for (constraint <- constraints.get(toCompile(i).dequeue())) {
            //println(s"Compiling ${constraint.toString(vn)}")
            //lazy val string = constraint.toString(vn)
            val delta = compile(compiler, constraint)
            //if (delta.nonEmpty && compiler == MergeEq) println(s"$string: $delta")
            changed |= delta.nonEmpty

            if (delta.nonEmpty)
              logger.info(delta.toString)

            for (rc <- delta.removed) {
              assert(!problem.constraintSet(rc), s"$compiler: $rc is still present")
              constraints.remove(rc.id)
            }

            for (c <- delta.added) {
              assert(problem.constraintSet(c), s"$compiler: $c is not present")
              constraints.put(c.id, c)
            }

            val enqueueVar = new LinkedHashSet[CSPOMExpression[_]]

            for (c <- delta.added) enqueueVar ++= c.fullScope
            //
            //            val enqueue = new HashSet[CSPOMConstraint[_]]
            //
            //            for (v <- enqueueVar) enqueue ++= problem.constraints(v)

            //            val enqueue = delta.added.flatMap(
            //              _.fullScope).distinct.flatMap(
            //                problem.constraints(_)).distinct

            for (j <- if (first) { 0 to i } else { toCompile.indices }) {
              //              if (j != i || compiler.selfPropagation) {
              for (v <- enqueueVar; ac <- problem.constraints(v)) {
                toCompile(j).enqueue(ac.id)
              }

              //              }
            }

          }

          //toCompile(i).remove(constraint.id)
          //println

        }

      }

      first = false
    }
    problem
  }

  def compile(compiler: ConstraintCompiler, constraint: CSPOMConstraint[_]): Delta = {
    require(problem.constraintSet(constraint), {
      val vn = new VariableNames(problem)
      s"${constraint.toString(vn)} not in $problem"
    })
    CSPOMCompiler.matches += 1

    compiler.mtch(constraint, problem) match {
      case Some(data) =>
        CSPOMCompiler.compiles += 1
        logger.debug(s"$compiler : ${constraint.toString(vn)}")
        compiler.compile(constraint, problem, data)
      case None => Delta()
    }

  }

}

object CSPOMCompiler {
  def compile(problem: CSPOM, compilers: Seq[ConstraintCompiler]): Try[CSPOM] = {
    val pbc = new CSPOMCompiler(problem, compilers.toIndexedSeq)

    val (r, t) = StatisticsManager.time(pbc.compile())

    compileTime += t

    r
  }

  @Statistic
  var matches = 0

  @Statistic
  var compiles = 0

  @Statistic
  var compileTime = 0.0

}
