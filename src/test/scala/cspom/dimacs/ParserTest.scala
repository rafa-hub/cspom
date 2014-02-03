package cspom.dimacs;

import cspom.CSPOM
import org.junit.Assert._
import org.junit.Test

final class ParserTest {

	val FILENAME = "flat30-1.cnf";

	@Test
	def test()  {
		val cspom = CNFParser.parse(classOf[ParserTest].getResourceAsStream(FILENAME))._1
		assertEquals(90, cspom.namedExpressions.size)
		assertEquals(300, cspom.constraints.size)
		//println(cspom)
	}

}
