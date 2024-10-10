package org.powbot.krulvis.nex.tree.branch

import org.powbot.api.rt4.Npcs
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.ATContext.alive
import org.powbot.krulvis.nex.Nex

class InArena(script: Nex) : Branch<Nex>(script, "InArena") {
	override val failedComponent: TreeComponent<Nex>
		get() = TODO("Not yet implemented")
	override val successComponent: TreeComponent<Nex>
		get() = TODO("Not yet implemented")

	override fun validate(): Boolean {
		TODO("Not yet implemented")
	}
}

class NexAlive(script: Nex) : Branch<Nex>(script, "NexAlive") {
	override val failedComponent: TreeComponent<Nex>
		get() = TODO("Not yet implemented")
	override val successComponent: TreeComponent<Nex>
		get() = TODO("Not yet implemented")

	override fun validate(): Boolean {
		script.nex = Npcs.stream().name("Nex").first()
		return script.nex.alive()
	}
}