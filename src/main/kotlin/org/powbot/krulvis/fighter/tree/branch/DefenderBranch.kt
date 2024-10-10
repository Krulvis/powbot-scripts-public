package org.powbot.krulvis.fighter.tree.branch

import org.powbot.api.Tile
import org.powbot.api.rt4.*
import org.powbot.api.rt4.Equipment.Slot.OFF_HAND
import org.powbot.api.rt4.walking.local.Utils
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.ATContext.getCount
import org.powbot.krulvis.api.extensions.Utils.long
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.krulvis.api.extensions.items.EquipmentItem
import org.powbot.krulvis.fighter.Defender
import org.powbot.krulvis.fighter.Fighter
import org.powbot.mobile.script.ScriptManager

class GettingDefenders(script: Fighter) : Branch<Fighter>(script, "Getting defenders") {
	override val failedComponent: TreeComponent<Fighter> = ShouldDodgeProjectile(script)
	override val successComponent: TreeComponent<Fighter> = ShouldExitRoom(script)

	override fun validate(): Boolean {
		return script.warriorGuild
	}
}

class ShouldExitRoom(script: Fighter) : Branch<Fighter>(script, "Should Exit Room?") {
	val doorTile = Tile(2847, 3541, 2)
	val doorTileBasement = Tile(2911, 9968, 2)

	override val successComponent: TreeComponent<Fighter> = SimpleLeaf(script, "Exit room") {
		val door = Objects.stream().at(doorTile).name("Door").firstOrNull() ?: Objects.stream().at(doorTileBasement)
			.name("Door").firstOrNull()
		if (door != null && Utils.walkAndInteract(door, "Open")) {
			waitFor(long()) { Players.local().tile() == doorTile }
			if (Inventory.getCount(script.warriorTokens) < 10) {
				script.logger.info("Stopping script, out of token")
				ScriptManager.stop()
			}
		}
	}

	override val failedComponent: TreeComponent<Fighter> = ShouldShowRuneDefender(script)

	override fun validate(): Boolean {
		val target = script.target()
		val currDefenderIndex = Defender.currentDefenderIndex()
		if (!target.valid() || !target.reachable()) {
			Defender.lastDefenderIndex = currDefenderIndex
		}
		return (Defender.lastDefenderIndex < currDefenderIndex && currDefenderIndex < Defender.defenders.size - 1)
			|| Inventory.getCount(script.warriorTokens) < 10
	}
}


class ShouldShowRuneDefender(script: Fighter) : Branch<Fighter>(script, "Should show rune defender?") {

	override val successComponent: TreeComponent<Fighter> = SimpleLeaf(script, "Show rune defender") {
		val runeDefender = EquipmentItem(Defender.defenders[6], OFF_HAND)
		val tile = Tile(2907, 9968, 0)
		if (runeDefender.inEquipment()) {
			runeDefender.dequip()
		} else if (Chat.canContinue()) {
			if (Chat.clickContinue()) {
				waitFor { !validate() }
			}
		} else {
			val lorelai = Npcs.stream().name("Lorelai").firstOrNull()
			if (lorelai == null || (!lorelai.reachable() && tile.distance() > 5)) {
				Movement.walkTo(tile)
			} else if (Utils.walkAndInteract(lorelai, "Use", selectItem = Defender.defenders[6])) {
				waitFor { Chat.canContinue() }
			}
		}
	}

	override val failedComponent: TreeComponent<Fighter> = ShouldDodgeProjectile(script)

	override fun validate(): Boolean {
		return Defender.lastDefenderIndex == 6 && Varpbits.varpbit(788) and 4096 != 4096
	}
}