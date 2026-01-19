package org.powbot.krulvis.giantsfoundry.tree.leaf

import org.powbot.api.rt4.Chat
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.walkAndInteract
import org.powbot.krulvis.api.extensions.Utils.sleep
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.krulvis.giantsfoundry.GiantsFoundry
import org.powbot.krulvis.giantsfoundry.data.Action
import org.powbot.mobile.script.ScriptManager

class GetAssignment(script: GiantsFoundry) : Leaf<GiantsFoundry>(script, "Getting new assignment") {

    override fun execute() {
        if (script.lastSword) {
            ScriptManager.stop()
            return
        }
        Action.reset()
        script.stages = emptyArray()
        if (Chat.canContinue()) {
            script.parseResults()
            Chat.clickContinue()
            sleep(1000)
            waitFor { script.hasCommission() || Chat.canContinue() || Chat.chatting() }
        } else if (Chat.chatting()) {
            Chat.completeChat("Yes.")
            waitFor { script.hasCommission() }
        } else {
            if (walkAndInteract(script.kovac(), "Commission")) {
                waitFor { script.hasCommission() }
            }
        }
    }
}