package org.powbot.krulvis.araxxor

import com.google.common.eventbus.Subscribe
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.powbot.api.*
import org.powbot.api.event.*
import org.powbot.api.rt4.*
import org.powbot.api.rt4.walking.model.Skill
import org.powbot.api.script.OptionType
import org.powbot.api.script.ScriptCategory
import org.powbot.api.script.ScriptConfiguration
import org.powbot.api.script.ScriptManifest
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.ATContext.dead
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.ATContext.missingHP
import org.powbot.krulvis.api.ATContext.stepNoConfirm
import org.powbot.krulvis.api.ATContext.surroundingTiles
import org.powbot.krulvis.api.extensions.Monster.borderingLayer
import org.powbot.krulvis.api.extensions.ResurrectSpell
import org.powbot.krulvis.api.extensions.Timer
import org.powbot.krulvis.api.extensions.items.Food
import org.powbot.krulvis.api.extensions.items.Potion
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
import org.powbot.krulvis.araxxor.Data.ACIDIC
import org.powbot.krulvis.araxxor.Data.ARAXXOR
import org.powbot.krulvis.araxxor.Data.ARAXXOR_ATTACK_ANIMS
import org.powbot.krulvis.araxxor.Data.ARAXXOR_DEATH_ANIM
import org.powbot.krulvis.araxxor.Data.ENRAGED_ATTACK_ANIMATION
import org.powbot.krulvis.araxxor.Data.EXPLODING
import org.powbot.krulvis.araxxor.Data.HADUKEN_ATTACK_ANIM
import org.powbot.krulvis.araxxor.Data.MIRROR
import org.powbot.krulvis.araxxor.Data.SIZE
import org.powbot.krulvis.araxxor.Data.TOXIC_ATTACK_ANIM
import org.powbot.krulvis.araxxor.Data.findSafestTile
import org.powbot.krulvis.araxxor.Data.getEnrageQuickWalkTiles
import org.powbot.krulvis.araxxor.Data.predictAcidPools
import org.powbot.krulvis.araxxor.tree.branch.IsKilling
import org.powbot.krulvis.fighter.*
import org.powbot.util.TransientGetter2D

private const val KILL_SMALL_SPIDER_EQUIPMENT = "SmallSpiderEquip"
private const val KILL_GREEN_SPIDER = "KillGreenSpider"
private const val WOOX_WALK = "WooxWalk"
private const val MIN_FOOD_OPTION = "MinFood"

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
        ScriptConfiguration(
            KILL_SMALL_SPIDER_EQUIPMENT,
            "What to wear for white spider killing?",
            OptionType.EQUIPMENT
        ),
        ScriptConfiguration(KILL_GREEN_SPIDER, "Kill green spider?", OptionType.BOOLEAN),
        ScriptConfiguration(WOOX_WALK, "Quick step enrage phase?", OptionType.BOOLEAN),
        ScriptConfiguration(INVENTORY_OPTION, "What to bring in inv?", OptionType.INVENTORY),
        ScriptConfiguration(MIN_FOOD_OPTION, "Min food for 2nd kill", OptionType.INTEGER, defaultValue = "10"),
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
    val smallGear by lazy { EquipmentRequirement.forOption(getOption(KILL_SMALL_SPIDER_EQUIPMENT)) }
    val killGreen by lazy { getOption<Boolean>(KILL_GREEN_SPIDER) }
    val quickStep by lazy { getOption<Boolean>(WOOX_WALK) }
    val allEquipment by lazy { (equipment + specEquipment + smallGear).distinct() }
    val inventory by lazy {
        InventoryRequirement.forOption(getOption(INVENTORY_OPTION))
            .filterNot { inv -> allEquipment.any { inv.item.id in it.item.ids } }
    }
    val specWeapon by lazy {
        Weapon.forId(
            specEquipment.firstOrNull { it.slot == Equipment.Slot.MAIN_HAND }?.item?.id ?: -1
        )
    }
    val minFood by lazy { getOption<Int>(MIN_FOOD_OPTION) }
    val resurrectSpell = ResurrectSpell.GREATER_GHOST
    val bankTeleport by lazy { TeleportMethod(Teleport.forName(getOption(BANK_TELEPORT_OPTION))) }
    val araxTeleport by lazy { TeleportMethod(Teleport.forName(getOption(MONSTER_TELEPORT_OPTION))) }
    var debugPaint = false
    val offensive = Prayer.Effect.PIETY
    var defensive = Prayer.Effect.PROTECT_FROM_MELEE

    var kills = 0
    var deaths = 0
    var banking = false
    var inside = false
    var npcs = listOf(Npc.Nil)
    var araxxor = Npc.Nil
    var mirror = Npc.Nil
    var acidic = Npc.Nil
    var exploding = Npc.Nil
    var target = Npc.Nil
    var araxxorRect = Rectangle.Nil
    var araxxorCenter = Tile.Nil
    var newPosition = Tile.Nil

    //Acid pools
    var unsafeTiles = emptyList<Tile>()
    val puke = mutableListOf<Projectile>()
    var pools = emptyList<GameObject>()
    lateinit var flags: TransientGetter2D<Int>

    //Enrage
    var enrage = false
    var enrageTiles = emptyArray<Tile>()
    var borderTiles = emptyArray<Tile>()
    var enrageNewPools = emptyArray<Tile>()
    var nearestOutsideTile = Tile.Nil
    var exploDestination = Tile.Nil
    val attackTimer = Timer(2400)
    val enrageWalkTimeout = Timer(600)
    var hitSplats = intArrayOf()

    //Eggs
    var eggs = emptyArray<Npc>()

    private val spiders = arrayOf(ARAXXOR, "Acidic Araxyte", MIRROR, EXPLODING, "Egg")

    override fun onStart() {
        super.onStart()

        explodingTimer.stop()
        drippingTimer.stop()
        harvestTimer.stop()
        PowDispatchers.Script.launch {
            EventFlows.ticks().collect { onTick(it) }
        }
        PowDispatchers.Script.launch {
            EventFlows.npcAnimationChanges().collectLatest { onNpcAnimation(it) }
        }
        PowDispatchers.Script.launch {
            EventFlows.messages().collectLatest { onMessage(it) }
        }
        PowDispatchers.Script.launch {
            EventFlows.projectileDestinationChanges().collectLatest { onProjectile(it) }
        }
        //Dud to make sure we have some npc's on first run
        onTick(TickEvent())
    }

    override fun canBreak(): Boolean {
        return !araxxor.valid()
    }

    private fun onProjectile(e: ProjectileDestinationChangedEvent) {
        if (e.target() == Actor.Nil) {
            logger.info(
                "Found projectile destination change, destination = ${e.destination()}, distance=${
                    e.destination().distance()
                } on tick=${ticks} cycleEnd=${e.cycleEnd}, localX=${
                    e.projectile.tile().localX()
                }, localY=${e.projectile.tile().localY()}"
            )
            puke.add(e.projectile)
        }
    }

    var myNextAttackTick = -1
    var lastAraxAnimation = -1
    var myTickPositions = mutableMapOf<Int, Tile>()
    val myLastTickPosition: Tile
        get() = myTickPositions[ticks - 1] ?: me.trueTile()
    var attacks = 0
    val explodingTimer = Timer(2400)
    val drippingTimer = Timer(6000)
    val harvestTimer = Timer(10000)

    private fun onNpcAnimation(e: NpcAnimationChangedEvent) {
        if (e.animation == ENRAGED_ATTACK_ANIMATION) {
            attackTimer.reset()
            enrageNewPools = predictAcidPools(araxxorCenter, myLastTickPosition)
        } else if (e.animation == ARAXXOR_DEATH_ANIM) {
            logger.info("Araxxor died, waiting for loot!")
            harvestTimer.reset()
            kills++
        } else if (e.npc.name == ARAXXOR) {
            enrageNewPools = emptyArray()
        }
        if (e.animation == TOXIC_ATTACK_ANIM) {
            attackTimer.reset()
            drippingTimer.reset()
            logger.info("Dripping for ${drippingTimer.getRemainderString()}...")
        } else if (e.animation == HADUKEN_ATTACK_ANIM) {
            attackTimer.reset()
            val orientation = Data.AraxxorOrientations.from(e.npc.orientation())
            val my = me.trueTile()
            var safeTile = orientation.hadukenTile(my, araxxorCenter)
            if (safeTile in unsafeTiles) {
                safeTile = findSafestTile(safeTile.surroundingTiles(true).toTypedArray(), unsafeTiles, my, flags)
            }
            logger.info("Haduken attack, step aside!")
            Movement.stepNoConfirm(safeTile)
        }
        if (e.npc.name in spiders) {
            logger.info("Found animation for ${e.npc.name} animation=${e.animation} on tick=${ticks}")
            if (e.npc.name == ARAXXOR) {
                lastAraxAnimation = e.animation
                if (e.animation in ARAXXOR_ATTACK_ANIMS) {
                    attacks++
                    attackTimer.reset()
                }
            }
        }
    }


    private fun onTick(e: TickEvent) {
        val cycle = Game.cycle()
        puke.removeIf {
            cycle > it.cycleEnd
        }
        logger.info("Active puke=${puke.size}, cycle=${cycle}")
        val npcs = Npcs.stream().name(*spiders).toList()
        araxxor = npcs.firstOrNull { it.name == ARAXXOR } ?: Npc.Nil

        if (araxxor.valid()) {
            val a = araxxor.trueTile()
            val hits = araxxor.hitsplatCycles()
            if (hitSplats.isNotEmpty()) {
                if (hits[0] > hitSplats[0]) {
                    val anim = me.animation()
                    if (anim != -1 && anim != lastAraxAnimation) {
                        myNextAttackTick = ticks + 4
                    }
                }
                logger.info("Hits=${hits.joinToString()}, oldHits=${hitSplats.joinToString()}")
                hitSplats.zip(hits).forEachIndexed { i, hit ->
                    logger.info("$i: $hit")
                }
            }
            hitSplats = hits
            araxxorRect = Rectangle(a.x, a.y, SIZE, SIZE)
            araxxorCenter = araxxorRect.center().tile()
            if (araxxor.healthPercent() <= 25) {
                enrage = true
            }
        } else {
            enrage = false
        }
        val newEggs = npcs.filter { it.name == "Egg" && it.actions.contains("Attack") }
        val eggSize = newEggs.size
        if (eggSize > 0) {
            val y = newEggs.sumOf { it.tile().y } / eggSize
            val firstGroup = newEggs.filter { it.y() < y }
            val secondGroup = newEggs.filter { it.y() > y }
            logger.info("Found ${newEggs.size} eggs, first group has ${firstGroup.size}, second group has ${secondGroup.size}, y=${y}")
            eggs =
                firstGroup.sortedByDescending { it.x() }.toTypedArray() + secondGroup.sortedBy { it.x() }.toTypedArray()
        }
        mirror = npcs.firstOrNull { it.name == MIRROR } ?: Npc.Nil
        acidic = npcs.firstOrNull { it.name == ACIDIC } ?: Npc.Nil
        target = determineTarget()
        exploding = npcs.firstOrNull { it.name == EXPLODING } ?: Npc.Nil
        pools = Objects.stream(15, GameObject.Type.INTERACTIVE).name("Acid pool").toList()
        unsafeTiles = pools.map { it.tile } + puke.map { it.destination() } + enrageNewPools
        enrageTiles = araxxorRect.getEnrageQuickWalkTiles()
        borderTiles = araxxorRect.borderingLayer(2)
        val myTrue = me.trueTile()
        myTickPositions[ticks] = myTrue
        myTickPositions.filterKeys { it <= ticks - 10 }.keys.forEach {
            myTickPositions.remove(it)
        }
        nearestOutsideTile = borderTiles.minByOrNull { it.distanceTo(myTrue) } ?: myTrue
        flags = Movement.collisionMap(myTrue.floor).flags()
    }

    private fun determineTarget(): Npc {
        return if (enrage) araxxor else if (!mirror.dead()) mirror else if (killGreen && !acidic.dead() && acidic.inViewport()) acidic else araxxor
    }

    private fun Point.tile() = Tile(x, y, 0)

    override fun createPainter(): KrulPaint<*> = AraxxorPainter(this)


    override val rootComponent: TreeComponent<*> = IsKilling(this)

    fun turnCamera() {
        val min = 75
        val max = 115
        var yaw = Random.nextInt(min, max)
        if (Camera.yaw() !in min..max) {
            Camera.pitch(true)
            Camera.angle(yaw, 1)
        }
    }

    fun canKillAgain(): Boolean {
        val foodCount = Food.entries.sumOf { it.getInventoryCount(false) } - missingHP() / 20
        val prayPots = Potion.PRAYER.doses()
        val restorePots = Potion.SUPER_RESTORE.doses()
        val prayRestore = Potion.PRAYER.getRestore() * (prayPots + restorePots)
        logger.info("Can kill again: food=${foodCount}, minFood=${minFood} potRestore=${prayPots}, restorePots=${restorePots}, prayRestore=${prayRestore}")
        return foodCount >= minFood && prayRestore + Skills.level(Skill.Prayer) > 120
    }

    fun resetInside() {
        inside = false
        araxTeleport.executed = false
        enrage = false
    }

    fun resetBanking() {
        banking = false
        bankTeleport.executed = false
    }

    @Subscribe
    fun onCheckBoxEvent(e: PaintCheckboxChangedEvent) {
        if (e.checkboxId == "paintDebug") {
            debugPaint = e.checked
        }
    }

    fun onMessage(e: MessageEvent) {
        if (e.message == "Oh dear, you are dead!") {
            deaths++
        }
    }

    fun aboutToAttack() = attackTimer.getRemainder() <= 600 || araxxor.animation() == ENRAGED_ATTACK_ANIMATION
}

fun main() {
    Araxxor().startScript("localhost", "GIM", false)
}
