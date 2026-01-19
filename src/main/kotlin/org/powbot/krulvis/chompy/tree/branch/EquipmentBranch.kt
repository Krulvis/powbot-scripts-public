package org.powbot.krulvis.chompy.tree.branch

import org.powbot.api.Notifications
import org.powbot.api.rt4.Equipment
import org.powbot.api.rt4.Inventory
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.script.tree.branch.ShouldSipPotion
import org.powbot.krulvis.chompy.ChompyBird
import org.powbot.mobile.script.ScriptManager

class HasEquipment(script: ChompyBird) : Branch<ChompyBird>(script, "HasRequirements") {
	override val failedComponent: TreeComponent<ChompyBird> = SimpleLeaf(script, "Stopping") {
		Notifications.showNotification("Does not have ogre bow, (ogre/brutal)arrows or bellows")
		ScriptManager.stop()
	}
	override val successComponent: TreeComponent<ChompyBird> = ShouldSipPotion(script, BirdSpawned(script))

	private val arrowRegex = Regex("""(\w\sbrutal|ogre\sarrow)""")
	override fun validate(): Boolean {
		val equipment = Equipment.get().map { it.name().lowercase() }
		return equipment.any { it.contains(arrowRegex) }
			&& equipment.any { it.contains("ogre bow") }
			&& Inventory.stream().nameContains("bellow").isNotEmpty()

	}
}