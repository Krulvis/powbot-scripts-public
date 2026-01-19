package org.powbot.krulvis.miner.tree.branch

import org.powbot.api.Tile
import org.powbot.api.rt4.*
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.ATContext.containsOneOf
import org.powbot.krulvis.api.ATContext.emptyExcept
import org.powbot.krulvis.api.extensions.Utils.long
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.krulvis.api.extensions.items.Item
import org.powbot.krulvis.api.extensions.items.Ore
import org.powbot.krulvis.miner.Data
import org.powbot.krulvis.miner.Data.getInvPaydirt
import org.powbot.krulvis.miner.Data.getMotherlodeCapacity
import org.powbot.krulvis.miner.Data.getMotherlodeCount
import org.powbot.krulvis.miner.Miner
import org.powbot.krulvis.miner.tree.leaf.*

class ShouldFixStrut(script: Miner) : Branch<Miner>(script, "Should fix strut") {

    override fun validate(): Boolean {
        if (!Bank.opened())
            Game.tab(Game.Tab.INVENTORY)
        val hasHammer = Inventory.containsOneOf(Item.HAMMER)
        val brokenCount = script.getBrokenStrutCount()
        script.rockLocations
        return (!Inventory.isFull() || hasHammer)
                && Tile(3746, 5667, 0).distance() <= 10
                && script.isPayDirtStuck()
                && brokenCount >= if (hasHammer) 1 else 2
    }

    override val successComponent: TreeComponent<Miner> = FixStrut(script)
    override val failedComponent: TreeComponent<Miner> = ShouldCastHumidify(script)
}

class ShouldCastHumidify(script: Miner) : Branch<Miner>(script, "Should Humidify") {

    override fun validate(): Boolean {
        return Data.hasEmptyWaterSkin() && !Data.hasWaterSkins() &&
                Magic.LunarSpell.HUMIDIFY.canCast()
    }

    override val successComponent: TreeComponent<Miner> = SimpleLeaf(script, "Cast humidfy") {
        if (Game.tab(Game.Tab.MAGIC)) {
            waitFor { Game.tab() == Game.Tab.MAGIC }
        }
        if (Magic.LunarSpell.HUMIDIFY.cast()) {
            waitFor(long()) { !Data.hasEmptyWaterSkin() }
        }
    }
    override val failedComponent: TreeComponent<Miner> = ShouldBank(script)
}

class ShouldBank(script: Miner) : Branch<Miner>(script, "Should Bank") {

    override fun validate(): Boolean {
        //Some dirty hammer dropping
        val hammer = Inventory.stream().id(Item.HAMMER).findFirst()
        hammer.ifPresent { Game.tab(Game.Tab.INVENTORY) && it.interact("Drop") }

        //Actual should bank
        val full = Inventory.isFull()
        return full || Bank.opened() || DepositBox.opened()
                || ((script.waterskins || Data.hasEmptyWaterSkin()) && !Data.hasWaterSkins())
                || shouldDepositPaydirt()
    }

    fun shouldDepositPaydirt(): Boolean {
        if (script.shouldEmptySack) {
            // Make sure we escape the shouldEmptySack bool true
            return !Inventory.emptyExcept(*Data.TOOLS) && getMotherlodeCount() == 0
        }
        val currentSackCount = getMotherlodeCount()
        val cap = getMotherlodeCapacity()
        // When we have enough pay-dirt to fill the sack, we should deposit it. However, this can only happen when there is space in the sack
        return currentSackCount < cap && getInvPaydirt() + currentSackCount >= cap
    }

    override val successComponent: TreeComponent<Miner> = ShouldDrop(script)
    override val failedComponent: TreeComponent<Miner> = ShouldEmptySack(script)
}

class ShouldEmptySack(script: Miner) : Branch<Miner>(script, "Should empty sack") {

    override fun validate(): Boolean {
        val count = getMotherlodeCount()
        val capacity = getMotherlodeCapacity()
        if (count >= capacity) {
            script.shouldEmptySack = true
        } else if (count == 0 && Inventory.emptyExcept(*Data.TOOLS)) {
            script.shouldEmptySack = false
        }
        return script.shouldEmptySack && script.getSack().isPresent
    }

    override val successComponent: TreeComponent<Miner> = EmptySack(script)
    override val failedComponent: TreeComponent<Miner> = AtSpot(script)
}

class ShouldDrop(script: Miner) : Branch<Miner>(script, "Should Drop") {

    override fun validate(): Boolean {
        return !script.bankOres
    }

    override val successComponent: TreeComponent<Miner> = Drop(script)
    override val failedComponent: TreeComponent<Miner> = HasPayDirt(script)
}


class HasPayDirt(script: Miner) : Branch<Miner>(script, "Has pay-dirt") {

    override fun validate(): Boolean {
        return Inventory.containsOneOf(*Ore.PAY_DIRT.ids)
    }

    override val successComponent: TreeComponent<Miner> = DropPayDirt(script)
    override val failedComponent: TreeComponent<Miner> = HasSandstone(script)
}

class HasSandstone(script: Miner) : Branch<Miner>(script, "Has sandstone") {

    override fun validate(): Boolean {
        return Inventory.containsOneOf(*Ore.SANDSTONE.ids) &&
                Objects.stream(50).type(GameObject.Type.INTERACTIVE).name("Grinder").action("Deposit")
                    .isNotEmpty()
    }

    override val successComponent: TreeComponent<Miner> = DropSandstone(script)
    override val failedComponent: TreeComponent<Miner> = IsBankOpen(script)
}

class IsBankOpen(script: Miner) : Branch<Miner>(script, "Is Bank open") {

    override fun validate(): Boolean {
        return Bank.opened() || DepositBox.opened()
    }

    override val successComponent: TreeComponent<Miner> = HandleBank(script)
    override val failedComponent: TreeComponent<Miner> = OpenBank(script)
}