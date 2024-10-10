package org.powbot.krulvis.demonicgorilla.tree.branch

import org.powbot.api.Notifications
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.demonicgorilla.DemonicGorilla
import org.powbot.krulvis.fighter.slayer.Slayer
import org.powbot.mobile.script.ScriptManager

class ShouldStop(script: DemonicGorilla) : Branch<DemonicGorilla>(script, "Should stop?") {

	override val successComponent: TreeComponent<DemonicGorilla> = SimpleLeaf(script, "Stopping") {
		Notifications.showNotification("Stopped because task was finished!")
		if (script.bankTeleport.execute())
			ScriptManager.stop()
	}
	override val failedComponent: TreeComponent<DemonicGorilla> = ShouldEquipAmmo(script)

	override fun validate(): Boolean {
		return script.lastTask && Slayer.taskRemainder() <= 0
	}
}