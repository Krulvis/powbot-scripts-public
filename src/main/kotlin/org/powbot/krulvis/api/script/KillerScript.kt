package org.powbot.krulvis.api.script

import kotlinx.coroutines.launch
import org.powbot.api.EventFlows
import org.powbot.api.PowDispatchers.Script
import org.powbot.api.Tile
import org.powbot.api.event.TickEvent
import org.powbot.api.rt4.*
import org.powbot.api.script.ScriptState
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.ATContext.stepNoConfirm
import org.powbot.krulvis.api.extensions.watcher.LootWatcher
import org.powbot.krulvis.api.extensions.watcher.NpcDeathWatcher
import org.powbot.krulvis.api.extensions.watcher.SpecialAttackWatcher
import org.powbot.mobile.script.ScriptManager

val defenseAnimations = arrayOf(4177, 420, 424, 1156)

abstract class KillerScript(val dodgeProjectiles: Boolean = true) : KrulScript(), Looting {

    abstract val ammoIds: IntArray
    var currentTarget: Npc = Npc.Nil
    var lootWachter: LootWatcher? = null
    val deathWatchers = mutableListOf<NpcDeathWatcher>()
    var kills: Int = 0
    private val slayerBraceletNames = arrayOf("Bracelet of slaughter", "Expeditious bracelet")
    fun getSlayerBracelet() = Inventory.stream().name(*slayerBraceletNames).first()
    val hasSlayerBracelet by lazy { getSlayerBracelet().valid() }
    var shouldWearSlayerBracelet = false
    fun wearingSlayerBracelet() = Equipment.stream().name(*slayerBraceletNames).isNotEmpty()

    var reducedStats = false

    override val ironmanLoot = mutableListOf<GroundItem>()
    abstract fun GroundItem.isLoot(): Boolean

    override fun onStart() {
        super.onStart()
        EventFlows.collectTicks { onKillerTickEvent(it) }
    }

    fun isLootWatcherActive() = lootWachter?.active == true
    private fun watchLootDrop(tile: Tile) {
        if (!isLootWatcherActive()) {
            logger.info("Waiting for loot at $tile")
            lootWachter = LootWatcher(tile, ammoIds, lootList = ironmanLoot, isLoot = { it.isLoot() })
        } else {
            logger.info("Already watching loot at tile: $tile for loot")
        }
    }

    open fun onDeath(npc: Npc) {
        kills++
        reducedStats = false
        if (hasSlayerBracelet && !wearingSlayerBracelet()) {
            val slayBracelet = getSlayerBracelet()
            if (slayBracelet.valid()) {
                shouldWearSlayerBracelet = true
                getSlayerBracelet().fclick()
                logger.info("Wearing bracelet on death at ${System.currentTimeMillis()}, cycle=${Game.cycle()}")
            }
        }
        watchLootDrop(if (npc.name.contains("kraken", true)) me.tile() else npc.tile())
    }

    private fun setCurrentTarget() {
        val interacting = me.interacting()
        if (interacting is Npc && interacting != Npc.Nil) {
            if ((interacting.valid() && interacting != currentTarget) || interacting.healthPercent() > 10) {
                shouldWearSlayerBracelet = false
            }
            currentTarget = interacting
            val activeLW = lootWachter
            if (activeLW?.active == true && activeLW.tile.distanceTo(currentTarget.tile()) < 2) return
            val deathWatcher = deathWatchers.firstOrNull { it.npc == currentTarget }
            if (deathWatcher == null || !deathWatcher.active) {
                val newDW = NpcDeathWatcher(interacting) { onDeath(interacting) }
                deathWatchers.add(newDW)
            }
        }
        deathWatchers.removeAll { !it.active }
    }

    var specWatcher: SpecialAttackWatcher? = null

    fun onKillerTickEvent(_e: TickEvent) {
        if (ScriptManager.state() != ScriptState.Running) return
        setCurrentTarget()
        val sWatcher = specWatcher
        if ((sWatcher == null || !sWatcher.active) && Combat.specialAttack()) {
            specWatcher = SpecialAttackWatcher(currentTarget) {
                reducedStats = true
            }
        }
        projectiles.removeIf {
            Game.cycle() > it.cycleEnd
        }
        val myDest = Movement.destination()
        val tile = if (myDest.valid()) myDest else me.trueTile()
        Projectiles.stream().filter { it.valid() && it.target() == Actor.Nil }.forEach {
            val dest = it.destination()
            logger.info(
                "Projectile id=${it.id} destination changed to ${dest}, distance=${dest.distanceTo(tile)}, cycle=${Game.cycle()} "
            )
            projectiles.add(it)
        }
        try {
            if (projectiles.any { it.valid() && it.destination() == tile }) {
                logger.info("Dangerous projectile spawned! tile=${tile}")
                findSafeSpotFromProjectile(currentTarget)
                val targetTile =
                    ironmanLoot.firstOrNull { loot -> loot.valid() && loot.tile.valid() && loot.tile != tile }?.tile
                        ?: projectileSafespot
                logger.info("Moving to target tile: $targetTile")
                Movement.stepNoConfirm(targetTile)
            }
        } catch (e: Exception) {
            logger.error(e.stackTraceToString() + "\n" + e.message + "\n" + e.cause?.message, e.cause ?: e)
        }
        if (melee && Projectiles.stream().any { it.valid() && it.target() == currentTarget }) {
            logger.info("Found projectile going to currentTarget, setting style to NOT melee")
            melee = false
        }
    }


    val projectiles: MutableList<Projectile> = mutableListOf()
    var projectileSafespot: Tile = Tile.Nil
    var melee = true

    private fun findSafeSpotFromProjectile(target: Npc) {
        val dangerousTiles = projectiles.map { it.destination() }
        val targetTile = target.tile()
        val centerTile = if (melee) targetTile else me.trueTile()
        val distanceToTarget = targetTile.distance()
        val grid = mutableListOf<Pair<Tile, Double>>()
        for (x in -2 until 2) {
            for (y in -2 until 2) {
                val t = Tile(centerTile.x + x, centerTile.y + y, centerTile.floor)
                if (t.blocked() || (!melee && target.distanceTo(t) < 1)) continue
                //If we are further than 5 tiles away, make sure that the tile is closer to the target so we don't walk further away
                if (distanceToTarget <= 5 || t.distanceTo(targetTile) < distanceToTarget) {
                    grid.add(t to dangerousTiles.minOf { it.distanceTo(t) })
                }
            }
        }
        projectileSafespot = grid.maxByOrNull { it.second }!!.first
        logger.info("Safe spot for projectile is $projectileSafespot, distance=${projectileSafespot.distance()}")
    }
}