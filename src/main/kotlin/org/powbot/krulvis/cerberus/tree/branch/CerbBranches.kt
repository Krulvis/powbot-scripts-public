package org.powbot.krulvis.cerberus.tree.branch

import org.powbot.api.rt4.Inventory
import org.powbot.api.rt4.Item
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.ATContext
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.extensions.Utils.sleep
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.krulvis.api.extensions.items.Weapon
import org.powbot.krulvis.api.extensions.requirements.EquipmentRequirement
import org.powbot.krulvis.api.script.tree.branch.CanLoot
import org.powbot.krulvis.api.script.tree.branch.ShouldConsume
import org.powbot.krulvis.cerberus.Cerberus
import org.powbot.krulvis.cerberus.tree.leaf.Attack
import org.powbot.krulvis.cerberus.tree.leaf.StandUnder
import org.powbot.krulvis.cerberus.tree.leaf.WalkToSpawn

class ShouldRockCake(script: Cerberus) : Branch<Cerberus>(script, "Should RockCake") {
    override val failedComponent: TreeComponent<Cerberus> = AtCerb(script)
    override val successComponent: TreeComponent<Cerberus> = SimpleLeaf(script, "Eating Stone") {
        val action = if (ATContext.currentHP() in 10 downTo 3) "Eat" else "Guzzle"
        rockCake?.interact(action)
    }

    var rockCake: Item? = null

    override fun validate(): Boolean {
        rockCake = Inventory.stream().id(7510).firstOrNull()
        val hp = ATContext.currentHP()
        return rockCake != null && hp > 1
    }
}

class AtCerb(script: Cerberus) : Branch<Cerberus>(script, "AtCerb?") {
    override val failedComponent: TreeComponent<Cerberus> = ShouldSetCamera(script)
    override val successComponent: TreeComponent<Cerberus> = CerbAlive(script)

    override fun validate(): Boolean {
        return script.door.valid()
    }
}

class CerbAlive(script: Cerberus) : Branch<Cerberus>(script, "CerbAlive?") {
    override val failedComponent: TreeComponent<Cerberus> = CanLoot(script, WalkToSpawn(script))
    override val successComponent: TreeComponent<Cerberus> = Flinching(script)
    override fun validate(): Boolean {
        return script.cerberus.valid()
    }
}

class Flinching(script: Cerberus) : Branch<Cerberus>(script, "Flinching") {
    override val failedComponent: TreeComponent<Cerberus> = ShouldConsume(script, CanAttack(script))
    override val successComponent: TreeComponent<Cerberus> = StandingUnderCerb(script)

    override fun validate(): Boolean {
        return script.flinch
    }
}

class StandingUnderCerb(script: Cerberus) : Branch<Cerberus>(script, "StandingUnderCerb?") {
    override val failedComponent: TreeComponent<Cerberus> = StandUnder(script)
    override val successComponent: TreeComponent<Cerberus> = CanAttack(script)

    override fun validate(): Boolean {
        return script.centerTile.distanceTo(me.trueTile()) < 2
    }
}

class CanAttack(script: Cerberus) : Branch<Cerberus>(script, "CanAttack?") {
    override val failedComponent: TreeComponent<Cerberus> = ShouldSwitchGear(script)
    override val successComponent: TreeComponent<Cerberus> = Attack(script)

    override fun validate(): Boolean {
        if (!script.cerberus.actions.contains("Attack")) return false
        if (script.flinch) {
            return script.flinchTimer.isFinished()
        }
        return me.interacting() != script.cerberus
    }
}

class ShouldSwitchGear(script: Cerberus) : Branch<Cerberus>(script, "ShouldSwitchGear?") {
    override val failedComponent: TreeComponent<Cerberus> = SimpleLeaf(script, "Wait for flinchtimer") {
        sleep(50)
    }
    override val successComponent: TreeComponent<Cerberus> = SimpleLeaf(script, "SwitchGear") {
        missing.forEach { it.item.equip(false) }
        if (script.hasAttackOption())
            script.clickDoor()
        waitFor { missing.all { it.meets() } }
    }

    var missing: List<EquipmentRequirement> = emptyList()
    override fun validate(): Boolean {
        val required =
            if (script.specWeapon.canSpecial() && (script.specWeapon != Weapon.DRAGON_WARHAMMER || script.cerbHP > 50)) {
                script.equipmentSpecial
            } else {
                script.equipment
            }
        missing = required.filter { !it.meets() }
        return missing.isNotEmpty()
    }
}