package org.powbot.krulvis.cerberus.tree.branch

import org.powbot.api.rt4.Npcs
import org.powbot.api.rt4.Prayer
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.extensions.items.Potion
import org.powbot.krulvis.api.script.tree.branch.ShouldConsume
import org.powbot.krulvis.api.script.tree.branch.ShouldConsume.Companion.canConsume
import org.powbot.krulvis.cerberus.Cerberus
import kotlin.random.Random

class AtCerb(script: Cerberus) : Branch<Cerberus>(script, "AtCerb?") {
    override val failedComponent: TreeComponent<Cerberus>
        get() = TODO("Not yet implemented")
    override val successComponent: TreeComponent<Cerberus> = Flinching(script)

    override fun validate(): Boolean {
        script.cerberus = Npcs.stream().name("Cerberus").first()
        return script.cerberus.valid()
    }
}

class Flinching(script: Cerberus) : Branch<Cerberus>(script, "Flinching") {
    override val failedComponent: TreeComponent<Cerberus> = ShouldConsume(script, CanAttack(script))
    override val successComponent: TreeComponent<Cerberus> = ShouldDrinkPotion(script)

    override fun validate(): Boolean {
        return script.flinch
    }
}

class ShouldDrinkPotion(script: Cerberus) : Branch<Cerberus>(script, "AtCerb?") {
    override val failedComponent: TreeComponent<Cerberus>
        get() = TODO("Not yet implemented")
    override val successComponent: TreeComponent<Cerberus> = SimpleLeaf(script, "SipPray") {
        pot!!.drink()
        ShouldConsume.consumeTick = script.ticks
    }

    var pot: Potion? = null
    var nextSip = Random.nextInt(5, 25)
    override fun validate(): Boolean {
        if (!script.canConsume() || Prayer.prayerPoints() >= nextSip) {
            return false
        }
        pot = Potion.getPrayerPotion()
        return pot != null
    }
}

class CanAttack(script: Cerberus) : Branch<Cerberus>(script, "CanAttack?") {
    override val failedComponent: TreeComponent<Cerberus>
        get() = TODO("Not yet implemented")
    override val successComponent: TreeComponent<Cerberus> = SimpleLeaf(script, "AttackCerb") {

    }

    override fun validate(): Boolean {
        if (script.flinch) {
            return !script.cerberus.healthBarVisible()
        }
        return me.interacting() != script.cerberus
    }
}