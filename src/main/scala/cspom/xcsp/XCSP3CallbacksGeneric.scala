package cspom.xcsp

import com.typesafe.scalalogging.LazyLogging
import cspom.extension.MDDRelation
import cspom.variable.{CSPOMConstant, CSPOMExpression, SimpleExpression}
import cspom.{CSPOM, CSPOMConstraint}
import mdd.{IdMap, Star, Starrable, ValueStar}
import org.xcsp.common.Constants
import org.xcsp.common.Types._
import org.xcsp.common.predicates.{XNode, XNodeLeaf, XNodeParent}
import org.xcsp.parser.entries.XDomains.XDomBasic
import org.xcsp.parser.entries.XValues.IntegerEntity
import org.xcsp.parser.entries.XVariables.XVarInteger

/**
  * Created by vion on 30/05/17.
  */
trait XCSP3CallbacksGeneric extends XCSP3CallbacksVars with LazyLogging {

  val cache = new IdMap[Array[Array[Int]], MDDRelation]()
  val extCache = new IdMap[Array[Array[Int]], MDDRelation]()

  override def buildCtrPrimitive(id: String, x: XVarInteger, op: TypeConditionOperatorRel, k: Int): Unit = {
    buildCtrPrimitiveCSPOM(toCspom(x), op, CSPOMConstant(k))
  }

  override def buildCtrPrimitive(id: String, x: XVarInteger, aop: TypeArithmeticOperator,
                                 p: Int, op: TypeConditionOperatorRel, y: XVarInteger): Unit = {
    buildCtrPrimitiveCSPOM(toCspom(x), aop, CSPOMConstant(p), op, toCspom(y))
  }

  override def buildCtrPrimitive(id: String, x: XVarInteger, opa: TypeArithmeticOperator,
                                 y: XVarInteger, op: TypeConditionOperatorRel, k: Int): Unit = {
    if (opa == TypeArithmeticOperator.SUB && k == 0) {
      // Optimizes the optimizer…
      buildCtrPrimitiveCSPOM(toCspom(x), op, toCspom(y))
    } else {
      buildCtrPrimitiveCSPOM(toCspom(x), opa, toCspom(y), op, CSPOMConstant(k))
    }
  }

  private def buildCtrPrimitiveCSPOM(x: SimpleExpression[Int], opa: TypeArithmeticOperator,
                                     y: SimpleExpression[Int], op: TypeConditionOperatorRel, k: SimpleExpression[Int]): Unit = {
    val aux = cspom.defineInt { r =>
      CSPOMConstraint(r)(opa.toString.toLowerCase)(x, y)
    }

    buildCtrPrimitiveCSPOM(aux, op, k)
  }

  private def buildCtrPrimitiveCSPOM(x: SimpleExpression[Int], op: TypeConditionOperatorRel, k: SimpleExpression[Int]): Unit = {
    import TypeConditionOperatorRel._
    op match {
      case LT => cspom.ctr("lt")(x, k)
      case LE => cspom.ctr("le")(x, k)
      case EQ => cspom.ctr("eq")(x, k)
      case NE => cspom.ctr("ne")(x, k)
      case GT => cspom.ctr("lt")(k, x)
      case GE => cspom.ctr("le")(k, x)
    }
  }

  override def buildCtrPrimitive(id: String, x: XVarInteger, op: TypeConditionOperatorSet, min: Int, max: Int): Unit = {
    buildCtrPrimitive(id, x, op, Array.range(min, max + 1))
  }

  override def buildCtrPrimitive(id: String, x: XVarInteger, op: TypeConditionOperatorSet, array: Array[Int]): Unit = {
    import TypeConditionOperatorSet._
    op match {
      case IN =>
        cspom.ctr("in")(toCspom(x), CSPOM.constantSeq(array))

      case NOTIN =>
        cspom.ctr(CSPOMConstraint(CSPOMConstant(false))("in")(toCspom(x), CSPOM.constantSeq(array)))
    }
  }

  /* Build constraints: intension */

  override def buildCtrPrimitive(id: String, x: XVarInteger, opa: TypeArithmeticOperator,
                                 y: XVarInteger, op: TypeConditionOperatorRel, k: XVarInteger): Unit = {
    buildCtrPrimitiveCSPOM(toCspom(x), opa, toCspom(y), op, toCspom(k))
  }

  override def buildCtrPrimitive(id: String, x: XVarInteger, aop: TypeArithmeticOperator, p: Int, op: TypeConditionOperatorRel, k: Int): Unit = {
    buildCtrPrimitiveCSPOM(toCspom(x), aop, CSPOMConstant(p), op, CSPOMConstant(k))
  }

  def extract(node: XNode[XVarInteger]): SimpleExpression[_] = {
    node match {
      case l: XNodeLeaf[XVarInteger] =>
        l.value match {
          case l: java.lang.Long if l.toLong.isValidInt => CSPOMConstant(l.toInt)
          case v: XVarInteger => toCspom(v)
        }
      case p: XNodeParent[XVarInteger] =>
        cspom.defineFree { x => intensionConstraint(x, p) }
    }
  }

  def typeString(t: TypeExpr): String = {
    t.toString.toLowerCase
  }

  def intensionConstraint(result: CSPOMExpression[_], p: XNodeParent[XVarInteger]): CSPOMConstraint[_] = {
    CSPOMConstraint(result)(typeString(p.getType))(p.sons.toSeq.map(extract): _*)
  }

  override def buildCtrIntension(id: String, scope: Array[XVarInteger], syntaxTreeRoot: XNodeParent[XVarInteger]): Unit = {
    cspom.ctr(intensionConstraint(CSPOMConstant(true), syntaxTreeRoot))
  }

  override def buildCtrExtension(id: String, list: Array[XVarInteger], tuples: Array[Array[Int]], positive: Boolean,
                                 flags: java.util.Set[TypeFlag]): Unit = {

    val relation = cache.getOrElseUpdate(tuples, {

      if (flags.contains(TypeFlag.STARRED_TUPLES)) {

        val doms = list.map { l =>
          val dom = l.dom.asInstanceOf[XDomBasic].values.asInstanceOf[Array[IntegerEntity]]
          IntegerEntity.toIntArray(dom, Int.MaxValue).toSeq
        }

        val starredTuples = tuples.map { t =>
          t.map {
            case Constants.STAR_INT => Star: Starrable
            case v => ValueStar(v)
          }
        }

        MDDRelation.fromStarred(starredTuples, doms).reduce()

      } else {
        extCache.getOrElseUpdate(tuples, MDDRelation(tuples).reduce())
      }
    })

    val scope = CSPOM.IntSeqOperations(list.toSeq.map(toCspom))

    logger.info(s"Parsed ${if (positive) "positive" else "negative"} extension constraint on $list with $relation")

    cspom.ctr {
      if (positive) {
        scope in relation
      } else {
        scope notIn relation
      }
    }

  }

  override def buildCtrExtension(id: String, x: XVarInteger, values: Array[Int], positive: Boolean,
                                 flags: java.util.Set[TypeFlag]): Unit = {
    val relation = MDDRelation(values.map(Array(_)))
    val scope = CSPOM.IntSeqOperations(Seq(toCspom(x)))

    logger.info(s"Parsed ${if (positive) "positive" else "negative"} extension constraint on $x with $relation")

    cspom.ctr {
      if (positive) {
        scope in relation
      } else {
        scope notIn relation
      }
    }
  }

}
