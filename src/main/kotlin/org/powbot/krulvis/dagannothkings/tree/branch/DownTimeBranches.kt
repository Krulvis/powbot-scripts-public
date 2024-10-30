package org.powbot.krulvis.dagannothkings.tree.branch

import org.powbot.api.rt4.Movement
import org.powbot.api.rt4.Npcs
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.ATContext.missingHP
import org.powbot.krulvis.api.ATContext.walkAndInteract
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.krulvis.dagannothkings.DagannothKings
import org.powbot.krulvis.dagannothkings.Data


class EvadeRex(script: DagannothKings) : Branch<DagannothKings>(script, "EvadeRex?") {
	override val successComponent: TreeComponent<DagannothKings> = SimpleLeaf(script, "EvadeRex") {
		Movement.step(script.evadeRexTile)
	}
	override val failedComponent: TreeComponent<DagannothKings> = HealOnSpinnops(script)

	override fun validate(): Boolean {
		return !Data.King.Rex.kill && script.evadeRexTile.distance() > 3
	}
}

class HealOnSpinnops(script: DagannothKings) : Branch<DagannothKings>(script, "HealOnSpinolyp") {
	override val failedComponent: TreeComponent<DagannothKings> = SimpleLeaf(script, "Downtime") {
		if (Movement.distance(script.centerTileEvadeSupreme) > 0) {
			Movement.step(script.centerTileEvadeSupreme, 0)
			waitFor { Movement.destination() == script.centerTileEvadeSupreme }
		}
	}
	override val successComponent: TreeComponent<DagannothKings> = SimpleLeaf(script, "HealOnSpinolyp") {
		script.spinolypEquipment.forEach { it.item.equip() }
		val spinolyp = Npcs.stream().name("Spinolyp").nearest().first()
		if (me.interacting() != spinolyp && walkAndInteract(spinolyp, "Attack")) {
			waitFor { me.interacting() == spinolyp }
		}
	}

	override fun validate(): Boolean {
		return script.healOnSpinolyp && missingHP() > 0
	}
}
