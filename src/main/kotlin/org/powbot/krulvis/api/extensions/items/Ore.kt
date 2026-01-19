package org.powbot.krulvis.api.extensions.items

import org.powbot.api.Tile
import org.powbot.api.rt4.GameObject
import org.powbot.api.rt4.Objects
import org.powbot.api.rt4.Varpbits
import org.powbot.krulvis.api.ATContext.uppercaseFirst
import java.util.*


enum class Ore(
	override val ids: IntArray,
	val miningLvl: Int,
	//settingId & shiftAmount are used in Blast furnace
	val settingId: Int,
	val shiftAmount: Int,
	vararg val colors: Int
) : Item {
	AMETHYST(intArrayOf(21347), 92, -1, -1, 6705),
	CALCIFIED(intArrayOf(29088), 41, -1, -1, 6705),
	PAY_DIRT(intArrayOf(12011), 45, -1, -1, 6705),
	VOLCANIC_ASH(intArrayOf(21622), 22, -1, -1, 6705),
	RUNITE(intArrayOf(451), 85, 548, 8, -31437),
	ADAMANTITE(intArrayOf(449), 70, 548, 0, 21662),
	GEM_ROCK(intArrayOf(1625, 1627, 1629, 1623, 1621, 1619, 1617), 65, -1, -1, -10335),
	MITHRIL(intArrayOf(447), 55, 547, 24, -22239),
	GOLD(intArrayOf(444), 40, 548, 16, 8885),
	SANDSTONE(intArrayOf(6971, 6973, 6975, 6977), 25, -1, 0, 6949),
	COAL(intArrayOf(453), 30, 547, 0, 10508),
	SILVER(intArrayOf(442), 20, -1, -1, 74),
	IRON(intArrayOf(440), 15, 547, 16, 2576),
	COPPER(intArrayOf(436), 1, -1, -1, 4510, 4645, 8889),
	TIN(intArrayOf(438), 1, -1, -1, 53),
	CLAY(intArrayOf(434), 1, -1, -1, 6705),
	TE_SALT(intArrayOf(22593), 72, -1, -1, 960),
	EFH_SALT(intArrayOf(22595), 72, -1, -1, -22328),
	URT_SALT(intArrayOf(22597), 72, -1, -1, 21704),
	BLURITE(intArrayOf(668), 1, -1, -1, 696969),
	SOFT_CLAY(intArrayOf(1761), 70, -1, -1, 7062);

	override val itemName: String = "${name.uppercaseFirst().replace("_", " ")} ore"
	private val mask = 255

	override val stackable: Boolean = false

	val blastFurnaceCount: Int
		get() = Varpbits.varpbit(settingId) shr shiftAmount and mask

	override fun hasWith(): Boolean {
		return getInventoryCount(false) > 0
	}

	override fun getCount(countNoted: Boolean): Int {
		return getInventoryCount(false)
	}

	fun getNearestRock(): Optional<GameObject> {
		return Objects.stream().filtered {
			it.hasOre(this)
		}.nearest().findFirst()
	}

	companion object {
		fun GameObject.hasOre(vararg ores: Ore = values()): Boolean {
			val name = name()
			val validNames = arrayOf("Ore vein", "Amethyst crystals", "Ash pile", "Calcified rocks")
			return name in validNames || ores.any { ore -> ore.colors.any { it.toShort() in modifiedColors() } }
		}

		/**
		 * @return true if there are no objects on the [GameObject]'s tile that have ore
		 */
		fun GameObject.mined(vararg ores: Ore = values()): Boolean {
			return Objects.stream().at(tile).none { it.hasOre(*ores) }
		}

		fun GameObject.getOre(): Ore? {
			val name = name()
			//Amethyst crystals can always be mined, ore veins are static but can only be mined if called
			// "Ore vein"
			if (name.contains("vein")) {
				return PAY_DIRT
			} else if (name == "Crystals") {
				return AMETHYST
			}
			return values().firstOrNull { ore ->
				hasOre(ore)
			}
		}

		fun Tile.getOre(): Ore? {
			val rock = Objects.stream().at(this).name("Rock").findFirst()
			return if (rock.isPresent) {
				values().firstOrNull { ore ->
					rock.get().hasOre(ore)
				}
			} else {
				null
			}
		}

		fun Int.getOre(): Ore? = values().firstOrNull { it.ids.contains(this) }

	}
}




