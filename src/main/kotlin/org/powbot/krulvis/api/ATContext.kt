package org.powbot.krulvis.api

import org.powbot.api.*
import org.powbot.api.rt4.*
import org.powbot.api.rt4.magic.Rune
import org.powbot.api.rt4.magic.RunePouch
import org.powbot.api.rt4.magic.RunePower
import org.powbot.api.rt4.walking.local.Flag
import org.powbot.api.rt4.walking.local.LocalPathFinder
import org.powbot.api.rt4.walking.model.Skill
import org.powbot.krulvis.api.antiban.DelayHandler
import org.powbot.krulvis.api.antiban.OddsModifier
import org.powbot.krulvis.api.extensions.Utils.mid
import org.powbot.krulvis.api.extensions.Utils.short
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.krulvis.api.extensions.Utils.waitForWhile
import org.powbot.mobile.rscache.loader.ItemLoader
import org.slf4j.LoggerFactory
import kotlin.math.abs


object ATContext {
	val logger = LoggerFactory.getLogger(ATContext.javaClass.simpleName)

	val me: Player get() = Players.local()

	var nextRun = Random.nextInt(2, 5)

	val walkDelay = DelayHandler(2500, OddsModifier(), "Walking delay")

	var debugComponents: Boolean = true

	fun debug(msg: String) {
		if (debugComponents) {
			logger.info(msg)
		}
	}

	fun fullPrayer() = Skills.realLevel(Skill.Prayer) == Skills.level(Skill.Prayer)


	fun Actor<*>.gargoyle() = name().contains("Gargoyle", true)

	val GARGOYLE_DESTROY_ANIM = 1520
	fun Actor<*>.dead() =
		!valid() || (healthBarVisible() && if (gargoyle()) animation() == GARGOYLE_DESTROY_ANIM else healthPercent() == 0)

	fun Actor<*>.alive() = valid() && (!healthBarVisible() || healthPercent() > 0)


	fun turnRunOn(): Boolean {
		if (Movement.running()) {
			return true
		}
		if (Movement.energyLevel() >= Random.nextInt(1, 5)) {
			debug("Turning run on")
			return Widgets.widget(Constants.MOVEMENT_MAP).component(Constants.MOVEMENT_RUN_ENERGY - 1).click()
		}
		return false
	}

	fun List<Tile>.atLastTile(distance: Int = 2) = last().distanceTo(Movement.destination()) <= distance

	fun List<Tile>.traverse(offset: Int = 2, distanceToLastTile: Int = 2, whileWaiting: () -> Any = {}): Boolean {
		debug("List<Tile>.traverse(offset=$offset, distanceToLastTile=$distanceToLastTile, whileWaiting=$whileWaiting)")
		if (atLastTile(offset) && Components.stream(219).id(1).viewable().isEmpty()) {
			debug("Already at last tile...")
			return true
		}
		val walkableTile = lastOrNull { it.onMap() }
		if (walkableTile == null) {
			debug("Can't find walkableTile? none are on minimap")
			return false
		}
		val tileToClick = walkableTile.derive(
			kotlin.random.Random.nextInt(-offset, offset),
			kotlin.random.Random.nextInt(-offset, offset)
		)
		if (Movement.step(tileToClick, minDistance = 0)) {
			debug("Successfully stepped to tileToClick=${tileToClick}")
			waitForWhile(mid(), { lastOrNull { it.onMap() } != walkableTile }) { whileWaiting() }
		} else {
			debug("Failed Movement.step(${tileToClick}, minDistance=0)")
		}
//        val atLastTile = atLastTile(distanceToLastTile)
		val destination = Movement.destination()
		val last = last()
		val distanceToLast = last.distanceTo(destination)
		debug("Standing on ${destination}, lastTile=${last}, distance=${distanceToLast}, closeEnough=${distanceToLast <= distanceToLastTile}")
		return distanceToLast <= distanceToLastTile
	}

	fun Movement.moving(): Boolean = destination() != Tile.Nil

	fun Movement.stepNoConfirm(tile: Tile): Boolean {
		val matrix = tile.matrix()
		if (tile.distance() < 10 && matrix.inViewport()) {
			return matrix.click("Walk here")
		}
		val pos = Game.tileToMap(tile)
		return Input.tap(pos)
	}

	fun walk(position: Tile?, enableRun: Boolean = true, forceMinimap: Boolean = false): Boolean {
		if (position == null || position == Tile.Nil) {
			return true
		}
		val flags = Movement.collisionMap(position.floor()).flags()
		val position = if (!position.loaded() || !position.blocked(flags)) position else position.getWalkableNeighbor()
			?: return false
		if (Players.local().tile() == position) {
			debug("Already on tile: $position")
			return true
		}
		if (enableRun && !Movement.running() && Movement.energyLevel() > nextRun) {
			Movement.running(true)
			nextRun = Random.nextInt(1, 5)
		}
		if (!Movement.moving() || walkDelay.isFinished()) {
			if (forceMinimap && position.onMap()
				&& LocalPathFinder.findWalkablePath(Players.local().tile(), position).isNotEmpty()
			) {
				Movement.step(position)
			} else {
				Movement.walkTo(position)
			}
			walkDelay.resetTimer()
		}
		return false
	}

	fun GenericItem.getPrice(): Int {
		val id = id()
		if (id == 995) return 1
		return GrandExchange.getItemPrice(if (noted()) id - 1 else id)
	}

	fun GenericItem.getHighAlchValue() = (value() * .6).toInt()

	/**
	 * Custom interaction function
	 */
	fun walkAndInteractWhile(
		target: InteractableEntity?,
		action: String,
		alwaysWalk: Boolean = false,
		allowWalk: Boolean = true,
		selectItem: Int = -1,
		useMenu: Boolean = true,
		whileWaiting: () -> Any = {}
	): Boolean {
		val t = target ?: return false
		val name = (t as Nameable).name()
		val pos = t.tile().getWalkableNeighbor()
		val destination = Movement.destination()

		turnRunOn()
		logger.info("Interacting with: $name at: $pos")
		if (Menu.opened() && Menu.contains {
				it.action.equals(action, true) && (name == null || it.option.contains(
					name,
					true
				))
			}) {
			debug("Clicking directly on opened menu")
			return handleMenu(action, name)
		}

		if (pos != null && allowWalk && destination != pos) {
			val triggerDistance = if (alwaysWalk) 4 else 12
			val targetTile = if (destination == Tile.Nil) Players.local() else destination
			val distanceToTarget = pos.distanceTo(me)
			if (distanceToTarget > triggerDistance || !t.inViewport(true)) {
				debug(
					"Walking before interacting distance too big=${distanceToTarget > triggerDistance}, InViewport=${
						t.inViewport(true)
					}"
				)
				debug("destination=${destination}, targetTile=${targetTile}, pos=${pos}, distanceToTarget=${distanceToTarget}")
				Movement.step(pos)
				if (!waitForWhile(1000, { Movement.destination().distanceTo(pos) <= 5 })) {
					return false
				}
				waitForWhile(mid(), { !t.valid() || t.inViewport(true) }, whileWaiting)
			}
		}

		val selectedId = Inventory.selectedItem().id()
		if (selectedId != selectItem) {
			Game.tab(Game.Tab.INVENTORY)
			if (selectItem > -1) {
				Inventory.stream().id(selectItem).firstOrNull()?.interact("Use", useMenu)
			} else {
				Inventory.stream().id(selectedId).firstOrNull()?.click()
			}

		}
		val interactBool =
			if (name == null || name == "null" || name.isEmpty()) t.interact(action, useMenu) else t.interact(
				action,
				name,
				useMenu
			)
		return waitForWhile(short(), {
			Inventory.selectedItemIndex() == -1 || Inventory.selectedItem().id() == selectItem
		}, whileWaiting) && interactBool
	}

	/**
	 * Custom interaction function
	 */
	fun walkAndInteract(
		target: InteractableEntity?,
		action: String,
		alwaysWalk: Boolean = false,
		allowWalk: Boolean = true,
		selectItem: Int = -1,
		useMenu: Boolean = true
	): Boolean {
		return walkAndInteractWhile(target, action, alwaysWalk, allowWalk, selectItem, useMenu)
	}

	/**
	 * Requires menu to be open
	 */
	fun handleMenu(action: String, name: String?): Boolean {
		if (!Menu.opened()) {
			return false
		}
		if (!Menu.contains {
				it.action.equals(action, true) && (name == null || it.option.contains(
					name,
					true
				))
			}) {
			debug("Closing menu in: handleMenu()")
			Menu.click { it.action == "Cancel" }
			waitFor { !Menu.opened() }
			return false
		}
		return Menu.click { it.action.contains(action, true) && (name == null || it.option.contains(name, true)) }
	}

	fun Locatable.distance(): Int =
		tile().distanceTo(Players.local()).toInt()

	fun Tile.distanceM(dest: Locatable): Int {
		return abs(dest.tile().x() - x()) + abs(dest.tile().y() - y())
	}

	fun Locatable.onMap(): Boolean = tile().matrix().onMap()

	fun Locatable.mapPoint(): Point = Game.tileToMap(tile())

	fun Tile.toRegionTile(): Tile {
		val mos = Game.mapOffset()
		return Tile(x() - mos.x(), y() - mos.y(), floor())
	}

	fun Equipment.containsOneOf(vararg ids: Int): Boolean = stream().anyMatch { it.id() in ids }
	fun Bank.containsOneOf(vararg ids: Int): Boolean = (stream().firstOrNull { it.id() in ids }?.stack ?: 0) > 0
	fun Inventory.containsOneOf(vararg ids: Int): Boolean = stream().anyMatch { it.id() in ids }
	fun Inventory.containsAll(vararg ids: Int): Boolean {
		val inv = stream().list()
		return ids.all { id -> inv.any { id == it.id } }
	}

	fun Inventory.emptyExcept(vararg ids: Int): Boolean = stream().firstOrNull { it.id() !in ids } == null

	fun Inventory.emptySlots(): Int = (28 - stream().count()).toInt()
	fun Inventory.getCount(vararg names: String): Int = getCount(true, *names)
	fun Inventory.getCount(vararg ids: Int): Int = getCount(true, *ids)
	fun Inventory.getCount(countStacks: Boolean, vararg ids: Int): Int {
		val items = stream().id(*ids).list()
		return if (countStacks) items.sumOf { it.stack } else items.count()
	}

	fun Inventory.getCount(countStacks: Boolean, vararg names: String): Int {
		val items = stream().name(*names).list()
		return if (countStacks) items.sumOf { it.stack } else items.count()
	}

	fun Bank.withdrawExact(items: Map<Int, Int>): Boolean {
		return items.all { withdrawExact(it.key, it.value) }
	}

	fun Bank.withdrawExact(id: Number, amount: Int, wait: Boolean = true): Boolean {
		val id = id.toInt()
		if (id <= 0) {
			return false
		}
		debug("WithdrawExact: $id, $amount")
		val currentAmount = Inventory.getCount(true, id)
		if (currentAmount < amount) {
			val withdrawCount = amount - currentAmount
			if (!containsOneOf(id)) {
				debug("No: ${ItemLoader.lookup(id)?.name()} with id=$id in bank")
				return false
			} else if (withdrawCount > 1 && withdrawCount >= stream().id(id).count(true)) {
				debug("Withdrawing all: $id, since bank contains too few")
				withdraw(id, Bank.Amount.ALL)
			} else if (withdrawCount >= Inventory.emptySlots() && ItemLoader.lookup(id)
					?.stackable() == false
			) {
				debug("Withdrawing all: $id, since there's just enough space")
				withdraw(id, Bank.Amount.ALL)
			} else if (withdrawCount in 2..4) {
				repeat(withdrawCount) {
					withdraw(id, 1)
				}
			} else if (!withdraw(id, withdrawCount)) {
				return false
			}
		} else if (currentAmount > amount) {
			deposit(id, Bank.Amount.ALL)
			if (wait) waitFor { !Inventory.containsOneOf(id) }
			return false
		}

		val success = if (wait) waitFor(5000) { Inventory.getCount(true, id) == amount } else true
		debug("Withdrawing was a success=$success, inventoryCount=${Inventory.getCount(true, id)}, amount=$amount")
		return success
	}

	/**
	 * Only useful for mobile
	 */
	fun closeOpenHUD(): Boolean {
		val tab = Game.tab()
		if (tab == Game.Tab.NONE) {
			return true
		}
		val c: Component = Widgets.widget(601).firstOrNull { (it?.textureId() ?: -1) in tab.textures } ?: return true
		return c.click()
	}


	fun currentHP(): Int = Skills.level(Constants.SKILLS_HITPOINTS)
	fun maxHP(): Int = Skills.realLevel(Constants.SKILLS_HITPOINTS)
	fun missingHP(): Int = maxHP() - currentHP()


	@JvmOverloads
	fun Locatable.getWalkableNeighbor(
		allowSelf: Boolean = true,
		diagonalTiles: Boolean = false,
		checkForWalls: Boolean = true,
		filter: (Tile) -> Boolean = { true },
	): Tile? {
		val walkableNeighbors = getWalkableNeighbors(allowSelf, diagonalTiles, checkForWalls)
		return walkableNeighbors.filter(filter).minByOrNull { it.distance() }
	}

	fun Locatable.canReach(): Boolean = getWalkableNeighbor(checkForWalls = false)?.reachable() == true

	@JvmOverloads
	fun Locatable.getWalkableNeighbors(
		allowSelf: Boolean = true,
		diagonalTiles: Boolean = false,
		checkForWalls: Boolean = true,
	): MutableList<Tile> {

		val t = tile()
		val x = t.x()
		val y = t.y()
		val f = t.floor()
		val cm = Movement.collisionMap(t.floor).flags()
		//the tile itself is not blocked, just return that...
		if (allowSelf && !t.blocked(cm)) {
			return mutableListOf(t)
		}

		val n = Tile(x, y + 1, f)
		val e = Tile(x + 1, y, f)
		val s = Tile(x, y - 1, f)
		val w = Tile(x - 1, y, f)
		val straight = listOf(n, e, s, w)
		val straightFlags = listOf(Flag.W_S, Flag.W_W, Flag.W_N, Flag.W_E)
		val ne = Tile(x + 1, y + 1, f)
		val se = Tile(x + 1, y - 1, f)
		val sw = Tile(x - 1, y - 1, f)
		val nw = Tile(x - 1, y + 1, f)
		val diagonal = listOf(ne, se, sw, nw)

		val walkableNeighbors = mutableListOf<Tile>()
		walkableNeighbors.addAll(straight.filterIndexed { i, it ->
			if (checkForWalls) {
				!it.blocked(
					cm,
					straightFlags[i]
				)
			} else !it.blocked(cm)
		})

		if (diagonalTiles) {
			walkableNeighbors.addAll(diagonal.filter { !it.blocked(cm) })
		}
		return walkableNeighbors
	}


	fun RunePouch.count(power: RunePower) = runes().filter { it.first.runePowers.contains(power) }.sumOf { it.second }

	fun RunePouch.count(rune: Rune) = runes().firstOrNull { it.first == rune }?.second ?: 0


	val BARROWS_REGEX = Regex("""(?<!\()\b\d+(?!\))""")
	val CHARGES_REGEX = Regex("""\(?\b\d+\)?""")

	fun String.stripBarrowsCharge() = replace(BARROWS_REGEX, "").trimEnd()
	fun String.stripNumbersAndCharges() = replace(CHARGES_REGEX, "").trimEnd()

	val TAG_REGEX = Regex("""<[^>]*>""")

	fun String.stripTags() = replace(TAG_REGEX, "").trimEnd()
	fun String.uppercaseFirst() = lowercase().replaceFirstChar { it.uppercase() }
}