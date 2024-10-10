package org.powbot.krulvis.thiever.blackjack

import org.powbot.api.Random
import org.powbot.api.rt4.Combat
import org.powbot.api.rt4.Npcs
import org.powbot.api.rt4.Objects
import org.powbot.api.rt4.Players
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.extensions.items.Food
import org.powbot.krulvis.api.extensions.Utils.sleep
import org.powbot.krulvis.api.extensions.Utils.waitFor

class ShouldEat(script: Blackjacking) : Branch<Blackjacking>(script, "Should eat?") {
    override val failedComponent: TreeComponent<Blackjacking> = ShouldEscape(script)
    override val successComponent: TreeComponent<Blackjacking> = SimpleLeaf(script, "Eating") {
        script.eatFood()
    }

    var food: Food? = null

    override fun validate(): Boolean {
        if (script.stopping) return false
        val food = Food.getFirstFood()
        val hp = Combat.health()
        val maxHp = Combat.maxHealth()

        return hp <= 20 || (food?.healing ?: 0) < maxHp - hp
    }

}

class ShouldEscape(script: Blackjacking) : Branch<Blackjacking>(script, "Should Escape?") {
    override val failedComponent: TreeComponent<Blackjacking> = ShouldClimbDown(script)
    override val successComponent: TreeComponent<Blackjacking> = SimpleLeaf(script, "Escaping") {
        val ladder = Objects.stream().at(script.ladderTile).name("Ladder").firstOrNull()
        if (ladder?.interact("Climb-up") == true) {
            if (waitFor(5000) { Players.local().tile().floor == 1 })
                sleep(Random.nextInt(2500, 3000))
        }
    }

    override fun validate(): Boolean {
        return script.stopping || (Players.local().healthBarVisible() && Npcs.stream().name(script.target)
            .interactingWithMe()
            .firstOrNull() != null)
    }
}

class ShouldClimbDown(script: Blackjacking) : Branch<Blackjacking>(script, "Should climb down?") {
    override val failedComponent: TreeComponent<Blackjacking> = Blackjack(script)
    override val successComponent: TreeComponent<Blackjacking> = SimpleLeaf(script, "Climbing down") {
        if (script.stopping) {

        }
        val ladder = Objects.stream().at(script.ladderTile.derive(0, 0, 1)).name("Ladder").firstOrNull()
        if (ladder?.interact("Climb-down") == true) {
            waitFor(5000) { Players.local().tile().floor == 0 }
        }
    }

    override fun validate(): Boolean {
        return Players.local().tile().floor == 1
    }
}
