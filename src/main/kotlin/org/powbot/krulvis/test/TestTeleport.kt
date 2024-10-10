package org.powbot.krulvis.test

import org.powbot.api.event.GameActionEvent
import org.powbot.api.event.MessageEvent
import org.powbot.api.script.AbstractScript
import org.powbot.api.script.ScriptManifest
import org.powbot.krulvis.api.extensions.teleports.poh.openable.NexusPortalTeleport

@ScriptManifest(name = "Krul test Teleport", version = "1.0.1", description = "", priv = true)
class TeleportScript : AbstractScript() {

	var teleport = NexusPortalTeleport.Falador

	@com.google.common.eventbus.Subscribe
	fun onGameActionEvent(e: GameActionEvent) {
		logger.info("$e")
	}

	@com.google.common.eventbus.Subscribe
	fun onMsg(e: MessageEvent) {
		logger.info("MSG: \n Type=${e.type}, msg=${e.message}")
	}

	override fun poll() {
		teleport.execute()
	}

}

fun main() {
	TeleportScript().startScript("127.0.0.1", "GIM", true)
}
