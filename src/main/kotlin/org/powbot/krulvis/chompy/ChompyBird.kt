package org.powbot.krulvis.chompy

import com.google.common.eventbus.Subscribe
import org.powbot.api.Tile
import org.powbot.api.event.NpcAnimationChangedEvent
import org.powbot.api.rt4.Npc
import org.powbot.api.rt4.Npcs
import org.powbot.api.script.ScriptCategory
import org.powbot.api.script.ScriptManifest
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.script.KrulScript
import org.powbot.krulvis.api.script.painter.ATPaint
import org.powbot.krulvis.chompy.tree.branch.HasEquipment

@ScriptManifest(name = "krul ChompyBird", description = "Kills chompy birds, doesn't Pluck", version = "1.0.0", scriptId = "b40b416a-e9b3-42b8-b724-91cdf684d2bd", category = ScriptCategory.Minigame)
class ChompyBird : KrulScript() {
	override fun createPainter(): ATPaint<*> = ChompyBirdPainter(this)

	var placingToads: Boolean = false
	var placementTile = Tile.Nil
	var currentTarget: Npc = Npc.Nil
	var kills = 0

	override val rootComponent: TreeComponent<*> = HasEquipment(this)

	fun getAttackableBird() = Npcs.stream().name("Chompy bird").filtered { it.isBirdValid() }.nearest().first()


	@Subscribe
	fun onNpcAnimation(nae: NpcAnimationChangedEvent) {
		val npc = nae.npc
		if (npc.name == "Chompy bird" && nae.animation == 6762
			&& npc.healthBarVisible() && npc.healthPercent() == 0) {
			logger.info("Death animation = ${nae.animation}")
			kills++
		}
	}
}


val eatingText = listOf("Sqwark!", "Gobble!")
fun Npc.isBirdEating() = overheadMessage() in eatingText

fun Npc.isBirdValid(): Boolean {
	return valid() && (!healthBarVisible() || healthPercent() > 0) && actions.contains("Attack") && !isBirdEating()
}

fun main() {
	ChompyBird().startScript()
}