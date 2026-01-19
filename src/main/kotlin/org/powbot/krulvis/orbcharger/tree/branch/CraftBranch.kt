package org.powbot.krulvis.orbcharger.tree.branch

import org.powbot.api.Condition.sleep
import org.powbot.api.Production
import org.powbot.api.Random
import org.powbot.api.rt4.Combat
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.extensions.items.Potion
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.krulvis.orbcharger.OrbCrafter
import org.powbot.krulvis.orbcharger.tree.leaf.Charge
import org.powbot.krulvis.orbcharger.tree.leaf.Walk

class AtObelisk(script: OrbCrafter) : Branch<OrbCrafter>(script, "ShouldBank?") {
    override val successComponent: TreeComponent<OrbCrafter> = ShouldDrinkAntipot(script)
    override val failedComponent: TreeComponent<OrbCrafter> = Walk(script)

    override fun validate(): Boolean {
        return script.orb.obeliskTile.distance() <= 2
    }
}

class ShouldDrinkAntipot(script: OrbCrafter) : Branch<OrbCrafter>(script, "ShouldDrinkAntiPoison?") {
    override val successComponent: TreeComponent<OrbCrafter> = SimpleLeaf(script, "Drink antipot") {
        if (Potion.getAntipot()?.drink() == true) {
            waitFor { !Combat.isPoisoned() }
        }
    }
    override val failedComponent: TreeComponent<OrbCrafter> = IsCrafting(script)

    override fun validate(): Boolean {
        return Combat.isPoisoned() && Potion.hasAntipot()
    }
}

class IsCrafting(script: OrbCrafter) : Branch<OrbCrafter>(script, "IsBankOpen?") {
    override val successComponent: TreeComponent<OrbCrafter> =
        SimpleLeaf(script, "Chilling?") { sleep(Random.nextInt(500, 600)) }
    override val failedComponent: TreeComponent<OrbCrafter> = Charge(script)

    override fun validate(): Boolean {
        return !script.fastCharge && !Production.stoppedMaking(script.orb.id)
    }
}