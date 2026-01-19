package org.powbot.krulvis.cerberus.tree.branch

import org.powbot.api.rt4.Inventory
import org.powbot.api.rt4.Skills
import org.powbot.api.rt4.walking.model.Skill
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.extensions.items.Food
import org.powbot.krulvis.api.extensions.items.Potion
import org.powbot.krulvis.cerberus.Cerberus
import org.powbot.krulvis.cerberus.tree.leaf.WalkToSpawn
import org.powbot.mobile.script.ScriptManager

class ShouldBank(script: Cerberus) : Branch<Cerberus>(script, "ShouldBank?") {
    override val failedComponent: TreeComponent<Cerberus> = WalkToSpawn(script)
    override val successComponent: TreeComponent<Cerberus> = SimpleLeaf(script, "Teleport to bank") {
        if (script.bankTeleport.teleport!!.execute()) {
            ScriptManager.stop()
        }
    }

    override fun validate(): Boolean {
        return Inventory.isFull() && !Food.hasFood() || (Skills.level(Skill.Prayer) == 0 && !Potion.PRAYER.hasWith())
    }
}