package org.powbot.krulvis.vorkath.tree.branches

import org.powbot.api.rt4.Npc
import org.powbot.api.rt4.Npcs
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.extensions.Utils.sleep
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.krulvis.api.script.tree.branch.ShouldConsume
import org.powbot.krulvis.vorkath.Vorkath

class Fighting(script: Vorkath) : Branch<Vorkath>(script, "IsFighting") {
	override val failedComponent: TreeComponent<Vorkath>
		get() = TODO("Not yet implemented")
	override val successComponent: TreeComponent<Vorkath> = ShouldConsume(script, Attacking(script))

	override fun validate(): Boolean {
		script.vorkath = Npcs.stream().name("Vorkath").first()
		return script.vorkath != Npc.Nil
	}
}

class Attacking(script: Vorkath) : Branch<Vorkath>(script, "IsFighting") {
	override val failedComponent: TreeComponent<Vorkath> = SimpleLeaf(script, "Attack") {
		if (script.vorkath.interact("Attack")) {
			waitFor { validate() }
		}
	}
	override val successComponent: TreeComponent<Vorkath> = SimpleLeaf(script, "Chill") {
		sleep(50)
	}

	override fun validate(): Boolean {
		return me.interacting() == script.vorkath
	}
}