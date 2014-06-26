package cspom.util

import org.scalatest.FlatSpec
import org.scalatest.Matchers
import org.scalacheck.Arbitrary
import org.scalacheck.Gen

class IntervalTest extends FlatSpec with Matchers {

  it should "compare correctly" in {
    IntInterval(0, 5) isBefore IntInterval(7, 10) shouldBe true
    IntInterval(0, 5) isBefore IntInterval(6, 10) shouldBe true
    IntInterval(7, 10) isAfter IntInterval(0, 5) shouldBe true
    IntInterval(6, 10) isAfter IntInterval(0, 5) shouldBe true
    an[IllegalArgumentException] should be thrownBy IntInterval(0, 5) & IntInterval(6, 10)
    IntInterval(0, 5) & IntInterval(5, 10) should not be empty
  }

  it should "detect mergeability" in {
    IntInterval(0, 5).canonical isConnected IntInterval(6, 10) shouldBe true
    IntInterval(0, 5) isConnected IntInterval(-6, -1).canonical shouldBe true
    IntInterval(0, 5) isConnected IntInterval(-6, 2) shouldBe true
    IntInterval(0, 5) isConnected IntInterval(-6, 20) shouldBe true
    IntInterval(0, 5) isConnected IntInterval(10, 20) shouldBe false
    IntInterval(0, 5) isConnected IntInterval(-20, -10) shouldBe false
  }

  it should "detect equality" in {
    IntInterval.all should not be IntInterval.greaterThan(0)
  }

}

object Intervals {
  def validInterval(i: Int, j: Int) = i <= j

  def validIntervals =
    for (
      i <- Arbitrary.arbitrary[Int];
      j: Int <- Gen.choose(i, Int.MaxValue)
    ) yield IntInterval(i, j)

  def smallIntervals =
    for (
      i <- Arbitrary.arbitrary[Int];
      j: Int <- Gen.choose(i, math.min(Int.MaxValue, i.toLong + 1000).toInt)
    ) yield IntInterval(i, j)
}
