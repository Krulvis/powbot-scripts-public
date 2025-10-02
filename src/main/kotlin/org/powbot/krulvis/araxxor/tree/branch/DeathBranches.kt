package org.powbot.krulvis.araxxor.tree.branch

import org.powbot.api.requirement.Requirement
import org.powbot.api.rt4.Movement
import org.powbot.api.rt4.Npc
import org.powbot.api.rt4.Npcs
import org.powbot.api.rt4.Prayer
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.ATContext.stepNoConfirm
import org.powbot.krulvis.api.ATContext.walkAndInteract
import org.powbot.krulvis.api.extensions.Death
import org.powbot.krulvis.api.extensions.Utils.long
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.krulvis.api.extensions.requirements.EquipmentRequirement
import org.powbot.krulvis.api.extensions.requirements.ItemRequirement
import org.powbot.krulvis.araxxor.Araxxor
import org.powbot.krulvis.araxxor.Data.spiderDungeonArea
import org.powbot.krulvis.araxxor.Data.webTunnelTile

class HasGravestone(script: Araxxor) : Branch<Araxxor>(script, "HasGravestone?") {
    override val failedComponent: TreeComponent<Araxxor> = CanLoot(script)
    override val successComponent: TreeComponent<Araxxor> = NearGravestone(script)

    override fun validate(): Boolean {
        return Death.gravestoneActive()
    }
}

class NearGravestone(script: Araxxor) : Branch<Araxxor>(script, "NearGravestone?") {
    override val failedComponent: TreeComponent<Araxxor> = InSpiderCave(script)
    override val successComponent: TreeComponent<Araxxor> = SimpleLeaf(script, "LootGravestone") {
        if (gravestone.distance() > 12) {
            Movement.stepNoConfirm(gravestone.tile())
        } else if (walkAndInteract(gravestone, "Loot")) {
            waitFor(long()) { !Death.gravestoneActive() }
        }
    }

    var gravestone = Npc.Nil

    override fun validate(): Boolean {
        gravestone = Npcs.stream().nameContains("Grave").action("Loot").first()
        return gravestone.distance() < 30
    }
}

class InSpiderCave(script: Araxxor) : Branch<Araxxor>(script, "InSpiderCave?") {
    override val failedComponent: TreeComponent<Araxxor> = HasDarkmeyerTeleport(script)
    override val successComponent: TreeComponent<Araxxor> = SimpleLeaf(script, "WalkToGravestone") {
        Prayer.prayer(Prayer.Effect.PROTECT_FROM_MELEE, true)
        Movement.stepNoConfirm(webTunnelTile)
    }

    override fun validate(): Boolean {
        return spiderDungeonArea.contains(me)
    }
}

class HasDarkmeyerTeleport(script: Araxxor) : Branch<Araxxor>(script, "HasTeleport?") {
    override val failedComponent: TreeComponent<Araxxor> =
        BankOpen(script, SimpleLeaf(script, "TakeAraxxTeleport") {
            val itemRequirements = script.araxTeleport.teleport!!.requirements.filterIsInstance<ItemRequirement>()
            itemRequirements.all { it.withdraw(true) }
            itemRequirements.filterIsInstance<EquipmentRequirement>().forEach { it.withdrawAndEquip(true) }
        })
    override val successComponent: TreeComponent<Araxxor> = OutsideDungeon(script)
    var requirements: List<Requirement> = emptyList()
    override fun validate(): Boolean {
        requirements = script.araxTeleport.teleport?.requirements ?: emptyList()

        requirements.forEach {
            script.logger.info("$it, meets=${it.meets()}")
        }
        return requirements.all { it.meets() }
    }
}