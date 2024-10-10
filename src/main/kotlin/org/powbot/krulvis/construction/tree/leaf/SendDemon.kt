package org.powbot.krulvis.construction.tree.leaf

import org.powbot.api.Random
import org.powbot.api.rt4.Components
import org.powbot.api.rt4.Npcs
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.extensions.House
import org.powbot.krulvis.api.extensions.Timer
import org.powbot.krulvis.api.extensions.Utils.sleep
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.krulvis.construction.Construction
import org.powbot.mobile.script.ScriptManager

class SendDemon(script: Construction) : Leaf<Construction>(script, "Sending demon") {

    fun demon() = Npcs.stream().name("Demon butler").firstOrNull()


    fun chatOption() = Components.stream(219, 1).text("Okay, here's 10,000 coins.").firstOrNull()
        ?: Components.stream(219).text("Un-note: 26 x Teak plank", "Yes").firstOrNull()
        ?: Components.stream(231).text("Tap here to continue").firstOrNull()


    fun handleChat() {
        var chat = chatOption()
        val t = Timer(5000)
        while (!t.isFinished() && chat != null) {
            chat.click()
            sleep(Random.nextInt(600, 1200))
            chat = chatOption()
        }
    }

    override fun execute() {
        if (script.plank.getInventoryCount(true) <= 25) {
            script.logger.info("Out of planks stopping script")
            ScriptManager.stop()
            return
        }
        if (chatOption() == null) {
            var demon = demon()
            if ((demon?.distance()?.toInt() ?: 99) >= 4) {
                House.callButler()
                waitFor { (demon()?.distance()?.toInt() ?: 99) <= 4 }
            }
            demon = demon()
            if ((demon?.distance()?.toInt() ?: 99) <= 4) {
                if (demon?.interact("Talk-to") == true) {
                    waitFor { chatOption() != null }
                }
            }
        }
        if (script.hasMats()) {
            return
        }
        if (chatOption() != null) {
            handleChat()
            if (waitFor { demon() == null }) {
                script.logger.info("Waiting for demon to return....")
                val returned = waitFor(10000) { demon() != null }
                script.logger.info("Demon returned =$returned")
            }
        }
    }
}