package org.powbot.krulvis.api.script

import com.google.common.eventbus.Subscribe
import org.powbot.api.event.InventoryChangeEvent
import org.powbot.api.rt4.Bank
import org.powbot.api.rt4.DepositBox
import org.powbot.api.script.ScriptState
import org.powbot.krulvis.api.extensions.items.Item.Companion.VIAL
import org.powbot.krulvis.api.extensions.items.TeleportEquipment
import org.powbot.krulvis.api.script.painter.ATPaint
import org.powbot.mobile.script.ScriptManager

interface UniqueLootTracker {

	val requiredIds: IntArray
	val painter: ATPaint<*>

	@Subscribe
	fun onInventoryChange(evt: InventoryChangeEvent) {
		if (ScriptManager.state() != ScriptState.Running) return
		val id = evt.itemId
		val isTeleport = TeleportEquipment.isTeleportEquipment(id)
		if (evt.quantityChange > 0 && id != VIAL
			&& !requiredIds.contains(id)
			&& !isTeleport
		) {
			if (Bank.opened() || DepositBox.opened()) return
			painter.trackItem(id, evt.quantityChange)
		}
	}
}