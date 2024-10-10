package org.powbot.krulvis.test


fun main() {
	val listi = listOf(0, 1, 2, 3, 4, 5, 6)

	listi.forEachIndexed { i, it ->
		println("$i, / 3 = ${(i -  1 )/ 2}")
	}

}
