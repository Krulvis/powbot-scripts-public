package org.powbot.krulvis.araxxor

import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.powbot.api.*
import org.powbot.api.event.NpcAnimationChangedEvent
import org.powbot.api.event.ProjectileDestinationChangedEvent
import org.powbot.api.event.TickEvent
import org.powbot.api.rt4.*
import org.powbot.api.script.OptionType
import org.powbot.api.script.ScriptCategory
import org.powbot.api.script.ScriptConfiguration
import org.powbot.api.script.ScriptManifest
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.ATContext.dead
import org.powbot.krulvis.api.extensions.ResurrectSpell
import org.powbot.krulvis.api.extensions.Timer
import org.powbot.krulvis.api.extensions.items.Weapon
import org.powbot.krulvis.api.extensions.requirements.EquipmentRequirement
import org.powbot.krulvis.api.extensions.requirements.InventoryRequirement
import org.powbot.krulvis.api.extensions.teleports.DARKMEYER_DRAKAN
import org.powbot.krulvis.api.extensions.teleports.FEROX_ENCLAVE_ROD
import org.powbot.krulvis.api.extensions.teleports.Teleport
import org.powbot.krulvis.api.extensions.teleports.TeleportMethod
import org.powbot.krulvis.api.extensions.teleports.poh.openable.CASTLE_WARS_JEWELLERY_BOX
import org.powbot.krulvis.api.script.KrulScript
import org.powbot.krulvis.api.script.painter.KrulPaint
import org.powbot.krulvis.araxxor.Data.ARAXXOR
import org.powbot.krulvis.araxxor.Data.ENRAGED_ATTACK_ANIMATION
import org.powbot.krulvis.araxxor.Data.EXPLODING
import org.powbot.krulvis.araxxor.Data.MIRROR
import org.powbot.krulvis.araxxor.Data.SIZE
import org.powbot.krulvis.araxxor.Data.TOXIC_ATTACK_ANIM
import org.powbot.krulvis.araxxor.tree.branch.IsKilling
import org.powbot.krulvis.fighter.*

private const val WHITE_SPIDER_EQUIPMENT = "WhiteSpiderEquip"

@ScriptManifest(
	"krul Araxxor",
	"Kills Araxxor",
	"Krulvis",
	"1.0.0",
	scriptId = "74c79aec-ba18-47d3-b15c-76a7b4470210",
	priv = true,
	category = ScriptCategory.Combat
)
@ScriptConfiguration.List(
	[
		ScriptConfiguration(EQUIPMENT_OPTION, "What to wear?", OptionType.EQUIPMENT),
		ScriptConfiguration(SPECIAL_EQUIPMENT_OPTION, "What to wear for special?", OptionType.EQUIPMENT),
		ScriptConfiguration(WHITE_SPIDER_EQUIPMENT, "What to wear for white spider killing?", OptionType.EQUIPMENT),
		ScriptConfiguration(INVENTORY_OPTION, "What to bring in inv?", OptionType.INVENTORY),
		ScriptConfiguration(
			BANK_TELEPORT_OPTION, "Bank Teleport", OptionType.STRING, defaultValue = CASTLE_WARS_JEWELLERY_BOX,
			allowedValues = [CASTLE_WARS_JEWELLERY_BOX, FEROX_ENCLAVE_ROD]
		),
		ScriptConfiguration(
			MONSTER_TELEPORT_OPTION, "Bank Teleport", OptionType.STRING, defaultValue = DARKMEYER_DRAKAN,
			allowedValues = [DARKMEYER_DRAKAN]
		),
	]
)
class Araxxor : KrulScript() {
	val equipment by lazy { EquipmentRequirement.forOption(getOption(EQUIPMENT_OPTION)) }
	val specEquipment by lazy { EquipmentRequirement.forOption(getOption(SPECIAL_EQUIPMENT_OPTION)) }
	val mirrorGear by lazy { EquipmentRequirement.forOption(getOption(WHITE_SPIDER_EQUIPMENT)) }
	val allEquipment by lazy { (equipment + specEquipment + mirrorGear).distinct() }
	val inventory by lazy {
		InventoryRequirement.forOption(getOption(INVENTORY_OPTION))
			.filterNot { inv -> allEquipment.any { inv.item.id in it.item.ids } }
	}
	val specWeapon by lazy {
		Weapon.forId(
			specEquipment.firstOrNull { it.slot == Equipment.Slot.MAIN_HAND }?.item?.id ?: -1
		)
	}
	val resurrectSpell = ResurrectSpell.GREATER_GHOST
	val bankTeleport by lazy { TeleportMethod(Teleport.forName(getOption(BANK_TELEPORT_OPTION))) }
	val araxTeleport by lazy { TeleportMethod(Teleport.forName(getOption(MONSTER_TELEPORT_OPTION))) }

	val offensive = Prayer.Effect.PIETY
	var defensive = Prayer.Effect.PROTECT_FROM_MELEE

	var banking = false
	var inside = false
	var npcs = listOf(Npc.Nil)
	var araxxor = Npc.Nil
	var mirror = Npc.Nil
	var exploding = Npc.Nil
	var target = Npc.Nil
	var araxxorRect = Rectangle.Nil
	var araxxorCenter = Tile.Nil

	//Acid pools
	var unsafeTiles = emptyList<Tile>()
	val puke = mutableListOf<Projectile>()
	var pools = emptyList<GameObject>()

	//Enrage
	var enrage = false
	var enrageTiles = emptyArray<Tile>()
	var nextEnrageTile = Tile.Nil
	var exploDestination = Tile.Nil

	//Eggs
	var eggs = emptyList<Npc>()
	var centerTile = Tile.Nil

	fun nextEgg(): String {
		return eggs.first().name
	}

	private val spiders = arrayOf(ARAXXOR, "Acidic Araxyte", MIRROR, EXPLODING, "Egg")

	override fun onStart() {
		super.onStart()

		explodingTimer.stop()
		drippingTimer.stop()
		PowDispatchers.Script.launch {
			EventFlows.ticks().collect { onTick(it) }
		}
		PowDispatchers.Script.launch {
			EventFlows.npcAnimationChanges().collectLatest { onNpcAnimation(it) }
		}
		PowDispatchers.Script.launch {
			EventFlows.projectileDestinationChanges().collectLatest { onProjectile(it) }
		}
		//Dud to make sure we have some npc's on first run
		onTick(TickEvent())
	}

	private fun onProjectile(e: ProjectileDestinationChangedEvent) {
		if (e.target() == Actor.Nil) {
			logger.info("Found projectile destination change")
			puke.add(e.projectile)
		}
	}

	var nextAttackTick = -1
	var lastAraxAnimation = -1
	var attacks = 0
	val explodingTimer = Timer(2400)
	val drippingTimer = Timer(2400)
	private fun onNpcAnimation(e: NpcAnimationChangedEvent) {
		if (e.animation == ENRAGED_ATTACK_ANIMATION) {
			nextAttackTick = ticks + 4
			logger.info("Found enrage attack. Can attack")
		}
		if (e.animation == TOXIC_ATTACK_ANIM) {
			drippingTimer.reset()
			logger.info("Dripping for ${drippingTimer.getRemainderString()}...")
		}
		if (e.npc.name in spiders) {
			logger.info("Found animation for ${e.npc.name} animation=${e.animation} on tick=${ticks}")
			if (e.npc.name == ARAXXOR) {
				lastAraxAnimation = e.animation
				if (e.animation != -1) {
					attacks++
				}
			}
		}
	}

	private fun onTick(e: TickEvent) {
		val cycle = Game.cycle()
		puke.removeIf {
			cycle > it.cycleEnd
		}
		val npcs = Npcs.stream().name(*spiders).toList()
		araxxor = npcs.firstOrNull { it.name == ARAXXOR } ?: Npc.Nil
		if (araxxor.valid()) {
			val a = araxxor.trueTile()
			araxxorRect = Rectangle(a.x, a.y, SIZE, SIZE)
			araxxorCenter = araxxorRect.center().tile()
			if (araxxor.healthPercent() <= 25) {
				enrage = true
			}
		} else {
			enrage = false
		}
		val newEggs = npcs.filter { it.name == "Egg" && it.actions.contains("Attack") }
		if (newEggs.size == 6) {
			centerTile = Tile(newEggs.sumOf { it.tile().x } / 6, newEggs.sumOf { it.tile().y } / 6, 0)
		}
		val first3 = newEggs.filter { it.y() <= centerTile.y }
		val second3 = newEggs.filter { it.y() > centerTile.y }
		eggs = first3.sortedBy { it.x() } + second3.sortedBy { it.x() }

		mirror = npcs.firstOrNull { it.name == MIRROR } ?: Npc.Nil
		target = if (!mirror.dead()) mirror else araxxor
		exploding = npcs.firstOrNull { it.name == EXPLODING } ?: Npc.Nil
		pools = Objects.stream(15, GameObject.Type.INTERACTIVE).name("Acid pool").toList()
		unsafeTiles = pools.map { it.tile } + puke.map { it.destination() }
		enrageTiles = araxxorRect.getOuterLayers()
		nextEnrageTile = enrageTiles.nearestInner()
	}

	fun Rectangle.getNearestOutsideTile(tile: Tile): Tile {
		val rectMin = Tile(x, y)
		val rectMax = Tile(x + width, y + height)
		// Calculate distances from the tile to each border
		val distanceLeft = tile.x - rectMin.x
		val distanceRight = rectMax.x - tile.x
		val distanceTop = tile.y - rectMin.y
		val distanceBottom = rectMax.y - tile.y

		// Find the direction of the nearest border
		return when (minOf(distanceLeft, distanceRight, distanceTop, distanceBottom)) {
			distanceLeft -> Tile(rectMin.x - 1, tile.y)       // Just outside the left border
			distanceRight -> Tile(rectMax.x + 1, tile.y)      // Just outside the right border
			distanceTop -> Tile(tile.x, rectMin.y - 1)        // Just outside the top border
			distanceBottom -> Tile(tile.x, rectMax.y + 1)     // Just outside the bottom border
			else -> throw IllegalStateException("Unexpected case") // This shouldn't occur
		}
	}

	private fun Rectangle.getOuterLayers(): Array<Tile> {
		val outerTiles = mutableListOf<Tile>()
		for (xi in x until x + width) {
			outerTiles.add(Tile(xi, y + 1))
			outerTiles.add(Tile(xi, y))

			outerTiles.add(Tile(xi, y + 1 + height - 1))
			outerTiles.add(Tile(xi, y + height))
		}

		for (yi in y until y + height) {
			outerTiles.add(Tile(x + 1, yi))
			outerTiles.add(Tile(x, yi))

			outerTiles.add(Tile(x + width - 1, yi))
			outerTiles.add(Tile(x + width, yi))
		}
		return outerTiles.toTypedArray()
	}


	private fun Array<Tile>.nearestInner(): Tile {
		if (isEmpty()) return Tile.Nil
		val inner = filterIndexed { index, _ -> index % 2 == 0 }.minByOrNull { it.distance() }!!
		val index = indexOf(inner)
		val rebuild = sliceArray(0 until index) + sliceArray(index until size)
		for (i in indices) {
			val tile = rebuild[i]
			val nearestOutside = araxxorRect.getNearestOutsideTile(tile)
			if (tile !in unsafeTiles && nearestOutside !in unsafeTiles) {
				return tile
			}
		}
		return Tile.Nil
	}

	private fun Point.tile() = Tile(x, y, 0)

	override fun createPainter(): KrulPaint<*> = AraxxorPainter(this)


	override val rootComponent: TreeComponent<*> = IsKilling(this)
}

fun main() {
	Araxxor().startScript("localhost", "GIM", false)
}
