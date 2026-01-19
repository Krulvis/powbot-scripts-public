package org.powbot.krulvis.api.extensions.teleports.poh.openable

import org.powbot.api.Tile
import org.powbot.api.requirement.Requirement
import org.powbot.api.rt4.Component
import org.powbot.api.rt4.ScrollHelper
import org.powbot.api.rt4.Widgets
import org.powbot.krulvis.api.ATContext.walkAndInteract
import org.powbot.krulvis.api.extensions.Utils.waitForDistance
import org.slf4j.Logger
import org.slf4j.LoggerFactory

const val FARMING_GUILD_SPIRIT_TREE_POH = "Farming Guild spirit tree (POH)"
const val POISON_WASTE_SPIRIT_TREE_POH = "Poison Waste spirit tree (POH)"

enum class SpiritTreeTeleport(override val action: String, override val destination: Tile) : OpenableHouseTeleport {
	FarmingGuild("Farming guild", Tile(1251, 3750, 0)),
	PoisonWaste("Poison Waste", Tile(2339, 3109, 0)),
	;

	override val logger: Logger = LoggerFactory.getLogger(javaClass.simpleName)
	override val requirements: List<Requirement> = emptyList()
	override val OBJECT_NAMES = arrayOf("Spiritual Fairy Tree", "Spirit Tree")
	override val WIDGET_ID: Int = 187
	override val COMP_ID: Int = 3

	override fun open(): Boolean {
		if (opened()) return true
		val mountedObj = getObject() ?: return false

		return walkAndInteract(mountedObj, "Tree")
			&& waitForDistance(mountedObj) { opened() }
	}

	private fun scrollComponent(): Component = Widgets.component(WIDGET_ID, 2)
	private fun paneComponent(): Component = Widgets.component(WIDGET_ID, 3)

	override fun scroll(): Boolean {
		val teleportComp = getTeleportComponent()
		val paneComp = paneComponent()
		val scrollComp = scrollComponent()
		return ScrollHelper.scrollTo({ teleportComp }, { paneComp }, { scrollComp })
	}

	override fun toString(): String {
		return "SpiritTreeTeleport($name)"
	}

	companion object {
		fun forName(name: String): SpiritTreeTeleport? {
			return if (!name.contains("spirit tree")) null
			else values().firstOrNull { name.replace(" ", "").contains(it.name, true) }
		}
	}
}