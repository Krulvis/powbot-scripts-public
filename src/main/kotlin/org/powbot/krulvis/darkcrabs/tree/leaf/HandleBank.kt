package org.powbot.krulvis.darkcrabs.tree.leaf

import org.powbot.api.Notifications
import org.powbot.api.rt4.Bank
import org.powbot.api.rt4.Inventory
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.containsOneOf
import org.powbot.krulvis.api.ATContext.emptyExcept
import org.powbot.krulvis.api.ATContext.getCount
import org.powbot.krulvis.api.ATContext.withdrawExact
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.krulvis.api.extensions.items.Item.Companion.HARPOONS
import org.powbot.krulvis.api.extensions.items.container.Container.FISH_BARREL

import org.powbot.krulvis.darkcrabs.DarkCrabs
import org.powbot.krulvis.darkcrabs.Data
import org.powbot.krulvis.darkcrabs.Data.DARK_BAIT
import org.powbot.krulvis.darkcrabs.Data.LOBSTER_POT
import org.powbot.krulvis.darkcrabs.Data.SPIRIT_FLAKES
import org.powbot.mobile.script.ScriptManager

class HandleBank(script: DarkCrabs) : Leaf<DarkCrabs>(script, "Handle Bank") {
    val TOOLS = intArrayOf(*HARPOONS, *FISH_BARREL.ids, 995, SPIRIT_FLAKES, DARK_BAIT, LOBSTER_POT)
    override fun execute() {
        val hasBarrel = FISH_BARREL.hasWith()
        if (hasBarrel && !FISH_BARREL.emptied) {
            FISH_BARREL.empty()
        } else if (!Inventory.emptyExcept(*TOOLS)) {
            script.logger.info("Depositing loot")
            Bank.depositAllExcept(*TOOLS)
            return
        }
        if (!Inventory.containsOneOf(LOBSTER_POT)) {
            script.logger.info("Withdrawing lobster pot")
            if (Bank.withdraw(LOBSTER_POT, 1)) {
                waitFor { Inventory.containsOneOf(LOBSTER_POT) }
            } else if (!Bank.containsOneOf(LOBSTER_POT)) {
                Notifications.showNotification("No lobster pot found.")
                ScriptManager.stop()
            }
            return
        }
        val cost = Data.resourceAreaCost
        val coins = Inventory.getCount(995)
        if (cost > coins) {
            if (Bank.withdraw(995, cost - coins)) {
                waitFor { Inventory.getCount(995) >= cost }
            }
        }
        val emptySpace = emptyInventorySlots()
        val requiredAmount = if (hasBarrel) emptySpace * 2 else emptySpace
        val bait = Inventory.getCount(DARK_BAIT)
        if (requiredAmount != bait) {
            if (Bank.withdrawExact(DARK_BAIT, requiredAmount)) {
                waitFor { Inventory.getCount(DARK_BAIT) == requiredAmount }
            }
        }
        val flakes = Inventory.getCount(SPIRIT_FLAKES)
        if (requiredAmount != flakes) {
            if (Bank.withdrawExact(SPIRIT_FLAKES, requiredAmount)) {
                waitFor { Inventory.getCount(SPIRIT_FLAKES) == requiredAmount }
            }
        }
        script.logger.info("EmptySpace=$emptySpace, RequiredAmount=$requiredAmount, bait=$bait, flakes=$flakes")
        if (flakes == requiredAmount && bait == requiredAmount) {
            Bank.close()
        }

    }

    fun emptyInventorySlots(): Int = 28 - Inventory.getCount(false, *HARPOONS, *FISH_BARREL.ids, 995, LOBSTER_POT)

}
