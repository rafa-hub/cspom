package cspom
import cspom.variable.{CSPOMConstant, CSPOMExpression}

import scala.jdk.CollectionConverters._
import com.typesafe.scalalogging.LazyLogging

final case class CSPOMConstraint[+T](
    result: CSPOMExpression[T],
    function: String,
    arguments: Seq[CSPOMExpression[Any]],
    params: Map[String, Any] = Map()) extends Parameterized with LazyLogging {
  assert(arguments.nonEmpty)

  def withParam(addParams: (String, Any)*) = new CSPOMConstraint(result, function, arguments, params ++ addParams)
  def withParams(addParams: Map[String, Any]): CSPOMConstraint[T] = withParam(addParams.toSeq: _*)

  def nonReified: Boolean = result.isTrue

  def fullScope: Seq[CSPOMExpression[_]] = result +: arguments

  lazy val flattenedScope: Set[CSPOMExpression[_]] =
    flattenedScopeDuplicates.toSet

  def flattenedScopeDuplicates: Iterator[CSPOMExpression[_]] = fullScope.iterator.flatMap(_.flatten)

  val id: Int = CSPOMConstraint.id
  CSPOMConstraint.id += 1

  def getArgs: java.util.List[CSPOMExpression[_]] = arguments.asJava

  override def hashCode: Int = id
  override def equals(o: Any): Boolean = o match {
    case o: AnyRef => o eq this
    case _ => false
  }

  private def replaceVarShallow[R, S <: R](candidate: CSPOMExpression[R], which: CSPOMExpression[R], by: CSPOMExpression[S]): CSPOMExpression[R] = {
    if (candidate == which) {
      by
    } else {
      candidate
    }
  }

  def replacedVar[R >: T, S >: T <: R](which: CSPOMExpression[R], by: CSPOMExpression[S]): CSPOMConstraint[R] = {
    val newResult = replaceVarShallow(result, which, by)
    val newArgs = arguments.map(replaceVarShallow(_, which, by))

    new CSPOMConstraint(newResult,
      function,
      newArgs,
      params)
  }

  override def toString: String = {
    val args = arguments.map(_.toString())
    if (result.isTrue) {
      toString(None, args)
    } else {
      toString(Some(result.toString), args)
    }
  }

  private def toString(result: Option[String], arguments: Seq[String]): String = {
    val content = s"$id. $function(${arguments.mkString(", ")})$displayParams"
    result match {
      case None => s"constraint $content"
      case Some(r) => s"constraint $r == $content"
    }
  }

  def toString(vn: CSPOMExpression[_] => String): String = {
    val args = arguments.map(a => a.toString(vn))
    if (result.isTrue) {
      toString(None, args)
    } else {
      toString(Some(vn(result)), args)
    }

  }

}

object CSPOMConstraint {
  var id = 0

  def param(key: String, v: Any): ConstraintParameters = ConstraintParameters(Map(key -> v))

  def apply(function: String)(arguments: CSPOMExpression[_]*): CSPOMConstraint[Boolean] =
    apply(CSPOMConstant(true))(function)(arguments: _*)

  def apply[R](result: CSPOMExpression[R])(function: String)(arguments: CSPOMExpression[_]*): CSPOMConstraint[R] =
    new CSPOMConstraint(result, function, arguments.toSeq)

}

case class ConstraintParameters(m: Map[String, Any]) extends Map[String, Any] {
  def param(key: String, v: Any): ConstraintParameters = ConstraintParameters(m + (key -> v))
  def updated[B1 >: Any](key: String, value: B1): Map[String, B1] = ConstraintParameters(m.updated(key, value))
  def removed(key: String): scala.collection.immutable.Map[String, Any] = ConstraintParameters(m - key)
  def get(key: String): Option[Any] = m.get(key)
  def iterator: Iterator[(String, Any)] = m.iterator
}

