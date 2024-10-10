package org.powbot.krulvis.blastfurnace.tree.branch

import org.powbot.api.rt4.*
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.ATContext.containsOneOf
import org.powbot.krulvis.api.ATContext.debug
import org.powbot.krulvis.api.extensions.items.Item.Companion.BUCKET_OF_WATER
import org.powbot.krulvis.api.extensions.items.Item.Companion.EMPTY_BUCKET
import org.powbot.krulvis.api.extensions.items.Item.Companion.VIAL
import org.powbot.krulvis.api.extensions.Utils.long
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.krulvis.blastfurnace.BlastFurnace
import org.powbot.krulvis.blastfurnace.tree.leaf.*

class ShouldDrinkPotion(script: BlastFurnace) : Branch<BlastFurnace>(script, "Should take bars") {
    override val successComponent: TreeComponent<BlastFurnace> = DrinkPotion(script)
    override val failedComponent: TreeComponent<BlastFurnace> = HasBarsInDispenser(script)

    override fun validate(): Boolean {
        return script.drinkPotion && Bank.opened()
                && (script.potion.inInventory() || Inventory.containsOneOf(VIAL) || script.potion.needsRestore(60))
    }
}

class HasBarsInDispenser(script: BlastFurnace) : Branch<BlastFurnace>(script, "Has bars in dispenser") {
    override val successComponent: TreeComponent<BlastFurnace> = ShouldCoolDispenser(script)
    override val failedComponent: TreeComponent<BlastFurnace> = ShouldPutOre(script)

    override fun validate(): Boolean {
        return !Inventory.isFull() && script.bar.blastFurnaceCount > 0
                && !Inventory.containsOneOf(
            script.bar.primary.id,
            script.bar.secondary.id
        )
    }
}

class ShouldCoolDispenser(script: BlastFurnace) : Branch<BlastFurnace>(script, "Should cool dispenser") {
    override val successComponent: TreeComponent<BlastFurnace> = CoolDispenser(script)
    override val failedComponent: TreeComponent<BlastFurnace> = ShouldDropBucket(script)

    override fun validate(): Boolean {
        return !script.hasIceGloves() && !script.cooledDispenser()
    }
}

class ShouldDropBucket(script: BlastFurnace) : Branch<BlastFurnace>(script, "Should drop bucket") {
    override val successComponent: TreeComponent<BlastFurnace> = SimpleLeaf(script, "Drop bucket") {
        if (bucket!!.interact("Drop")) {
            waitFor { !validate() }
        }
    }
    override val failedComponent: TreeComponent<BlastFurnace> = TakeBars(script)

    var bucket: Item? = null

    override fun validate(): Boolean {
        bucket = Inventory.stream().id(BUCKET_OF_WATER, EMPTY_BUCKET).firstOrNull()
        return bucket != null
    }
}

class ShouldPutOre(script: BlastFurnace) : Branch<BlastFurnace>(script, "Should Put Ore") {
    override val successComponent: TreeComponent<BlastFurnace> = PutOre(script)
    override val failedComponent: TreeComponent<BlastFurnace> = ShouldWaitAtDispenser(script)

    override fun validate(): Boolean {
        return Inventory.containsOneOf(script.bar.primary.id, script.bar.secondary.id)
    }
}

class ShouldWaitAtDispenser(script: BlastFurnace) : Branch<BlastFurnace>(script, "Should Wait @ Dispenser") {
    override val successComponent: TreeComponent<BlastFurnace> = SimpleLeaf(script, "Waiting for XP drop") {
        val xp = Skills.experience(Constants.SKILLS_SMITHING)
        val dispenserTile = script.dispenserTile.derive(-1, 0)
        val walkTile = if (!script.hasIceGloves()) GroundItems.stream().name("Bucket").firstOrNull()?.tile
            ?: dispenserTile else dispenserTile
        if (walkTile.distance() >= 2) {
            Movement.step(walkTile)
            if (waitFor(long()) { Skills.experience(Constants.SKILLS_SMITHING) > xp && script.bar.blastFurnaceCount >= 27 }) {
                debug("Waited for bars to smelt before equipping ice gloves")
            }
        }
    }
    override val failedComponent: TreeComponent<BlastFurnace> = HandleBank(script)

    override fun validate(): Boolean {
        if (Inventory.containsOneOf(script.bar.id)) {
            script.waitForBars.stop()
        }
        return !Inventory.isFull() && !script.waitForBars.isFinished()
    }
}