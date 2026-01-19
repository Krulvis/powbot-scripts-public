package org.powbot.krulvis.gwd.branches

import org.powbot.api.Point
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.script.tree.branch.ShouldConsume
import org.powbot.krulvis.gwd.GWD.GOD_WARS_AREA
import org.powbot.krulvis.gwd.GWDScript
import org.powbot.krulvis.gwd.leaf.EnterGeneralRoom
import org.powbot.krulvis.gwd.leaf.GetKC
import org.powbot.krulvis.gwd.leaf.ToGWD
import org.powbot.krulvis.gwd.leaf.WalkToKCLocation


class HasKC<S>(script: S) : Branch<S>(script, "HasKC?") where S : GWDScript<S> {
	override val failedComponent: TreeComponent<S> = ShouldEquipGear(script, script.equipmentKC, AtKCLocation(script))
	override val successComponent: TreeComponent<S> =
		ShouldConsume(script, ShouldEquipGear(script, script.equipmentGeneral, EnterGeneralRoom(script)))

	override fun validate(): Boolean = script.god.killCount()
}

class AtKCLocation<S>(script: S) : Branch<S>(script, "AtKCLocation?") where S : GWDScript<S> {
	override val failedComponent: TreeComponent<S> = AtGWD(script)
	override val successComponent: TreeComponent<S> = GetKC(script)

	override fun validate(): Boolean = script.god.kcLocation.distance() < 30
}

class AtGWD<S>(script: S) : Branch<S>(script, "AtGWD?") where S : GWDScript<S> {
	override val failedComponent: TreeComponent<S> = ToGWD(script)
	override val successComponent: TreeComponent<S> = WalkToKCLocation(script)

	override fun validate(): Boolean {
		val t = me.tile()
		return GOD_WARS_AREA.getPolygon().contains(Point(t.x, t.y))
	}
}