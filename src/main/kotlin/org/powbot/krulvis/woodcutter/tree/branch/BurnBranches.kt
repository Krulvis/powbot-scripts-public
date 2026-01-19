package org.powbot.krulvis.woodcutter.tree.branch

import org.powbot.api.rt4.Inventory
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.woodcutter.Woodcutter
import org.powbot.krulvis.woodcutter.tree.leaf.Burn

class ShouldBurn(script: Woodcutter) : Branch<Woodcutter>(script, "Should Burn?") {
    override val failedComponent: TreeComponent<Woodcutter> = ShouldBank(script)
    override val successComponent: TreeComponent<Woodcutter> = Burn(script)

    override fun validate(): Boolean {
        return script.burning || (Inventory.isFull() && script.burn)
    }
}
