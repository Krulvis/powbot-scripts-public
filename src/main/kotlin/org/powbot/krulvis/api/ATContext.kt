package org.powbot.krulvis.api

import org.powbot.api.*
import org.powbot.api.rt4.*
import org.powbot.api.rt4.magic.Rune
import org.powbot.api.rt4.magic.RunePouch
import org.powbot.api.rt4.magic.RunePower
import org.powbot.api.rt4.walking.local.Flag
import org.powbot.api.rt4.walking.local.LocalPathFinder
import org.powbot.api.rt4.walking.model.Skill
import org.powbot.api.waiter.ActionWaiter
import org.powbot.krulvis.api.antiban.DelayHandler
import org.powbot.krulvis.api.antiban.OddsModifier
import org.powbot.krulvis.api.extensions.Utils.mid
import org.powbot.krulvis.api.extensions.Utils.short
import org.powbot.krulvis.api.extensions.Utils.sleep
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.krulvis.api.extensions.Utils.waitForDistanceWhile
import org.powbot.krulvis.api.extensions.Utils.waitForWhile
import org.powbot.mobile.rscache.loader.ItemLoader
import org.slf4j.LoggerFactory
import kotlin.math.abs
import kotlin.math.max


object ATContext {
    val logger = LoggerFactory.getLogger(ATContext.javaClass.simpleName)

    val me: Player get() = Players.local()

    const val RUN_THRESHOLD_VARP = 3190

    var nextRun = Random.nextInt(2, 5)

    val walkDelay = DelayHandler(2500, OddsModifier(), "Walking delay")

    var debugComponents: Boolean = true

    fun debug(msg: String) {
        if (debugComponents) {
            logger.info(msg)
        }
    }

    fun fullPrayer() = Skills.realLevel(Skill.Prayer) == Skills.level(Skill.Prayer)

    fun Prayer.disablePrayers() {
        val activePrayers = activePrayers()
        activePrayers.forEach {
            prayer(it, false)
            sleep(20)
        }
    }

    // Helper extension function to get surrounding tiles
    fun Tile.surroundingTiles(includeCorners: Boolean = false): List<Tile> {
        val list = mutableListOf(
            Tile(x + 1, y),     // East
            Tile(x - 1, y),     // West
            Tile(x, y + 1),     // North
            Tile(x, y - 1),     // South
        )
        if (includeCorners) {
            list.add(Tile(x - 1, y - 1))
            list.add(Tile(x - 1, y + 1))
            list.add(Tile(x + 1, y + 1))
            list.add(Tile(x + 1, y - 1))
        }
        return list
    }

    fun Array<Tile>.center(): Tile {
        if (isEmpty()) return Tile.Nil

        val sumX = sumOf { it.x }
        val sumY = sumOf { it.y }
        val count = size

        return Tile(sumX / count, sumY / count, first().floor())
    }

    fun Actor<*>.gargoyle() = name().contains("Gargoyle", true)

    fun Actor<*>.animating() = animation() != -1

    const val GARGOYLE_DESTROY_ANIM = 1520
    fun Actor<*>.dead() =
        !valid() || (healthBarVisible() && if (gargoyle()) animation() == GARGOYLE_DESTROY_ANIM else healthPercent() == 0)

    fun Actor<*>.alive() = valid() && (!healthBarVisible() || healthPercent() > 0)

    fun runThreshold() = Varpbits.varpbit(RUN_THRESHOLD_VARP, 17, 127)

    fun turnRunOn(): Boolean {
        if (Movement.running() || runThreshold() > 0) {
            return true
        }
        if (Movement.energyLevel() >= Random.nextInt(1, 5)) {
            return Movement.running(true)
        }
        return false
    }

    fun List<Tile>.atLastTile(distance: Int = 2): Boolean {
        val dest = Movement.destination()
        return if (dest.valid()) {
            last().distanceTo(Movement.destination()) <= distance
        } else {
            last().distanceTo(me.trueTile()) <= distance
        }
    }

    fun List<Tile>.nearPath(maxDistance: Int = 12): Boolean {
        val pos = me.trueTile()
        return any { it.distanceTo(pos) <= maxDistance }
    }

    fun List<Tile>.traverse(offset: Int = 1, distanceToLastTile: Int = 2, whileWaiting: () -> Any = {}): Boolean {
        debug("List<Tile>.traverse(offset=$offset, distanceToLastTile=$distanceToLastTile, whileWaiting=$whileWaiting)")
        if (atLastTile(offset) && Components.stream(219).id(1).viewable().isEmpty()) {
            debug("Already at last tile...")
            return true
        }
        val next = lastOrNull { it.onMap() }
        if (next == null) {
            debug("Can't find walkableTile? none are on minimap")
            return false
        }
        var destination = Movement.destination()

        if (destination.distanceTo(next) <= distanceToLastTile || Movement.stepNoConfirm(next)) {
            debug("Successfully stepped to tileToClick=${next}")
            return waitForWhile(mid(), { lastOrNull { it.onMap() } != next }) { whileWaiting() }
        } else {
            debug("Failed Movement.step(${next}, minDistance=0)")
        }

        val last = last()
        destination = Movement.destination()
        val distanceToLast = last.distanceTo(destination)
        debug("Standing on ${destination}, lastTile=${last}, distance=${distanceToLast}, closeEnough=${distanceToLast <= distanceToLastTile}")
        return false
    }

    fun Movement.moving(): Boolean = destination() != Tile.Nil

    fun Locatable.distanceToDest(): Double {
        val destination = Movement.destination()
        return if (destination.valid()) {
            distanceTo(destination)
        } else {
            distance()
        }
    }

    fun Movement.stepNoConfirm(tile: Tile, perfectInteract: Boolean = false): Boolean {
        val matrix = tile.matrix()
        logger.info("stepNoConfirm: tile=$tile")
        if (tile.distance() < 10 && matrix.inViewport()) {
            logger.info("Using matrix to walk")
            val point = matrix.nextPoint()
            return point.interact("Walk here")
        }
        var pos = Game.tileToMap(tile)
        if (pos == Point.Nil) {
            val currentTile = Players.local().tile()
            val dx = tile.x() - currentTile.x()
            val dy = tile.y() - currentTile.y()
            val length = kotlin.math.sqrt((dx * dx + dy * dy).toDouble())

            // Normalize and scale to desired distance (14)
            val scale = 12.0 / length
            val newX = currentTile.x() + (dx * scale).toInt()
            val newY = currentTile.y() + (dy * scale).toInt()

            // Create new tile at calculated position and try to map it
            val newTile = Tile(newX, newY, currentTile.floor())
            pos = newTile.mapPoint()
        }
        logger.info("Using point with Input.tap to walk: pos=$pos")
        return Input.tap(pos)
    }

    fun walk(position: Tile?, enableRun: Boolean = true, forceMinimap: Boolean = false): Boolean {
        if (position == null || position == Tile.Nil) {
            return true
        }
        val position = if (!position.loaded() || !position.blocked()) position else position.getWalkableNeighbor()
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
        allowWalk: Boolean = true,
        selectItem: Int = -1,
        whileWaiting: () -> Any = {},
        maxDistance: Int = 12,
        perfectInteract: Boolean = false
    ): Boolean {
        val t = target ?: return false
        val name = (t as Nameable).name()
        val pos = t.tile().getWalkableNeighbor() ?: t.tile()
        val destination = Movement.destination()
        val distanceToPos = if (destination != Tile.Nil) pos.distanceTo(destination) else pos.distance()

        if (t is Modelable<*>) {
//			InteractiveState.setRenderOnly(t.renderables())
        }

        turnRunOn()
        var point = t.nextPoint()
        var inViewport = point.inViewport()
        debug("Interacting with: $name pos=$pos, point=$point, inViewport=$inViewport")
        if (!inViewport || distanceToPos > maxDistance) {
            val startWait = System.currentTimeMillis()
            debug("Not in viewport, walking before interacting distance=${distanceToPos}, allowWalk=$allowWalk")
            if (!allowWalk) {
                return false
            }
            Movement.stepNoConfirm(pos)
            if (!waitForWhile(1000, { Movement.destination().distanceTo(pos) <= 5 })) {
                return false
            }
            inViewport = waitForDistanceWhile(pos, extraWait = mid(), whileWaiting = whileWaiting, condition = {
                point = t.nextPoint()
                !t.valid() || point.inViewport()
            })

            if (!inViewport) {
                debug("After waiting for ${System.currentTimeMillis() - startWait} ms, point=$point still not in viewport")
                return false
            }
        }


        val selectedId = Inventory.selectedItem().id()
        if (selectedId != selectItem) {
            Game.tab(Game.Tab.INVENTORY)
            if (selectItem > -1) {
                Inventory.stream().id(selectItem).firstOrNull()?.interact("Use")
            } else {
                Inventory.stream().id(selectedId).firstOrNull()?.click()
            }
        }
        return waitForWhile(short(), {
            Inventory.selectedItemIndex() == -1 || Inventory.selectedItem().id() == selectItem
        }, whileWaiting) && if (perfectInteract) point.interact(action, name) else t.interact(action, name)
    }

    private fun Point.inViewport(checkIfObstructed: Boolean = true) =
        this != Point.Nil && Game.inViewport(this, checkIfObstructed)

    fun Point.interact(action: String, name: String? = null): Boolean {
        Game.setSingleTapToggle(true)
        val waiter = ActionWaiter(action, name)
        val nullWaiter = ActionWaiter("Walk here", "")
        waiter.reset()

        try {
            if (Menu.opened() && handleMenu(action, name)) {
                Game.setSingleTapToggle(false)
                if (waitFor(500) { waiter.finished() }) {
                    waiter.unregister()
                    return true
                }
            }
            debug("interacting on point=$this, action=$action, name=$name")
            if (Game.openTabBounds().contains(this) && !Game.closeOpenTab()) {
                debug("couldn't close obstructing tab, returning false")
            } else if (Input.tap(this) && waitFor(500) { Menu.opened() || nullWaiter.finished() || waiter.finished() }) {
                if (Menu.opened() && handleMenu(action, name)) {
                    if (waitFor(500) { waiter.finished() }) {
                        debug("Successful interaction waiter is finished=${waiter.finished()}")
                        Game.setSingleTapToggle(false)
                        nullWaiter.unregister()
                        waiter.unregister()
                        return true
                    }
                } else {
                    debug("Failed to do menu interaction")
                }
            }
        } catch (e: Exception) {
            logger.info("Exception in point.interact")
            logger.error(e.stackTraceToString())
        }
        Game.setSingleTapToggle(false)
        nullWaiter.unregister()
        waiter.unregister()
        return false
    }

    /**
     * Custom interaction function
     */
    fun walkAndInteract(
        target: InteractableEntity?,
        action: String,
        allowWalk: Boolean = true,
        selectItem: Int = -1,
        maxDistance: Int = 12,
        perfectInteract: Boolean = false
    ): Boolean {
        return walkAndInteractWhile(
            target,
            action,
            allowWalk,
            selectItem,
            maxDistance = maxDistance,
            perfectInteract = true
        )
    }

    /**
     * Requires menu to be open
     */
    private fun handleMenu(action: String, name: String?): Boolean {
        val index = Menu.indexOf(Menu.TextFilter(action, name))
        if (index < 0) {
            debug("Closing menu in: handleMenu()")
            Menu.click { it.action == "Cancel" }
            Condition.wait({ !Menu.opened() }, 20, 100)
            return false
        }
        val slotPoint = Menu.nextSlotPoint(index)
        return Input.tap(slotPoint)
    }

    fun Locatable.distance(): Int =
        tile().distanceTo(Players.local()).toInt()

    fun Tile.distanceM(dest: Locatable): Int {
        return abs(dest.tile().x() - x()) + abs(dest.tile().y() - y())
    }

    /**
     * Chebyshev distance calc
     */
    fun Locatable.distanceC(other: Locatable): Int {
        val dx = abs(tile().x - other.tile().x)
        val dy = abs(tile().y - other.tile().y)
        return max(dx, dy)
    }

    fun Locatable.onMap(): Boolean = tile().matrix().onMap()

    fun Locatable.mapPoint(): Point = Game.tileToMap(tile())

    fun Tile.toRegionTile(): Tile {
        val mos = Game.mapOffset()
        return Tile(x() - mos.x(), y() - mos.y(), floor())
    }

    fun DepositBox.depositAllInventoryExcept(vararg ids: Int): Boolean {
        val filter = object : Filter<Item> {
            override fun accept(t: Item?): Boolean {
                return t != null && t.type == ItemType.DEPOSIT_BOX && t.id in ids
            }
        }
        return depositAllExcept(filter)
    }

    fun Bank.depositInventoryButton() = Components.stream(12).action("Deposit inventory").first()
    fun Bank.isOutOf(name: String) = waitFor(5000) { opened() && stream().name(name).count() <= 0 }
    fun Equipment.containsOneOf(vararg ids: Int): Boolean = stream().anyMatch { it.id() in ids }
    fun Bank.containsOneOf(vararg ids: Int): Boolean = (stream().firstOrNull { it.id() in ids }?.stack ?: 0) > 0
    fun Inventory.containsOneOf(vararg ids: Int): Boolean = stream().anyMatch { it.id() in ids }
    fun Inventory.containsAll(vararg ids: Int): Boolean {
        val inv = stream().list()
        return ids.all { id -> inv.any { id == it.id } }
    }

    fun Inventory.emptyExcept(vararg names: String): Boolean = stream().none { item ->
        names.none { strippedNamed -> item.name().contains(strippedNamed, true) }
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
        //the tile itself is not blocked, just return that...
        if (allowSelf && !t.blocked()) {
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
                    straightFlags[i]
                )
            } else !it.blocked()
        })

        if (diagonalTiles) {
            walkableNeighbors.addAll(diagonal.filter { !it.blocked() })
        }
        return walkableNeighbors
    }


    fun RunePouch.count(power: RunePower) = runes().filter { it.first.runePowers.contains(power) }.sumOf { it.second }

    fun RunePouch.count(rune: Rune) = runes().firstOrNull { it.first == rune }?.second ?: 0


    val BARROWS_REGEX = Regex("""(?<!\()\b\d+(?!\))""")
    val CHARGES_REGEX = Regex("""\(?\b\d+\)?""")
    val CHARGES_REGEX_ONLY_NUMBERS = Regex("""\(\b(\d+)\)""")

    fun String.stripBarrowsCharge() = replace(BARROWS_REGEX, "").trimEnd()
    fun String.stripNumbersAndCharges() = replace(CHARGES_REGEX, "").trimEnd()

    fun Item.charges() = CHARGES_REGEX_ONLY_NUMBERS.find(name())?.value?.toIntOrNull() ?: 0

    val TAG_REGEX = Regex("""<[^>]*>""")

    fun String.stripTags() = replace(TAG_REGEX, "").trimEnd()
    fun String.uppercaseFirst() = lowercase().replaceFirstChar { it.uppercase() }
}