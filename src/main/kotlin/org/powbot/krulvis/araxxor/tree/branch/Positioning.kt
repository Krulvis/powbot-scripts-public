package org.powbot.krulvis.araxxor.tree.branch

import org.powbot.api.Tile
import org.powbot.api.rt4.Movement
import org.powbot.api.rt4.Objects
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.ATContext.stepNoConfirm
import org.powbot.krulvis.api.ATContext.surroundingTiles
import org.powbot.krulvis.api.extensions.Monster.borderingLayer
import org.powbot.krulvis.api.extensions.Monster.getNearestOutsideForInsideTile
import org.powbot.krulvis.api.extensions.Utils.short
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.krulvis.araxxor.Araxxor
import org.powbot.krulvis.araxxor.Data
import org.powbot.krulvis.araxxor.Data.findSafestTile
import kotlin.math.abs
import kotlin.math.sign


class ShouldReposition(script: Araxxor) : Branch<Araxxor>(script, "ShouldReposition?") {
    override val failedComponent: TreeComponent<Araxxor> = ShouldCastResurrect(script)
    override val successComponent: TreeComponent<Araxxor> = SimpleLeaf(script, "Reposition") {
        val shouldWalk = walkFromDrip && script.explodingTimer.isFinished()
        Movement.running(!shouldWalk)
        var startTick = script.ticks

        if (Movement.destination() != script.newPosition) {
            Movement.stepNoConfirm(script.newPosition, true)
        }

        if (script.enrage && !script.quickStep) {
            script.enrageWalkTimeout.reset()
        } else {
            waitFor(short()) { script.ticks > startTick }
        }
    }


    var walkFromDrip = false
    var myPos = Tile.Nil

    override fun validate(): Boolean {
        myPos = me.trueTile()

        if (!script.araxxor.valid()) {
            script.logger.info("Araxxor is not valid, walking to center of room")
            script.newPosition = Objects.stream(30).name("Web tunnel").first().tile.derive(-10, 0)
            return script.newPosition != me.trueTile() && Movement.destination() != script.newPosition
        }
        script.newPosition = positionForDrip()
        val exploDestination = positionForExploding()
        val nextEggDestination = tileNearNextEgg()
        val enrageTile = enrageTile()
        if (!script.explodingTimer.isFinished() || (!script.enrage && script.exploding.valid())) {
            script.logger.info("Exploding spider exists, walking to dodge spider")
            script.newPosition = exploDestination
        } else if (!script.enrage && nextEggDestination != myPos) {
            script.logger.info("Next egg is not in view, walking to it")
            script.newPosition = nextEggDestination
        } else if (script.enrage && enrageTile != myPos) {
            script.logger.info("Enraged, walking to enrage tile")
            script.newPosition = enrageTile
        }

        if (script.unsafeTiles.contains(script.newPosition)) {
            script.logger.info("New position is unsafe, generating new tile from surrounding")
            val potentialTiles = script.newPosition.surroundingTiles(true).toTypedArray()
            script.newPosition = findSafestTile(potentialTiles, script.unsafeTiles, myPos, script.flags)
        }
        return script.newPosition != myPos && Movement.destination() != script.newPosition
    }

    fun enrageTile(): Tile {
        var dest = Movement.destination()
        if (dest == Tile.Nil) {
            dest = myPos
        }
        if (!script.enrage) return dest
        val araxxorAttacking = script.aboutToAttack()
        if (script.quickStep && !araxxorAttacking) {
            //Find position underneath araxxor that will lead to safe spot after attacking
            val insideTiles = script.enrageTiles
            val selfDamagingTile = insideTiles.filter {
                !script.unsafeTiles.contains(it) &&
                        !script.unsafeTiles.contains(script.araxxorRect.getNearestOutsideForInsideTile(it))
            }.minByOrNull { it.distanceTo(myPos) }
            if (selfDamagingTile != null) {
                return selfDamagingTile
            }
        }

        //When he is about to attack or when he is in attack, we gotta make sure we move
        if (araxxorAttacking) {
            script.logger.info("ABOUT TO ATTACK")
            //We're already moving
            if (dest.distanceTo(myPos) >= 2) {
                return dest
            } else {
                val potential = script.araxxorRect.borderingLayer(1).filter {
                    !script.unsafeTiles.contains(it) && !script.enrageNewPools.contains(it)
                            && it.distanceTo(myPos) >= 2
                }
                return findSafestTile(
                    potential.toTypedArray(),
                    script.unsafeTiles + script.enrageNewPools,
                    myPos,
                    script.flags,
                    surroundingWeight = 0.5,
                    distanceWeight = -1.0,
                    pathWeight = 3.0
                )
            }
        } else {
            return dest
        }
    }

    fun tileNearNextEgg(): Tile {
        val egg = script.eggs.firstOrNull()?.tile() ?: return myPos
        //We don't want to walk towards red egg
        if (Data.Egg.forNpc(script.eggs.firstOrNull()!!) == Data.Egg.RED) return myPos
        if (egg.matrix().inViewport()) return myPos
        val arax = script.araxxorCenter

        // Calculate direction vector from current position to egg
        val dx = egg.x - myPos.x
        val dy = egg.y - myPos.y

        // Calculate new position that's towards the egg but constrained by distance from araxxorCenter
        val newX = myPos.x + dx.coerceIn(-8, 8)
        val newY = myPos.y + dy.coerceIn(-8, 8)
        val proposedTile = Tile(newX, newY, myPos.floor())

        // If the proposed tile is too far from araxxorCenter, adjust it
        val dxFromCenter = proposedTile.x - arax.x
        val dyFromCenter = proposedTile.y - arax.y

        return Tile(
            arax.x + dxFromCenter.coerceIn(-5, 5),
            arax.y + dyFromCenter.coerceIn(-5, 5),
            me.floor()
        )
    }

    fun positionForDrip(): Tile {
        walkFromDrip = !script.drippingTimer.isFinished()
        if (!walkFromDrip) return myPos
        val dest = Movement.destination()
        if (dest != Tile.Nil && dest.distance() * 600 >= script.drippingTimer.getRemainder()) return dest
        val options = script.borderTiles
        return findSafestTile(
            options,
            script.unsafeTiles,
            myPos,
            script.flags,
            distanceWeight = 2.0,
            pathWeight = 1.0
        )
    }

    fun positionForExploding(): Tile {
        if (!script.exploding.valid()) {
            return myPos
        }

        val arax = script.araxxorCenter
        val expl = script.exploding.tile()
        val explDistance = expl.distanceTo(myPos)
        // If exploding spider is close, reset timer
        if (explDistance <= 4 && script.explodingTimer.isFinished()) {
            script.logger.info("EXPLODING CLOSE ENOUGH, STARTING TIMER")
            script.explodingTimer.reset()
        }


        if (!script.explodingTimer.isFinished()) {
            script.logger.info("EXPLODING!!! distance to us = $explDistance")

            // Calculate vector from exploding spider to me
            val dx = myPos.x - expl.x
            val dy = myPos.y - expl.y

            val runToTile = if (abs(dx) >= abs(dy)) {
                expl.derive((dx.sign * 6).coerceIn(-6, 6), 0)
            } else {
                expl.derive(0, (dy.sign * 6).coerceIn(-6, 6))
            }

            // Generate potential safe tiles in the direction away from the exploding spider
            val potentialTiles = runToTile.surroundingTiles(true).toMutableList()

            // Find the safest tile among potential destinations
            script.exploDestination = findSafestTile(
                potentialTiles.toTypedArray(),
                script.unsafeTiles,
                myPos,
                script.flags,
                distanceWeight = 1.0
            )

            if (script.exploDestination == myPos) {
                script.exploDestination = runToTile
            }

            //We gotta make sure we're on the right spot, otherwise script will walk back into explo guy
            return if (script.exploDestination.distanceTo(myPos) >= 1) script.exploDestination else myPos
        } else {
            // When no immediate threat, stay on opposite side of Araxxor from the exploding spider
            val dx = (arax.x - expl.x)
            val dy = (arax.y - expl.y)
            if (abs(dx) <= 4 && abs(dy) <= 4) {
                return myPos
            }
            // Generate potential safe tiles around Araxxor
            val potentialTiles = mutableListOf<Tile>()
            potentialTiles.add(arax.derive(dx.coerceIn(-4, 4), dy.coerceIn(-4, 4)))
            potentialTiles.addAll(potentialTiles.first().surroundingTiles())

            // Find the safest tile among potential destinations
            script.exploDestination = findSafestTile(
                potentialTiles.toTypedArray(),
                script.unsafeTiles,
                myPos,
                script.flags
            )

        }

        return if (script.exploDestination.distanceTo(myPos) >= 5) script.exploDestination else myPos

    }
}