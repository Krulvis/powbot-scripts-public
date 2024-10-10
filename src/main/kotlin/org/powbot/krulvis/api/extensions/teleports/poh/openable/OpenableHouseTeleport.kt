package org.powbot.krulvis.api.extensions.teleports.poh.openable

import org.powbot.api.Notifications
import org.powbot.api.rt4.Component
import org.powbot.api.rt4.GameObject
import org.powbot.api.rt4.Objects
import org.powbot.api.rt4.Widgets
import org.powbot.krulvis.api.ATContext.walkAndInteract
import org.powbot.krulvis.api.extensions.teleports.poh.HouseTeleport
import org.powbot.krulvis.api.extensions.Utils.waitForDistance
import org.powbot.mobile.script.ScriptManager

interface OpenableHouseTeleport : HouseTeleport {

	val WIDGET_ID: Int
	val COMP_ID: Int
	val OBJECT_NAMES: Array<String>

	fun getObject(): GameObject? = Objects.stream().name(*OBJECT_NAMES).firstOrNull()
	fun opened() = Widgets.component(WIDGET_ID, COMP_ID).visible()

	fun open(): Boolean {
		if (opened()) return true
		val mountedObj = getObject() ?: return false

		return walkAndInteract(mountedObj, "Teleport Menu")
			&& waitForDistance(mountedObj) { opened() }
	}

	fun getTeleportComponent(): Component = Widgets.component(WIDGET_ID, COMP_ID).filterNotNull()
		.firstOrNull { it.text().contains(action, true) } ?: Component.Nil

	fun scroll(): Boolean = true
	override fun insideHouseTeleport(): Boolean {
		val obj = getObject() ?: return false
		if (obj.actions().contains(action)) {
			return walkAndInteract(obj, action)
		} else if (open()) {
			val teleportToSelect = getTeleportComponent()
			if (teleportToSelect.text().contains("<str>")) {
				Notifications.showNotification("Cannot use ${this.javaClass.simpleName} to $action, it is striped through")
				ScriptManager.stop()
			}
			return teleportToSelect.valid() && scroll() && teleportToSelect.interact(
				teleportToSelect.actions().firstOrNull() ?: "Continue"
			)
		}
		return false
	}

	companion object {

		fun find(name: String): OpenableHouseTeleport? {
			return SpiritTreeTeleport.forName(name) ?: JewelleryBoxTeleport.forName(name) ?: MountedGloryTeleport.forName(name)
			?: NexusPortalTeleport.forName(name) ?: MountedDigsiteTeleport.forName(name)
			?: MountedXericsTeleport.forName(name)
		}
	}
}