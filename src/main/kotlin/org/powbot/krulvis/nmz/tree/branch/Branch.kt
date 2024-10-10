package org.powbot.krulvis.nmz.tree.branch

import org.powbot.api.rt4.Inventory
import org.powbot.api.rt4.Item
import org.powbot.api.rt4.Prayer
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.ATContext
import org.powbot.krulvis.api.extensions.items.Potion
import org.powbot.krulvis.api.script.tree.branch.ShouldSipPotion
import org.powbot.krulvis.api.extensions.Timer
import org.powbot.krulvis.api.extensions.Utils
import org.powbot.krulvis.nmz.NightmareZone
import org.powbot.krulvis.nmz.tree.leaf.EnterDream
import org.powbot.krulvis.nmz.tree.leaf.SetupInventory
import org.powbot.mobile.script.ScriptManager
import kotlin.random.Random

class OutsideNMZ(script: NightmareZone) : Branch<NightmareZone>(script, "Is outside nmz") {
    override val failedComponent: TreeComponent<NightmareZone> = ShouldSipPotion(script, ShouldPray(script))
    override val successComponent: TreeComponent<NightmareZone> = HasCorrectInventory(script)

    override fun validate(): Boolean {
        val outside = script.outsideNMZ()
        if (outside && script.stopOutside) {
            script.logger.info("Stopping because outside of NMZ")
            ScriptManager.stop()
        }
        return outside
    }
}

class HasCorrectInventory(script: NightmareZone) : Branch<NightmareZone>(script, "Has correct inventory") {
    override val failedComponent: TreeComponent<NightmareZone> = SetupInventory(script)
    override val successComponent: TreeComponent<NightmareZone> = EnterDream(script)

    override fun validate(): Boolean {
        return true
//        val inv = Inventory.items()
//        return script.inventoryItems.all { wi -> wi.value == inv.filter { it.id == wi.key }.sumOf { it.stack } }
    }
}

class ShouldPray(script: NightmareZone) : Branch<NightmareZone>(script, "Should pray") {
    override val failedComponent: TreeComponent<NightmareZone> = ShouldDrinkOverload(script)
    override val successComponent: TreeComponent<NightmareZone> = SimpleLeaf(script, "Flick pray") {
        Prayer.quickPrayer(true)
    }

    override fun validate(): Boolean {
        return Potion.PRAYER.hasWith() && !Prayer.quickPrayer()
    }
}

class ShouldDrinkOverload(script: NightmareZone) : Branch<NightmareZone>(script, "Should Overload") {
    override val failedComponent: TreeComponent<NightmareZone> = ShouldRockCake(script)
    override val successComponent: TreeComponent<NightmareZone> = SimpleLeaf(script, "Drink overload") {
        Potion.OVERLOAD.drink()
    }

    override fun validate(): Boolean {
        return Potion.OVERLOAD.hasWith() && Potion.OVERLOAD.needsRestore(90)
    }
}

class ShouldRockCake(script: NightmareZone) : Branch<NightmareZone>(script, "Should RockCake") {
    override val failedComponent: TreeComponent<NightmareZone> = ShouldFlickRapidHeal(script)
    override val successComponent: TreeComponent<NightmareZone> = SimpleLeaf(script, "Eating Stone") {
        val action = if (ATContext.currentHP() in 10 downTo 3) "Eat" else "Guzzle"
        rockCake?.interact(action)
    }

    var rockCake: Item? = null

    override fun validate(): Boolean {
        rockCake = Inventory.stream().id(7510).firstOrNull()
        val hp = ATContext.currentHP()
        return rockCake != null && hp > 1 && Potion.OVERLOAD.lastSip.getElapsedTime() > 10000
    }
}

class ShouldFlickRapidHeal(script: NightmareZone) : Branch<NightmareZone>(script, "Should Flick Rapid Heal") {
    override val failedComponent: TreeComponent<NightmareZone> = ShouldDrinkAbsorption(script)
    override val successComponent: TreeComponent<NightmareZone> = SimpleLeaf(script, "Flick RapidHeal") {
        Prayer.prayer(Prayer.Effect.RAPID_HEAL, true)
        Utils.sleep(Random.nextInt(50, 150))
        Prayer.prayer(Prayer.Effect.RAPID_HEAL, false)
        script.nextFlick = Timer(Random.nextInt(10, 40) * 1000)
    }

    override fun validate(): Boolean {
        return ATContext.currentHP() <= 10 && Prayer.prayerPoints() > 0 && script.nextFlick.isFinished()
    }
}

class ShouldDrinkAbsorption(script: NightmareZone) : Branch<NightmareZone>(script, "Should Drink Absorption") {
    override val failedComponent: TreeComponent<NightmareZone> = SimpleLeaf(script, "Chill") {}
    override val successComponent: TreeComponent<NightmareZone> = SimpleLeaf(script, "Drink absorption") {
        Potion.ABSORPTION.drink()
    }

    override fun validate(): Boolean {
        return Potion.getAbsorptionRemainder() <= 50 && Potion.ABSORPTION.hasWith()
    }
}