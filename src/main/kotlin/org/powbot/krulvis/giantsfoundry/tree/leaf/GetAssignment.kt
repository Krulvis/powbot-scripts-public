package org.powbot.krulvis.giantsfoundry.tree.leaf

import org.powbot.api.rt4.Chat
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.walkAndInteract
import org.powbot.krulvis.api.extensions.Utils.sleep
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.krulvis.giantsfoundry.GiantsFoundry

class GetAssignment(script: GiantsFoundry) : Leaf<GiantsFoundry>(script, "Getting new assignment") {

    //1021 0 -> 512
    //512
    //1000000000

    //3429 128 -> 154

    //128       154 (light & flat)
    //10000000 10011010
    override fun execute() {
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