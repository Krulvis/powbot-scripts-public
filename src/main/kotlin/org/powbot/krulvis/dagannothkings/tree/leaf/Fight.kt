package org.powbot.krulvis.dagannothkings.tree.leaf

import org.powbot.api.rt4.Prayer
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.dagannothkings.DagannothKings
import org.powbot.krulvis.dagannothkings.Data.King.Companion.king

class Fight(script: DagannothKings) : Leaf<DagannothKings>(script, "Fighting") {
	override fun execute() {
		val king = script.target.king() ?: return
		val protectionPrayer = king.protectionPrayer
		val offensivePrayer = king.offensivePrayer
		if (offensivePrayer != null && !Prayer.prayerActive(offensivePrayer)) {
			Prayer.prayer(offensivePrayer, true)
		}
	}
}