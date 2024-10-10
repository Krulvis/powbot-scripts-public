package org.powbot.krulvis.cluesolver.clues

import org.powbot.api.Tile
import org.powbot.api.rt4.Objects
import org.powbot.krulvis.api.ATContext.walkAndInteract
import org.powbot.krulvis.api.extensions.Utils.waitForDistance

enum class InteractionClue(
	override val id: Int,
	override val solveSpot: Tile,
	val objectName: String,
) : Clue {

	;

	fun getObject() = Objects.stream(solveSpot).name(objectName).first()

	override fun solve(): Boolean {
		val obj = getObject()
		if (obj.actions().contains("Open") && !obj.actions().contains("Search")) {
			if (walkAndInteract(obj, "Open")) {
				waitForDistance(obj) { getObject().actions().contains("Search") }
			}
		} else if (walkAndInteract(obj, "Search")) {
			return waitForDistance(obj) { Clue.getClue() != this }
		}
		return false
	}
}