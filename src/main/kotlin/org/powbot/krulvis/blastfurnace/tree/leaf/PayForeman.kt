package org.powbot.krulvis.blastfurnace.tree.leaf

import org.powbot.api.Notifications
import org.powbot.api.rt4.*
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.emptyExcept
import org.powbot.krulvis.api.ATContext.getCount
import org.powbot.krulvis.api.ATContext.walkAndInteract
import org.powbot.krulvis.api.extensions.Utils.long
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.krulvis.blastfurnace.*
import org.powbot.mobile.script.ScriptManager

class PayForeman(script: BlastFurnace) : Leaf<BlastFurnace>(script, "Pay Foreman") {
    override fun execute() {
        if (Inventory.getCount(995) < 2500) {
            val amount = if (script.cofferCount() < 150) 2500 + script.cofferAmount else 2500
            if (!Bank.opened()) {
                val chest = Objects.stream().name("Bank chest").findFirst()
                chest.ifPresent { if (walkAndInteract(it, "Use")) waitFor(long()) { Bank.opened() } }
            } else if (!Inventory.emptyExcept(COAL_BAG_CLOSED, ICE_GLOVES, SMITHS_GLOVES, GOLD_GLOVES)) {
                Bank.depositAllExcept(COAL_BAG_CLOSED, ICE_GLOVES, SMITHS_GLOVES, GOLD_GLOVES)
            } else if (Bank.withdraw(995, amount)) {
                waitFor { Inventory.getCount(995) >= amount }
            } else if (Bank.stream().id(995).count(true) <= amount) {
                Notifications.showNotification("Stopped because no more money")
                ScriptManager.stop()
            }
        } else if (Chat.stream().textContains("Yes").isNotEmpty()) {
            Chat.stream().textContains("Yes").findFirst().ifPresent {
                val coins = Inventory.getCount(995)
                it.select()
                if (waitFor(long()) { coins > Inventory.getCount(995) }) {
                    script.foremanTimer.reset(10 * 60 * 1000)
                }
            }
        } else if (Bank.close()) {
            val foreman = Npcs.stream().name("Blast Furnace Foreman").findFirst()
            foreman.ifPresent {
                if (walkAndInteract(it, "Pay")) {
                    waitFor(long()) { Chat.stream().textContains("Yes").isNotEmpty() }
                }
            }
        }

    }

}