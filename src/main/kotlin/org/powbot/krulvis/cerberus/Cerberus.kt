package org.powbot.krulvis.cerberus

import org.powbot.api.EventFlows
import org.powbot.api.Tile
import org.powbot.api.event.NpcAnimationChangedEvent
import org.powbot.api.event.ProjectileDestinationChangedEvent
import org.powbot.api.event.TickEvent
import org.powbot.api.rt4.*
import org.powbot.api.script.OptionType
import org.powbot.api.script.ScriptCategory
import org.powbot.api.script.ScriptConfiguration
import org.powbot.api.script.ScriptManifest
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.ATContext.currentHP
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.extensions.Timer
import org.powbot.krulvis.api.extensions.items.Weapon.Companion.weapon
import org.powbot.krulvis.api.extensions.requirements.EquipmentRequirement
import org.powbot.krulvis.api.extensions.requirements.InventoryRequirement
import org.powbot.krulvis.api.extensions.teleports.Teleport
import org.powbot.krulvis.api.extensions.teleports.TeleportMethod
import org.powbot.krulvis.api.extensions.teleports.poh.openable.CASTLE_WARS_JEWELLERY_BOX
import org.powbot.krulvis.api.script.KillerScript
import org.powbot.krulvis.api.script.painter.KrulPaint
import org.powbot.krulvis.api.script.tree.branch.ShouldConsume
import org.powbot.krulvis.cerberus.Data.FLINCH_OPTION
import org.powbot.krulvis.cerberus.tree.branch.AtCerb
import org.powbot.krulvis.fighter.BANK_TELEPORT_OPTION
import org.powbot.krulvis.fighter.EQUIPMENT_OPTION
import org.powbot.krulvis.fighter.INVENTORY_OPTION
import org.powbot.krulvis.fighter.SPECIAL_EQUIPMENT_OPTION

@ScriptManifest(
    "krul Cerberus",
    "Kills Cerberus",
    "Krulvis",
    "1.0.0",
    category = ScriptCategory.Combat,
    scriptId = "8300535a-3b11-4a31-9b65-86a4b0302bc8",
    priv = true
)
@ScriptConfiguration.List(
    [
        ScriptConfiguration(EQUIPMENT_OPTION, "What to wear?", OptionType.EQUIPMENT, defaultValue = ""),
        ScriptConfiguration(SPECIAL_EQUIPMENT_OPTION, "What to wear for special attack?", OptionType.EQUIPMENT),
        ScriptConfiguration(INVENTORY_OPTION, "What to bring?", OptionType.INVENTORY),
        ScriptConfiguration(FLINCH_OPTION, "Flinch Cerberus (walk under)?", OptionType.BOOLEAN, defaultValue = "true"),
        ScriptConfiguration(
            BANK_TELEPORT_OPTION, "How to get to bank?", OptionType.STRING,
            defaultValue = CASTLE_WARS_JEWELLERY_BOX, allowedValues = ["NONE", CASTLE_WARS_JEWELLERY_BOX]
        ),
    ]
)
class Cerberus : KillerScript(false) {
    val equipment by lazy { EquipmentRequirement.forOption(getOption(EQUIPMENT_OPTION)) }
    val equipmentSpecial by lazy { EquipmentRequirement.forOption(getOption(SPECIAL_EQUIPMENT_OPTION)) }
    val specWeapon by lazy { equipmentSpecial.weapon() }
    val inventory by lazy { InventoryRequirement.forOption(getOption(INVENTORY_OPTION)) }
    val flinch by lazy { getOption<Boolean>(FLINCH_OPTION) }
    val bankTeleport by lazy { TeleportMethod(Teleport.forName(getOption(BANK_TELEPORT_OPTION))) }
    var cerberus = Npc.Nil

    override val ammoIds: IntArray = intArrayOf(-1)

    override fun GroundItem.isLoot(): Boolean = name() in loot

    override fun createPainter(): KrulPaint<*> = CerbPainter(this)

    override val rootComponent: TreeComponent<*> = AtCerb(this)

    val shouldConsume = ShouldConsume(this, SimpleLeaf(this, "N/A") {}, waitForConsume = false)

    var cerbTile = Tile.Nil
    var centerTile = Tile.Nil
    val flinchTimer = Timer(3200)
    var door = GameObject.Nil
    var cerbKills = 0
    override fun onStart() {
        super.onStart()
        door = findDoor()
        EventFlows.collectTicks { onTick(it) }
        EventFlows.collectNpcAnimationChanges { onNpcAnim(it) }
        onTick(TickEvent())
    }

    fun onNpcAnim(e: NpcAnimationChangedEvent) {
        if (e.npc == cerberus) {
            logger.info("Cerb Animation changed to ${e.animation}")
            if (e.animation == 4495) {
                cerbKills++
            }
        }
    }

    override fun getGroundLoot(): List<GroundItem> {
        return GroundItems.stream().name(*loot).toList()
    }

    var cerbHP = 100
    fun onTick(e: TickEvent) {
        cerberus = Npcs.stream().name("Cerberus").first()
        cerbTile = cerberus.trueTile()
        centerTile = Tile(cerbTile.x + 2, cerbTile.y + 2)
        if (cerberus.healthBarVisible()) {
            cerbHP = cerberus.healthPercent()
            flinchTimer.reset()
        } else if (!cerberus.actions.contains("Attack")) {
            cerbHP = 100
            flinchTimer.reset()
        }
        if (me.healthBarVisible()) {
            flinchTimer.reset()
        }
        door = findDoor()
    }

    override fun canBreak(): Boolean {
        return false
    }

    fun hasAttackOption(): Boolean = cerberus.actions.contains("Attack")

    fun clickDoor() {
        door.bounds(-112, 144, -224, -50, -32, 2)
        door.interact("Exit")
    }

    val doorTiles = arrayOf(Tile(1304, 1289, 0))
    val spawnTiles = arrayOf(Tile(1304, 1317, 0))
    fun findDoor(): GameObject = doorTiles.firstNotNullOfOrNull {
        Objects.stream(it).type(GameObject.Type.INTERACTIVE).name("Portcullis").first()
    } ?: GameObject.Nil

    val loot = arrayOf(
        "Primordial crystal",
        "Pegasian crystal",
        "Eternal crystal",
        "Smouldering stone",
        "Rune platebody",
        "Rune full helm",
        "Rune pickaxe",
        "Rune axe",
        "Black d'hide body",
        "Lava battlestaff",
        "Rune 2h sword",
        "battlestaff",
        "Rune halberd",
        "Rune chainbody",
        "Soul rune",
        "Blood rune",
        "Cannonball",
        "Runite bolts (unf)",
        "Death rune",
        "Coal",
        "Super restore(4)",
        "Coins",
        "Dragon bones",
        "Wine of zamorak",
        "Ashes",
        "Fire orb",
        "Grimy torstol",
        "Runite ore",
        "Uncut diamond",
        "Ranarr seed",
        "Torstol seed",
        "Key master teleport",
        "Ensouled hellhound head",
        "Clue scroll (elite)",
        "Jar of souls",
    )
}

fun main() {
    Cerberus().startScript(useDefaultConfigs = false)
}