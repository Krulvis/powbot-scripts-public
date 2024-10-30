package org.powbot.krulvis.fighter.tree.branch

import org.powbot.api.rt4.*
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.extensions.Prayer.closeQuickPray
import org.powbot.krulvis.api.extensions.Prayer.setQuickPrayers
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.krulvis.api.extensions.items.Item.Companion.JUG
import org.powbot.krulvis.api.extensions.items.Item.Companion.PIE_DISH
import org.powbot.krulvis.api.extensions.items.Item.Companion.VIAL
import org.powbot.krulvis.api.extensions.items.RunePouch
import org.powbot.krulvis.fighter.Fighter
import org.powbot.krulvis.fighter.tree.leaf.PrayAtAltar


class ShouldDropTrash(script: Fighter) : Branch<Fighter>(script, "Should Drop Trash?") {

	val TRASH = intArrayOf(VIAL, PIE_DISH, JUG)
	override val successComponent: TreeComponent<Fighter> = SimpleLeaf(script, "Dropping vial") {
		if (Inventory.stream().id(*TRASH).firstOrNull()?.interact("Drop") == true)
			waitFor { Inventory.stream().id(*TRASH).firstOrNull() == null }
	}
	override val failedComponent: TreeComponent<Fighter> = ShouldCombineKeys(script)

	override fun validate(): Boolean {
		return Inventory.stream().id(*TRASH).firstOrNull() != null
	}
}

class ShouldCombineKeys(script: Fighter) : Branch<Fighter>(script, "ShouldCombineKeys?") {

	val TOOTH_HALF = 985
	val LOOP_HALF = 987
	override val successComponent: TreeComponent<Fighter> = SimpleLeaf(script, "CombiningKeys") {
		val tooth = Inventory.stream().id(TOOTH_HALF).first()
		if (tooth.useOn(Inventory.stream().id(LOOP_HALF).first())) {
			waitFor { !validate() }
		}
	}
	override val failedComponent: TreeComponent<Fighter> = ShouldInsertRunes(script)

	override fun validate(): Boolean {
		val inv = Inventory.get()
		return inv.any { it.id == LOOP_HALF } && inv.any { it.id == TOOTH_HALF }
	}
}

class ShouldInsertRunes(script: Fighter) : Branch<Fighter>(script, "Should Insert Runes?") {

	var inventoryRune: Item? = null

	override val successComponent: TreeComponent<Fighter> = SimpleLeaf(script, "Inserting runes") {
		if (Inventory.selectedItem().id != inventoryRune?.id) {
			inventoryRune?.interact("Use")
		} else if (Inventory.stream().id(*RunePouch.ids).firstOrNull()
				?.interact("Use") == true
		) {
			waitFor { getInsertableRune() == null }
		}
	}
	override val failedComponent: TreeComponent<Fighter> = ShouldBuryBones(script)

	private fun getInsertableRune(): Item? {
		val runes = RunePouch.runes()
		return Inventory.stream()
			.firstOrNull { invItem -> runes.any { invItem.id == it.first.id && it.second < 16000 - invItem.stack } }
	}

	override fun validate(): Boolean {
		inventoryRune = getInsertableRune()
		return RunePouch.inInventory() && inventoryRune != null
	}
}


///BANKING SITS IN BETWEEN HERE


class ShouldPrayAtAltar(script: Fighter) : Branch<Fighter>(script, "ShouldPrayAtAltar?") {
	override val successComponent: TreeComponent<Fighter> = PrayAtAltar(script)
	override val failedComponent: TreeComponent<Fighter> = ShouldSetQuickPray(script)


	override fun validate(): Boolean {
		if (!script.prayAtNearbyAltar || Prayer.prayerPoints() >= script.nextAltarPrayRestore) return false
		return Objects.stream().type(GameObject.Type.INTERACTIVE).action("Pray-at").nearest().first().valid()
	}

}

class ShouldSetQuickPray(script: Fighter) : Branch<Fighter>(script, "ShouldSetQuickPray?") {
	override val successComponent: TreeComponent<Fighter> = SimpleLeaf(script, "SetQuickPray") {
		Prayer.setQuickPrayers(*script.quickPrayers)
	}
	override val failedComponent: TreeComponent<Fighter> = ShouldCloseQuickPray(script)


	override fun validate(): Boolean {
		return script.usingPrayer && !Prayer.quickPrayers().contentEquals(script.quickPrayers)
	}

}

class ShouldCloseQuickPray(script: Fighter) : Branch<Fighter>(script, "ShouldCloseQuickPray?") {
	override val successComponent: TreeComponent<Fighter> = SimpleLeaf(script, "CloseQuickPray") {
		Prayer.closeQuickPray()
	}
	override val failedComponent: TreeComponent<Fighter> = UsingCannon(script)


	override fun validate(): Boolean {
		return !Prayer.closeQuickPray()
	}

}
