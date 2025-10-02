package org.powbot.krulvis.araxxor.tree.branch

import org.powbot.api.Filter
import org.powbot.api.Notifications
import org.powbot.api.rt4.Bank
import org.powbot.api.rt4.Inventory
import org.powbot.api.rt4.Item
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.ATContext.emptyExcept
import org.powbot.krulvis.api.extensions.BankLocation.Companion.openNearest
import org.powbot.krulvis.api.extensions.Death
import org.powbot.krulvis.api.extensions.Utils.long
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.krulvis.api.extensions.items.Food
import org.powbot.krulvis.api.extensions.items.Potion
import org.powbot.krulvis.api.extensions.items.teleports.ITeleportItem
import org.powbot.krulvis.api.extensions.requirements.EquipmentRequirement
import org.powbot.krulvis.api.extensions.requirements.InventoryRequirement
import org.powbot.krulvis.api.extensions.teleports.TeleportMethod
import org.powbot.krulvis.araxxor.Araxxor
import org.powbot.krulvis.fighter.slayer.Slayer
import org.powbot.mobile.script.ScriptManager

class ShouldBank(script: Araxxor) : Branch<Araxxor>(script, "ShouldBank?") {
    override val failedComponent: TreeComponent<Araxxor> = InLair(script)
    override val successComponent: TreeComponent<Araxxor> = BankOpen(script, ShouldStopScript(script))

    override fun validate(): Boolean {
        if (script.banking) return true
        if (script.inside) {
            if (script.canKillAgain()) {
                script.logger.info("Inside the lair and can kill again.")
                return false
            } else {
                script.logger.info("Inside the lair and can't kill again.")
                script.resetInside()
                script.banking = true
            }
        }
        val missingEquipment = script.allEquipment.filter { !it.item.hasWith() }
        val missingInv = script.inventory.filter { !it.meets() }
        if (missingEquipment.isNotEmpty() || missingInv.isNotEmpty()) {
            script.logger.info("MissingEquipment=${missingEquipment.joinToString()}, missingInv=${missingInv.joinToString()}}")
            if (missingEquipment.isEmpty() && missingInv.all { it.hasEnoughDoses() }) {
                script.logger.info("Super combat potion is not needed, so we can skip the banking step.")
                return false
            }
            script.banking = true
        }
        return script.banking
    }

    val pots1Dose = arrayOf(
        Potion.ANTI_VENOM_PLUS_EXTENDED,
        Potion.ANTI_VENOM_PLUS,
    )

    val potsBoost = arrayOf(
        Potion.SUPER_COMBAT,
        Potion.DIVINE_SUPER_COMBAT
    )

    fun InventoryRequirement.hasEnoughDoses(): Boolean {
        if (item in pots1Dose) {
            return getCount() >= 1
        } else if (item in potsBoost) {
            return !(item as Potion).needsRestore(80) || getCount() >= 1
        }
        return false
    }
}

class BankOpen(script: Araxxor, override val successComponent: TreeComponent<Araxxor>) :
    Branch<Araxxor>(script, "BankOpen?") {
    override val failedComponent: TreeComponent<Araxxor> = SimpleLeaf(script, "OpenBank") {
        script.resetInside()
        val teleport = if (Death.gravestoneActive()) TeleportMethod(null) else script.bankTeleport
        if (Bank.openNearest(teleport)) {
            waitFor(long()) { Bank.opened() }
        }
    }

    override fun validate(): Boolean = Bank.opened()
}

class ShouldStopScript(script: Araxxor) : Branch<Araxxor>(script, "ShouldStopScript?") {
    override val failedComponent: TreeComponent<Araxxor> = ShouldDeposit(script)
    override val successComponent: TreeComponent<Araxxor> = SimpleLeaf(script, "StoppingScript") {
        Notifications.showNotification("Slayer task is complete, stopping script.")
        script.logger.info("Slayer task is complete, stopping script.")
        ScriptManager.stop()
    }

    override fun validate(): Boolean {
        return Slayer.taskRemainder() <= 0
    }
}

class ShouldDeposit(script: Araxxor) : Branch<Araxxor>(script, "ShouldDeposit?") {
    private val potionsToFullDose =
        arrayOf(Potion.ANTI_VENOM, Potion.ANTI_VENOM_PLUS, Potion.PRAYER, Potion.SUPER_RESTORE)
    val itemNames by lazy {
        (script.inventory.map {
            //Only keep best ID for potions that we want to bring (4) dose of
            if (it.item in potionsToFullDose) it.item.itemName + "(4)" else it.item.itemName
        } + script.allEquipment.map { it.item.itemName }).toTypedArray()
    }
    override val failedComponent: TreeComponent<Araxxor> = ShouldEquipSpec(script)

    val filter = object : Filter<Item> {
        override fun accept(t: Item?): Boolean {
            val name = t?.name() ?: return true
            return itemNames.any { name.contains(it, true) }
        }
    }

    override val successComponent: TreeComponent<Araxxor> = SimpleLeaf(script, "Deposit") {
        script.logger.info("Depositing except: ${itemNames.joinToString()}")
        Bank.depositAllExcept(filter)
    }

    override fun validate(): Boolean = !Inventory.emptyExcept(*itemNames)
}

class ShouldEquipSpec(script: Araxxor) : Branch<Araxxor>(script, "ShouldEquipSpecGear?") {
    override val failedComponent: TreeComponent<Araxxor> = ShouldGetEquipment(script)
    override val successComponent: TreeComponent<Araxxor> = SimpleLeaf(script, "EquipSpecGear") {
        missingEquipment.forEach { it.withdrawAndEquip(false) }
    }
    private var missingEquipment = emptyList<EquipmentRequirement>()
    override fun validate(): Boolean {
        missingEquipment = script.specEquipment.filter { !it.meets() }
        return missingEquipment.isNotEmpty()
    }
}

class ShouldGetEquipment(script: Araxxor) : Branch<Araxxor>(script, "ShouldGetEquipment?") {
    override val failedComponent: TreeComponent<Araxxor> = ShouldGetInventory(script)
    override val successComponent: TreeComponent<Araxxor> = SimpleLeaf(script, "GetEquipment") {
        missingEquipment.forEach { it.withdraw(false) }
    }
    private var missingEquipment = emptyList<EquipmentRequirement>()
    override fun validate(): Boolean {
        missingEquipment = script.allEquipment.filter { !it.item.hasWith() }
        return missingEquipment.isNotEmpty()
    }
}

class ShouldGetInventory(script: Araxxor) : Branch<Araxxor>(script, "ShouldGetInventory?") {
    override val failedComponent: TreeComponent<Araxxor> = SimpleLeaf(script, "CloseBanking") {
        Bank.close()
        script.banking = false
    }
    override val successComponent: TreeComponent<Araxxor> = SimpleLeaf(script, "GetInventory") {
        if (Inventory.isFull()) {
            val food = Food.getFirstFood() ?: return@SimpleLeaf
            Bank.deposit(food.id, Bank.Amount.ALL)
        }
        missingInventory.forEach {
            script.logger.info("Missing inventory item: ${it.item.itemName}, has=${it.getCount()}, shouldHave=${it.minAmount}, teleportItem=${it.item is ITeleportItem}")
            it.withdraw(false)
        }
    }
    private var missingInventory = emptyList<InventoryRequirement>()
    override fun validate(): Boolean {
        missingInventory = script.inventory.filter { !it.meets() }
        return missingInventory.isNotEmpty()
    }
}
