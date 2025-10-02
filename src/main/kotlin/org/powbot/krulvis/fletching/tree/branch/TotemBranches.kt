package org.powbot.krulvis.fletching.tree.branch

import org.powbot.api.rt4.GameObject
import org.powbot.api.rt4.Inventory
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.ATContext.walkAndInteract
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.krulvis.api.extensions.Utils.waitForDistance
import org.powbot.krulvis.fletching.AuburnvaleFletcher
import org.powbot.krulvis.fletching.OFFERINGS
import org.powbot.krulvis.fletching.tree.leaf.CarveLayers
import org.powbot.krulvis.fletching.tree.leaf.Decorations
import org.powbot.krulvis.fletching.tree.leaf.WalkToNext

class NearNext(script: AuburnvaleFletcher) : Branch<AuburnvaleFletcher>(script, "NearNext?") {
    override val failedComponent: TreeComponent<AuburnvaleFletcher> = WalkToNext(script)
    override val successComponent: TreeComponent<AuburnvaleFletcher> = CanClaimOfferings(script)

    override fun validate(): Boolean {
        return script.current.key.totemTile.distance() < 6
    }
}

class CanClaimOfferings(script: AuburnvaleFletcher) : Branch<AuburnvaleFletcher>(script, "CanClaimOfferings?") {
    override val failedComponent: TreeComponent<AuburnvaleFletcher> = IsBuilt(script)
    override val successComponent: TreeComponent<AuburnvaleFletcher> = SimpleLeaf(script, "Claim Offerings") {
        val currCount = Inventory.stream().id(OFFERINGS).count(true)
        script.fletching = false
        if (walkAndInteract(offerings, "Claim")) {
            waitForDistance(offerings, extraWait = 2000) {
                currCount < Inventory.stream().id(OFFERINGS).count(true)
            }
        }
    }

    var offerings = GameObject.Nil
    override fun validate(): Boolean {
        if (Inventory.isFull() && Inventory.stream().id(OFFERINGS).isEmpty()) return false
        offerings = script.current.key.offerings()
        return offerings.valid()
    }
}

class IsBuilt(script: AuburnvaleFletcher) : Branch<AuburnvaleFletcher>(script, "IsBuilt?") {
    override val failedComponent: TreeComponent<AuburnvaleFletcher> = SimpleLeaf(script, "Build totem") {
        script.fletching = false
        val site = script.current.key.totem()
        if (!site.valid()) {
            script.logger.info("Cannot find totem site")
        } else if (walkAndInteract(site, "Build")) {
            waitForDistance(site, extraWait = 2000) { script.current.key.built() }
        }
    }
    override val successComponent: TreeComponent<AuburnvaleFletcher> = HasLayers(script)

    override fun validate(): Boolean {
        return script.current.key.built()
    }
}

class HasLayers(script: AuburnvaleFletcher) : Branch<AuburnvaleFletcher>(script, "HasLayers?") {
    override val failedComponent: TreeComponent<AuburnvaleFletcher> = CarveLayers(script)

    override val successComponent: TreeComponent<AuburnvaleFletcher> = HasLogs(script)

    override fun validate(): Boolean {
        return script.current.key.hasLayers()
    }
}

class HasLogs(script: AuburnvaleFletcher) : Branch<AuburnvaleFletcher>(script, "HasLogs?") {
    override val failedComponent: TreeComponent<AuburnvaleFletcher> = Decorations(script)


    override val successComponent: TreeComponent<AuburnvaleFletcher> = SimpleLeaf(script, "Set Next") {
        if (script.totemTimer.isFinished()) {
            script.setNextTotem()
        } else {
            script.logger.info("Waiting for totem to be destroyed")
        }
    }

    override fun validate(): Boolean {
        return script.current.key.hasDecorations()
    }
}