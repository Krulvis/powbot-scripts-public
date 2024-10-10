package org.powbot.krulvis.mta.tree.branches

import org.powbot.api.rt4.Inventory
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.krulvis.mta.MTA
import org.powbot.krulvis.mta.rooms.GraveyardRoom
import org.powbot.krulvis.mta.rooms.GraveyardRoom.getEdible
import org.powbot.krulvis.mta.rooms.GraveyardRoom.getFruitCount
import org.powbot.krulvis.mta.tree.leafs.DepositBones
import org.powbot.krulvis.mta.tree.leafs.TakeBones


class CanDeposit(script: MTA) : Branch<MTA>(script, "Can convert bones?") {
    override val failedComponent: TreeComponent<MTA> = CanConvertBones(script)
    override val successComponent: TreeComponent<MTA> = DepositBones(script)

    override fun validate(): Boolean {
        return Inventory.isFull() && getEdible().valid()
    }
}

class CanConvertBones(script: MTA) : Branch<MTA>(script, "Can convert bones?") {
    override val failedComponent: TreeComponent<MTA> = TakeBones(script)
    override val successComponent: TreeComponent<MTA> = SimpleLeaf(script, "Converting bones") {
        if (GraveyardRoom.getSpell().cast()) {
            waitFor { GraveyardRoom.getBoneItems().isEmpty() }
        }
    }

    override fun validate(): Boolean {
        val bones = GraveyardRoom.getBoneItems()
        val space = bones.count() + Inventory.emptySlotCount()
        return bones.getFruitCount() >= space
    }
}

