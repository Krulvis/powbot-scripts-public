package org.powbot.krulvis.fighter.tree.branch

import org.powbot.api.Notifications
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.demonicgorilla.tree.branch.ShouldEquipAmmo
import org.powbot.krulvis.fighter.Fighter
import org.powbot.krulvis.fighter.slayer.Slayer
import org.powbot.mobile.script.ScriptManager

class ShouldStop(script: Fighter) : Branch<Fighter>(script, "Should stop?") {

	override val successComponent: TreeComponent<Fighter> = SimpleLeaf(script, "Stopping") {
		Notifications.showNotification("Stopped because task was finished!")
		ScriptManager.stop()
	}
	override val failedComponent: TreeComponent<Fighter> = ShouldEquipAmmo(script)

	override fun validate(): Boolean {
		return script.lastTask && Slayer.taskRemainder() <= 0
	}
}