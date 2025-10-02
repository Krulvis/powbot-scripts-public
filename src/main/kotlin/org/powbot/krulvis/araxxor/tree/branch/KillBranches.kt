package org.powbot.krulvis.araxxor.tree.branch

import org.powbot.api.rt4.GameObject
import org.powbot.api.rt4.Movement
import org.powbot.api.rt4.Objects
import org.powbot.api.rt4.Prayer
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.ATContext.currentHP
import org.powbot.krulvis.api.ATContext.dead
import org.powbot.krulvis.api.ATContext.distanceToDest
import org.powbot.krulvis.api.ATContext.stepNoConfirm
import org.powbot.krulvis.api.extensions.ResurrectSpell
import org.powbot.krulvis.api.extensions.Timer
import org.powbot.krulvis.api.extensions.Utils.sleep
import org.powbot.krulvis.api.extensions.items.Food
import org.powbot.krulvis.api.extensions.requirements.EquipmentRequirement
import org.powbot.krulvis.api.script.tree.branch.ShouldConsume
import org.powbot.krulvis.araxxor.Araxxor
import org.powbot.krulvis.araxxor.Data.ACIDIC
import org.powbot.krulvis.araxxor.Data.MIRROR
import org.powbot.krulvis.araxxor.tree.leaf.KillTarget

class IsKilling(script: Araxxor) : Branch<Araxxor>(script, "IsKilling?") {
    override val failedComponent: TreeComponent<Araxxor> = HasGravestone(script)
    override val successComponent: TreeComponent<Araxxor> = ShouldConsume(script, ShouldEscape(script), false)

    override fun validate(): Boolean {
        if (script.araxxor.valid()) {
            script.inside = true
            return !script.araxxor.actions.contains("Harvest") && !script.araxxor.dead()
        }
        return false
    }
}

class ShouldEscape(script: Araxxor) : Branch<Araxxor>(script, "ShouldEscape?") {
    override val failedComponent: TreeComponent<Araxxor> = ShouldPray(script)
    override val successComponent: TreeComponent<Araxxor> = SimpleLeaf(script, "Escape") {
        script.inside = false
        script.banking = true
        script.bankTeleport.execute()
    }

    override fun validate(): Boolean {
        return currentHP() < 25 && !Food.hasFood()
    }
}

class ShouldPray(script: Araxxor) : Branch<Araxxor>(script, "ShouldPray?") {
    override val failedComponent: TreeComponent<Araxxor> = ShouldEquipGear(script)
    override val successComponent: TreeComponent<Araxxor> = SimpleLeaf(script, "Praying") {
        if (script.offensive !in active) {
            Prayer.prayer(script.offensive, true)
            sleep(30)
        }
        if (script.defensive !in active) {
            Prayer.prayer(script.defensive, true)
        }
        prayTimer.reset()
    }

    private val prayTimer = Timer(300)

    private var active = emptyArray<Prayer.Effect>()
    override fun validate(): Boolean {
        if (!prayTimer.isFinished()) return false
        active = Prayer.activePrayers()
        script.defensive = Prayer.Effect.PROTECT_FROM_MELEE
        return script.offensive !in active || script.defensive !in active
    }
}


class ShouldEquipGear(script: Araxxor) : Branch<Araxxor>(script, "ShouldEquipGear?") {
    override val failedComponent: TreeComponent<Araxxor> = ShouldReposition(script)
    override val successComponent: TreeComponent<Araxxor> = SimpleLeaf(script, "Equipping gear") {
        missingGear.forEach { it.item.equip(false) }
        equipTick = script.ticks
    }

    private var equipTick = 0
    private var missingGear = emptyList<EquipmentRequirement>()
    override fun validate(): Boolean {
        if (equipTick + 1 > script.ticks) return false
        val requiredGear = if (script.target.name == MIRROR || script.target.name == ACIDIC) {
            script.smallGear
        } else if (script.specWeapon.canSpecial()) {
            script.specEquipment
        } else {
            script.equipment
        }
        missingGear = requiredGear.filter { !it.meets() }

        return missingGear.isNotEmpty()
    }
}

class ShouldCastResurrect(script: Araxxor) : Branch<Araxxor>(script, "ShouldResurrect?") {
    override val failedComponent: TreeComponent<Araxxor> = KillTarget(script)
    override val successComponent: TreeComponent<Araxxor> = SimpleLeaf(script, "Resurrecting") {
        val spell = script.resurrectSpell.spell
        if (!script.araxxor.valid()) {
            val escape = Objects.stream(15, GameObject.Type.INTERACTIVE).name("Squeeze-through").nearest().first()
            val centerTile = escape.tile().derive(-10, 0)
            if (centerTile.distanceToDest() > 2) {
                Movement.stepNoConfirm(centerTile)
            }
        }
        val casted = spell.cast()
        script.logger.info("Casted spell = $casted")
        if (casted) {
            ResurrectSpell.resetTimer()
            script.logger.info("Remaining time=${ResurrectSpell.resurrectTimer.getRemainder()}")
        }
    }

    override fun validate(): Boolean {
        return script.resurrectSpell.canCast()
    }
}

