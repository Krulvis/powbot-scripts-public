package org.powbot.krulvis.thiever.blackjack

import org.powbot.api.Random
import org.powbot.api.rt4.Constants
import org.powbot.api.rt4.Npcs
import org.powbot.api.rt4.Players
import org.powbot.api.rt4.Skills
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.extensions.Utils.sleep

class Blackjack(script: Blackjacking) : Leaf<Blackjacking>(script, "Blackjack") {
    override fun execute() {
        if (Players.local().healthBarVisible())
            return
        val target = Npcs.stream().name(script.target).nearest().firstOrNull()
        var xp = Skills.experience(Constants.SKILLS_THIEVING)
        if (target != null && target.interact("Knock-Out")) {
            sleep(Random.nextInt(650, 800))
//            xp = Skills.experience(Constants.SKILLS_THIEVING)
            if (target.interact("Pickpocket")) {
                if (!Players.local().healthBarVisible()) {
                    sleep(Random.nextInt(750, 900))
                    target.interact("Pickpocket")
                }
                sleep(Random.nextInt(1500, 2000))
            }
        }
    }


}