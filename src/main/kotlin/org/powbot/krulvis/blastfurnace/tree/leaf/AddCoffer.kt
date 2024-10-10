package org.powbot.krulvis.blastfurnace.tree.leaf

import org.powbot.api.Input
import org.powbot.api.Notifications
import org.powbot.api.Tile
import org.powbot.api.rt4.Bank
import org.powbot.api.rt4.Chat
import org.powbot.api.rt4.Inventory
import org.powbot.api.rt4.Objects
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.emptyExcept
import org.powbot.krulvis.api.ATContext.getCount
import org.powbot.krulvis.api.ATContext.walkAndInteract
import org.powbot.krulvis.api.extensions.Utils.long
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.krulvis.blastfurnace.*
import org.powbot.mobile.script.ScriptManager

class AddCoffer(script: BlastFurnace) : Leaf<BlastFurnace>(script, "Adding to coffer") {
    override fun execute() {
        val depositButton = Chat.stream().textContains("Deposit coins.").findFirst()
        if (Inventory.getCount(995) < script.cofferAmount) {
            script.logger.info("Not enough gold yet...")
            if (!Bank.opened()) {
                val chest = Objects.stream().name("Bank chest").findFirst()
                chest.ifPresent { if (walkAndInteract(it, "Use")) waitFor(long()) { Bank.opened() } }
            } else if (!Inventory.emptyExcept(COAL_BAG_CLOSED, ICE_GLOVES, SMITHS_GLOVES, GOLD_GLOVES)) {
                Bank.depositAllExcept(COAL_BAG_CLOSED, ICE_GLOVES, SMITHS_GLOVES, GOLD_GLOVES)
            } else if (Bank.withdraw(995, script.cofferAmount)) {
                waitFor { Inventory.getCount(995) >= script.cofferAmount }
            } else if (Bank.stream().id(995).count(true) <= script.cofferAmount) {
                Notifications.showNotification("Stopped because no more money")
                ScriptManager.stop()
            }
        } else if (depositButton.isPresent) {
            if (depositButton.get().select()) {
                waitFor(long()) { Chat.pendingInput() }
            }
        } else if (Bank.close() && Chat.pendingInput()) {
            script.logger.info("Pending deposit input...")
            Input.sendln("${script.cofferAmount / 1000 + 1}k")
            waitFor { script.cofferCount() > 100 }
        } else {
            val matrix = Tile(1946, 4957, 0).matrix()
            if (script.interact(matrix, "Use")) {
                waitFor(long()) { chatOpen() }
            }
        }

    }

    fun chatOpen(): Boolean = Chat.stream().text("Deposit coins.").isNotEmpty()

}