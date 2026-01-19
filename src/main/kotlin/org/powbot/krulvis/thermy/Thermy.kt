package org.powbot.krulvis.thermy

import org.powbot.api.EventFlows
import org.powbot.api.Tile
import org.powbot.api.event.TickEvent
import org.powbot.api.rt4.*
import org.powbot.api.script.OptionType
import org.powbot.api.script.ScriptCategory
import org.powbot.api.script.ScriptConfiguration
import org.powbot.api.script.ScriptManifest
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.extensions.Timer
import org.powbot.krulvis.api.extensions.requirements.EquipmentRequirement
import org.powbot.krulvis.api.extensions.requirements.InventoryRequirement
import org.powbot.krulvis.api.extensions.teleports.Teleport
import org.powbot.krulvis.api.extensions.teleports.TeleportMethod
import org.powbot.krulvis.api.extensions.teleports.poh.openable.CASTLE_WARS_JEWELLERY_BOX
import org.powbot.krulvis.api.script.KillerScript
import org.powbot.krulvis.api.script.painter.KrulPaint
import org.powbot.krulvis.fighter.BANK_TELEPORT_OPTION
import org.powbot.krulvis.fighter.EQUIPMENT_OPTION
import org.powbot.krulvis.fighter.INVENTORY_OPTION
import org.powbot.krulvis.thermy.tree.branch.AtThermy

const val SAFESPOT_OPTION = "Safespot"

@ScriptManifest(
    "krul Thermy",
    "Kills Thermonuclear Smoke Devil",
    "Krulvis",
    "1.0.0",
    category = ScriptCategory.Combat,
    priv = true
)
@ScriptConfiguration.List(
    [
        ScriptConfiguration(EQUIPMENT_OPTION, "What to wear?", OptionType.EQUIPMENT),
        ScriptConfiguration(INVENTORY_OPTION, "What to bring?", OptionType.INVENTORY),
        ScriptConfiguration(SAFESPOT_OPTION, "Safespot", OptionType.BOOLEAN, defaultValue = "true"),
        ScriptConfiguration(
            BANK_TELEPORT_OPTION, "How to get to bank?", OptionType.STRING,
            defaultValue = CASTLE_WARS_JEWELLERY_BOX, allowedValues = ["NONE", CASTLE_WARS_JEWELLERY_BOX]
        ),
    ]
)
class Thermy : KillerScript() {
    val equipment by lazy { EquipmentRequirement.forOption(getOption(EQUIPMENT_OPTION)) }
    val inventory by lazy { InventoryRequirement.forOption(getOption(INVENTORY_OPTION)) }
    val safespot by lazy { getOption<Boolean>(SAFESPOT_OPTION) }
    val bankTeleport by lazy { TeleportMethod(Teleport.forName(getOption(BANK_TELEPORT_OPTION))) }

    var thermy = Npc.Nil
    var tt = Tile.Nil
    val freezeTimer = Timer(21000)
    var closestMaxDistTile = Tile.Nil

    val startTile = Tile(2362, 9443, 0)
    val minDistance = 10
    override val ammoIds: IntArray = intArrayOf(-1)

    //Freezing
    val freezeIds = arrayOf(368)
    var freezeTile = Tile.Nil


    override fun GroundItem.isLoot(): Boolean {
        return name() in LOOT
    }

    override fun createPainter(): KrulPaint<*> = ThermyPainter(this)

    override val rootComponent: TreeComponent<*> = AtThermy(this)

    override fun onStart() {
        super.onStart()
        EventFlows.collectTicks {
            onTick(it)
        }
        freezeTimer.stop()
    }

    fun onTick(e: TickEvent) {
        thermy = Npcs.stream().name("Thermonuclear smoke devil").first()
        val thermyTile = thermy.tile()
        tt = arrayOf(thermyTile, thermyTile.derive(-1, -1)).minByOrNull { it.distanceTo(me.trueTile()) } ?: thermyTile
        val projectiles = Projectiles.stream().toList()
        projectiles.forEach { projectile ->
            if (projectile.target() == thermy && freezeIds.contains(projectile.id)) {
                freezeTile = tt
            }
        }
        findTileAtRange()
        if (freezeTile != tt && freezeTimer.getRemainder() >= 6000) {
            logger.warn("Detected change in Thermy tile, resetting freeze timer.")
            freezeTimer.stop()
        }
    }

    fun findTileAtRange() {
        val myPos = me.trueTile()
        val flags = Movement.collisionMap(myPos.floor).flags()
        // Generate a list of tiles that are exactly 15 tiles away in various directions
        val potentialTiles = mutableListOf<Tile>()
        arrayOf(-minDistance, minDistance).forEach { d ->
            (-minDistance..minDistance).forEach { dd ->
                val derivedY = tt.derive(d, dd)
                if (!derivedY.blocked(flags)) {
                    potentialTiles.add(derivedY)
                }
                val derivedX = tt.derive(dd, d)
                if (!derivedX.blocked(flags)) {
                    potentialTiles.add(derivedX)
                }
            }
        }

        // Find the closest suitable tile to the player
        closestMaxDistTile = potentialTiles.minByOrNull { tile ->
            tile.distanceTo(myPos)
        } ?: Tile.Nil
    }

    val LOOT = arrayOf(
        "Rune dagger",
        "Rune chainbody",
        "Rune scimitar",
        "Red d'hide body",
        "Rune battleaxe",
        "Mystic air staff",
        "Mystic fire staff",
        "Rune knife(p++)",
        "Dragon scimitar",
        "Ancient staff",
        "Occult necklace",
        "Smoke battlestaff",
        "Dragon chainbody",
        "Smoke rune",
        "Air rune",
        "Soul rune",
        "Rune arrow",
        "Sanfew serum(4)",
        "Prayer potion(4)",
        "Mithril bar",
        "Magic logs",
        "Gold ore",
        "Diamond",
        "Raw shark",
        "Shark lure",
        "Coins",
        "Grimy toadflax",
        "Desert goat horn",
        "Onyx bolt tips",
        "Snapdragon seed",
        "Ranarr seed",
        "Magic seed",
        "Dragonstone ring",
        "Crystal key",
        "Jar of smoke",
        "Clue"
    )
}

fun main() {
    Thermy().startScript("127.0.0.1", "GIM", true)
}