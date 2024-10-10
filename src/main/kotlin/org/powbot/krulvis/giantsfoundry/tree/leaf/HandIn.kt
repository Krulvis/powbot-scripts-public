package org.powbot.krulvis.giantsfoundry.tree.leaf

import org.powbot.api.rt4.Chat
import org.powbot.api.rt4.Widgets
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.walkAndInteract
import org.powbot.krulvis.api.extensions.Utils.sleep
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.krulvis.giantsfoundry.GiantsFoundry

class HandIn(script: GiantsFoundry) : Leaf<GiantsFoundry>(script, "Handing in") {

    fun widget() = Widgets.widget(231)

    override fun execute() {
        if (Chat.canContinue()) {
            script.parseResults()
            Chat.clickContinue()
            sleep(1500)
            waitFor { Chat.canContinue() }
        } else {
            walkAndInteract(script.kovac(), "Hand-in")
            waitFor { Chat.chatting() }
        }
    }
}