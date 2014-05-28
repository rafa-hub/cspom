package cspom.util

import java.math.RoundingMode
import java.math.RoundingMode
import com.google.common.math.IntMath
import com.google.common.collect.DiscreteDomain
import com.google.common.collect.Cut
import RangeSet._

object IntervalsArithmetic {

  def apply[A <% Ordered[A]](
    f: (GuavaRange[A], GuavaRange[A]) => GuavaRange[A],
    ii: RangeSet[A], jj: RangeSet[A]): RangeSet[A] = {
    var result = RangeSet[A]()
    for (i <- ii.ranges; j <- jj.ranges) {
      result ++= f(i, j)
    }
    result
  }

  def apply[A <% Ordered[A]](f: GuavaRange[A] => GuavaRange[A], ii: RangeSet[A]): RangeSet[A] = {
    ii.ranges.foldLeft(RangeSet[A]())(_ ++ f(_))
  }

  private def asInfinities(r: GuavaRange[Int]): (Infinitable, Infinitable) = {
    val l = if (r.hasLowerBound) {
      Finite(r.lowerEndpoint)
    } else {
      MinInf
    }

    val u = if (r.hasUpperBound) {
      Finite(r.upperEndpoint)
    } else {
      PlusInf
    }

    (l, u)
  }

  private def asRange(l: Infinitable, lbt: BoundType, u: Infinitable, ubt: BoundType) = {
    (l, u) match {
      case (Finite(l), Finite(u)) => {
        if (l > u) { GuavaRange.closedOpen(l, l) }
        else { GuavaRange(l, lbt, u, ubt) }
      }
      case (MinInf, Finite(u)) => GuavaRange.upTo(u, ubt)
      case (Finite(l), PlusInf) => GuavaRange.downTo(l, lbt)
      case (MinInf, PlusInf) => GuavaRange.all[Int]
      case _ => throw new IllegalStateException
    }
  }

  private def minBound[A](b: (A, BoundType)*)(implicit ord: Ordering[A]): (A, BoundType) = {
    b.min(Ordering.Tuple2(ord, BoundType.closedIsLess))
  }

  private def maxBound[A](b: (A, BoundType)*)(implicit ord: Ordering[A]): (A, BoundType) = {
    b.max(Ordering.Tuple2(ord, BoundType.closedIsMore))
  }

  implicit class RangeArithmetics(r: RangeSet[Int]) {
    def +(i: RangeSet[Int]): RangeSet[Int] = IntervalsArithmetic(_ + _, r, i)
    def unary_-(): RangeSet[Int] = IntervalsArithmetic(-_, r)
    def -(i: RangeSet[Int]): RangeSet[Int] = IntervalsArithmetic(_ - _, r, i)
    def *(i: RangeSet[Int]): RangeSet[Int] = IntervalsArithmetic(_ * _, r, i)
    def /(i: RangeSet[Int]): RangeSet[Int] = IntervalsArithmetic(_ / _, r, i)
    def abs(): RangeSet[Int] = IntervalsArithmetic(_.abs, r)
  }

  implicit class Arithmetics(r: GuavaRange[Int]) {
    /**
     * [a, b] + [c, d] = [a + c, b + d]
     * [a, b] − [c, d] = [a − d, b − c]
     * [a, b] × [c, d] = [min (a × c, a × d, b × c, b × d), max (a × c, a × d, b × c, b × d)]
     * [a, b] ÷ [c, d] = [min (a ÷ c, a ÷ d, b ÷ c, b ÷ d), max (a ÷ c, a ÷ d, b ÷ c, b ÷ d)] when 0 is not in [c, d].
     */

    def +(i: GuavaRange[Int]): GuavaRange[Int] = {
      val (a, b) = asInfinities(r)
      val (c, d) = asInfinities(i)

      asRange(a + c, r.lowerBoundType & i.lowerBoundType, b + d, r.upperBoundType & i.upperBoundType)
    }

    def unary_-(): GuavaRange[Int] = {
      GuavaRange(-r.upperEndpoint, r.upperBoundType, -r.lowerEndpoint, r.lowerBoundType)
    }

    def -(i: GuavaRange[Int]) = this + -i

    def -(v: Int) = this + -v

    def *(i: GuavaRange[Int]): GuavaRange[Int] = {

      val (a, bc) = asInfinities(r.canonical(IntDiscreteDomain))
      val (c, dc) = asInfinities(i.canonical(IntDiscreteDomain))

      val d = dc - Finite(1)
      val b = bc - Finite(1)

      val l = List(a * c, a * d, b * c, b * d).min

      val u = List(a * c, a * d, b * c, b * d).max

      asRange(l, Closed, u, Closed)

    }

    def /(i: GuavaRange[Int]) = {
      if (i.contains(0)) {
        GuavaRange.all[Int]
      } else {
        val (a, bc) = asInfinities(r.canonical(IntDiscreteDomain))
        val (c, dc) = asInfinities(i.canonical(IntDiscreteDomain))

        val d = dc - Finite(1)
        val b = bc - Finite(1)

        val l = List(
          a.div(c, RoundingMode.CEILING),
          a.div(d, RoundingMode.CEILING),
          b.div(c, RoundingMode.CEILING),
          b.div(d, RoundingMode.CEILING)).min

        val u = List(
          a.div(c, RoundingMode.FLOOR),
          a.div(d, RoundingMode.FLOOR),
          b.div(c, RoundingMode.FLOOR),
          b.div(d, RoundingMode.FLOOR)).max

        asRange(l, Closed, u, Closed)
      }
    }

    def /(v: Int) = {
      val (lb, ub) = asInfinities(r)
      val d = Finite(v)
      if (v >= 0) {
        asRange(
          lb.div(d, RoundingMode.CEILING), if (lb.divisible(d)) r.lowerBoundType else Closed,
          ub.div(d, RoundingMode.FLOOR), if (ub.divisible(d)) r.upperBoundType else Closed)
      } else {
        asRange(
          ub.div(d, RoundingMode.CEILING), if (ub.divisible(d)) r.upperBoundType else Closed,
          lb.div(d, RoundingMode.FLOOR), if (lb.divisible(d)) r.lowerBoundType else Closed)
      }
    }

    def abs: GuavaRange[Int] = {
      val (l, u) = asInfinities(r)
      if (u <= Finite(0)) { -r }
      else if (l >= Finite(0)) { r }
      else {
        val (b, bt) = maxBound[Infinitable]((-l, r.lowerBoundType), (u, r.upperBoundType))

        asRange(Finite(0), Closed, b, bt)
      }
    }

  }
}
