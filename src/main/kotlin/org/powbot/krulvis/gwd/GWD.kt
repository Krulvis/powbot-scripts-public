package org.powbot.krulvis.gwd

import org.powbot.api.Area
import org.powbot.api.Tile
import org.powbot.api.rt4.GameObject
import org.powbot.api.rt4.Npcs
import org.powbot.api.rt4.Objects
import org.powbot.api.rt4.Widgets
import org.powbot.krulvis.api.ATContext.me
import org.slf4j.LoggerFactory

object GWD {

	val logger = LoggerFactory.getLogger(javaClass.simpleName)

	val SARADOMIN_ENCAMPMENT = Area(Tile(2882, 5280), Tile(2931, 5254))
	val ANCIENT_PRISON = Area(Tile(2847, 5232), Tile(2944, 5186))
	val GOD_WARS_AREA = Area(Tile(2818, 5381), Tile(2944, 5252))
	val GOD_WARS_OUTSIDE_AREA = Area(Tile(2892, 3715, 0), Tile(2936, 3766, 0))
	val CRACK_TILE = Tile(2900, 3713, 0)
	val HOLE_TILE = Tile(2919, 3747, 0)
	val PATH_TO_ENTRANCE = listOf(
		Tile(2904, 3720, 0),
		Tile(2903, 3726, 0),
		Tile(2905, 3731, 0),
		Tile(2909, 3734, 0),
		Tile(2909, 3740, 0),
		Tile(2912, 3744, 0),
		Tile(2916, 3749, 0)
	)
	const val EQUIPMENT_BODYGUARDS_OPTION = "Equipment Bodyguards"
	const val EQUIPMENT_KILLCOUNT_OPTION = "Equipment Killcount"
	const val EQUIPMENT_GENERAL_OPTION = "Equipment General"
	const val STACK_BODYGUARDS_OPTION = "Stack Bodyguards"

	const val KC_ROOT = 406

	enum class God(
		val deltaCoords: IntArray,
		val doorTile: Tile,
		val kcLocation: Tile,
		val generalName: String,
		val mageName: String,
		val rangeName: String,
		val meleeName: String,
		vararg val kcNames: String
	) {
		Saradomin(
			intArrayOf(0, -8, 1, 7),
			Tile(2908, 5265, 0),
			Tile(2910, 5262, 0),
			"Commander Zilyana",
			"Growler",
			"Bree",
			"Starlight",
			"Spiritual warrior", "Spiritual mage", "Knight of Saradomin", "Saradomin priest", "Spiritual ranger"
		),
		Zamorak(
			intArrayOf(0, -8, 0, 8),
			Tile(0, 0, 0),
			Tile(0, 0, 0),
			"K'ril Tsutsaroth",
			"Balfrug Kreeyath",
			"Zakl'n Gritch",
			"Tstanon Karlak",
			"Imp"
		),
		Bandos(
			intArrayOf(0, -8, 0, 8),
			Tile(0, 0, 0),
			Tile(0, 0, 0),
			"General Graardor",
			"Sergeant Steelwill",
			"Sergeant Grimspike",
			"Sergeant Strongstack",
			"Goblin"
		),
		Armadyl(
			intArrayOf(0, -8, 0, 8),
			Tile(0, 0, 0), Tile(0, 0, 0),
			"Kree'arra",
			"Wingman Skree",
			"Flockleader Geerin",
			"Flight Kilisa"
		),
		;

		val altarName = "$name altar"
		fun getGeneral() = Npcs.stream().name(generalName).first()
		fun getMage() = Npcs.stream().name(mageName).first()
		fun getRange() = Npcs.stream().name(rangeName).first()
		fun getMelee() = Npcs.stream().name(meleeName).first()

		var generalArea = Area(Tile.Nil, Tile.Nil)
		fun generalAreaSet() = generalArea != Area(Tile.Nil, Tile.Nil)
		fun resetGeneralArea() {
			generalArea = Area(Tile.Nil, Tile.Nil)
		}

		fun inside(): Boolean {
			val area = if (generalAreaSet()) generalArea else calculateGeneralArea()
			logger.info("Area is nil = ${area == Area(Tile.Nil, Tile.Nil)}")
			if (!area.contains(me.tile())) {
				resetGeneralArea()
				return false
			}
			return true
		}

		fun calculateGeneralArea(): Area {
			val objects = Objects.stream(25, GameObject.Type.INTERACTIVE).name("Big door", altarName).toList()
			if (objects.size != 2) return Area(Tile.Nil, Tile.Nil)
			val door = objects.first { it.name == "Big door" }.tile
			val altar = objects.first { it.name == altarName }.tile
			generalArea = Area(
				Tile(door.x + deltaCoords[0], door.y + deltaCoords[1], door.floor),
				Tile(altar.x + deltaCoords[2], altar.y + deltaCoords[3], door.floor)
			)
			return generalArea
		}

		fun killCount(): Boolean {
			val widget = Widgets.widget(KC_ROOT)
			if (!widget.valid()) return false
			val comps = widget.components()
			val index = comps.firstOrNull { it.text() == name }?.index() ?: -1
			return !comps[index + 5].text().contains("<col=ff4747>")
		}
	}

	val lootNames = arrayOf(
		"saradomin sword",
		"saradomin's light",
		"armadyl crossbow",
		"saradomin hilt",
		"godsword shard",
		"adamant platebody",
		"rune kiteshield",
		"rune plateskirt",
		"prayer potion",
		"super restore",
		"saradomin brew",
		"coins",
		"ranarr seed",
		"grimy ranarr weed",
		"magic seed",
		"law rune",
		"dragon med helm",
		"dragon spear",
		"clue scroll",
		"rune dart"
	)
}