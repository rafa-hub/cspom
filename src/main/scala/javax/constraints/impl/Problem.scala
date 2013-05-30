package javax.constraints.impl

import cspom.CSPOM
import cspom.constraint.CSPOMConstraint
import cspom.variable.CSPOMVariable
import cspom.constraint.GeneralConstraint
import java.io.BufferedWriter
import java.io.OutputStreamWriter
import cspom.Loggable

class Problem(name: String) extends AbstractProblem(name) with Loggable {
  def this() = this("")

  val cspom = new CSPOM()

  // Members declared in javax.constraints.impl.AbstractProblem
  protected def createSolver(): javax.constraints.Solver = {
    Class.forName("javax.constraints.impl.search.Solver").getConstructor(classOf[Problem]).newInstance(this).asInstanceOf[javax.constraints.Solver]
  }

  def createVariable(name: String, min: Int, max: Int): javax.constraints.Var = {
    new Var(this, name, cspom.interVar(name, min, max))
  }
  def debug(l: String) { logger.fine(l) }
  def error(l: String) { logger.severe(l) }
  def getImplVersion(): String = "CSPOM JSR331 implementation"
  def log(l: String) { logger.info(l) }
  def post(constraint: javax.constraints.Constraint) {
    cspom.addConstraint(constraint.getImpl.asInstanceOf[CSPOMConstraint])
  }
  def variableBool(name: String): javax.constraints.VarBool = new VarBool(this, name)

  // Members declared in javax.constraints.Problem
  def allDiff(scope: Array[javax.constraints.Var]): javax.constraints.Constraint = {
    val constraint = new GeneralConstraint("alldifferent", scope.map(_.getImpl.asInstanceOf[CSPOMVariable]): _*)
    new Constraint(this, constraint)
  }
  def linear(v1: javax.constraints.Var, op: String, v2: javax.constraints.Var): javax.constraints.Constraint =
    post(v1, op, v2)

  def linear(v: javax.constraints.Var, op: String, c: Int): javax.constraints.Constraint =
    post(v, op, c)

  def loadFromXML(is: java.io.InputStream) {
    cspom.loadXML(is)
  }
  def storeToXML(os: java.io.OutputStream, comments: String) {
    val ow = new OutputStreamWriter(os)
    xml.XML.write(ow, cspom.toXCSP, xml.XML.encoding, false, null)
    ow.close()
  }
  def post(v1: javax.constraints.Var, op: String, v2: javax.constraints.Var): javax.constraints.Constraint = {
    val constraint = cspom.ctr(op, v1.getImpl.asInstanceOf[CSPOMVariable], v2.getImpl.asInstanceOf[CSPOMVariable])
    new Constraint(this, constraint)
  }
  def post(v: javax.constraints.Var, op: String, c: Int): javax.constraints.Constraint = {
    val constraint = cspom.ctr(op, v.getImpl.asInstanceOf[CSPOMVariable], cspom.varOf(c))
    new Constraint(this, constraint)
  }
  def post(sum: Array[javax.constraints.Var], op: String, v: javax.constraints.Var): javax.constraints.Constraint = {
    val lb = sum.map(_.getMin()).sum
    val ub = sum.map(_.getMax()).sum
    val r = cspom.interVar(lb, ub)
    val c = cspom.ctr("zerosum", -1 :: List.fill(sum.length)(1), r +: sum.map(_.getImpl.asInstanceOf[CSPOMVariable]): _*)
    val constraint = cspom.ctr(op, r, v.getImpl.asInstanceOf[CSPOMVariable])
    new Constraint(this, constraint)
  }
  def post(vs: Array[javax.constraints.Var], op: String, v: Int): javax.constraints.Constraint = {
    val constant = cspom.varOf(v)
    post(vs, op, new Var(this, constant.name, constant))
  }
  def post(x$1: Array[Int], x$2: Array[javax.constraints.Var], x$3: String, x$4: javax.constraints.Var): javax.constraints.Constraint = ???
  def post(x$1: Array[Int], x$2: Array[javax.constraints.Var], x$3: String, x$4: Int): javax.constraints.Constraint = ???
  def postCardinality(x$1: Array[javax.constraints.Var], x$2: Int, x$3: String, x$4: javax.constraints.Var): javax.constraints.Constraint = ???
  def postCardinality(x$1: Array[javax.constraints.Var], x$2: Int, x$3: String, x$4: Int): javax.constraints.Constraint = ???
  def postElement(x$1: Array[javax.constraints.Var], x$2: javax.constraints.Var, x$3: String, x$4: javax.constraints.Var): javax.constraints.Constraint = ???
  def postElement(x$1: Array[javax.constraints.Var], x$2: javax.constraints.Var, x$3: String, x$4: Int): javax.constraints.Constraint = ???
  def postElement(x$1: Array[Int], x$2: javax.constraints.Var, x$3: String, x$4: javax.constraints.Var): javax.constraints.Constraint = ???
  def postElement(x$1: Array[Int], x$2: javax.constraints.Var, x$3: String, x$4: Int): javax.constraints.Constraint = ???
  def scalProd(x$1: Array[Int], x$2: Array[javax.constraints.Var]): javax.constraints.Var = ???

}