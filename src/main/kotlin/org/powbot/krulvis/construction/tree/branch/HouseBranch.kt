package org.powbot.krulvis.construction.tree.branch

import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.extensions.House
import org.powbot.krulvis.construction.Construction
import org.powbot.krulvis.construction.tree.leaf.EnterHouse
import org.powbot.krulvis.construction.tree.leaf.GoBuildingMode

class InHouse(script: Construction) : Branch<Construction>(script, "InHouse?") {
    override val failedComponent: TreeComponent<Construction> = EnterHouse(script)
    override val successComponent: TreeComponent<Construction> = BuildingModeOn(script)

    override fun validate(): Boolean {
        return House.isInside()
    }
}

class BuildingModeOn(script: Construction) : Branch<Construction>(script, "BuildingModeOn?") {
    override val failedComponent: TreeComponent<Construction> = GoBuildingMode(script)
    override val successComponent: TreeComponent<Construction> = ShouldSendDemon(script)

    override fun validate(): Boolean {
        return House.inBuildingMode()
    }
}