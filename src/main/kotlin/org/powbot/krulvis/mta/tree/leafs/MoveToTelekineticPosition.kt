package org.powbot.krulvis.mta.tree.leafs

import org.powbot.api.rt4.Movement
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.mta.MTA
import org.powbot.krulvis.mta.rooms.TelekineticRoom
import org.powbot.krulvis.mta.rooms.TelekineticRoom.walk

class MoveToTelekineticPosition(script: MTA) : Leaf<MTA>(script, "Go to casting position") {

	override fun execute() {
		val optimal = TelekineticRoom.optimal()
		if (optimal.distanceTo(Movement.destination()) > 0) {
			optimal.walk()
		}
	}


}