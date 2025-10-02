package org.powbot.krulvis.fletching.tree.leaf

import org.powbot.api.rt4.Bank
import org.powbot.api.rt4.Inventory
import org.powbot.api.rt4.bank.Quantity
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.emptyExcept
import org.powbot.krulvis.fletching.AuburnvaleFletcher

class HandleBank(script: AuburnvaleFletcher) : Leaf<AuburnvaleFletcher>(script, "Handle Bank") {
    override fun execute() {
        val necessary = arrayOf(script.logs.logName, "Fletching knife", "knife", script.deco.name, "Vale offerings")
        if (!Inventory.emptyExcept(*necessary)) {
            script.logger.info("Depositing inventory because has stuff that is not welcome")
            Bank.depositAllExcept(*necessary)
        }
        if (Inventory.stream().nameContains("knife").isEmpty()) {
            script.logger.info("Withdrawing knife")
            if(Bank.stream().name("Fletching knife").count(true) >= 1){
                Bank.withdraw("Fletching knife", 1)
            }else{
                Bank.withdraw("Knife", 1)
            }
        } else {
            script.logger.info("Withdrawing logs: ${script.logs.logName}")
            Bank.withdraw(script.logs.logName, Bank.Amount.ALL)
        }
    }
}