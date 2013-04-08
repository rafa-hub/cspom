package cspom.extension
import cspom.constraint.{ CSPOMConstraint }
import cspom.variable.CSPOMVariable
import cspom.xcsp.Extension

/**
 * A class that implements extension constraints, that it constraints defined by
 * a set of allowed or forbidden tuples.
 *
 * @author vion
 *
 * @param <T>
 */
final class ExtensionConstraint(
  private var _relation: Relation,
  val init: Boolean,
  scope: Seq[CSPOMVariable])
  extends CSPOMConstraint("ext", scope) {

  def this(r: Relation, i: Boolean, s: Array[CSPOMVariable]) = this(r, i, s.toSeq)

  def relation = _relation
  
  override def toString = "%s: %s (%s)".format(super.toString, relation, if (init) "forbidden" else "allowed");

  override def evaluate(tuple: Seq[_]) = init ^ relation.contains(tuple.map(_.asInstanceOf[Int]))

  //  override lazy val hashCode = 31 * super.hashCode + relation.hashCode
  //
  //  override def equals(obj: Any) = obj match {
  //    case ec: ExtensionConstraint => super.equals(ec) && ec.relation == this.relation
  //    case _ => false
  //  }

  //  def standardize(newScope: Seq[CSPOMVariable]) = {
  //    assert(newScope.size == arity)
  //    new ExtensionConstraint(relation.asInstanceOf[LazyRelation].permute(scope.map(v => newScope.indexOf(v))), init, newScope.toList)
  //  }

  override def replacedVar(which: CSPOMVariable, by: CSPOMVariable) = {
    new ExtensionConstraint(relation, init, scope map { v => if (v == which) by else v })
  }
  
  def closeRelation(){
    _relation = null
  }

}