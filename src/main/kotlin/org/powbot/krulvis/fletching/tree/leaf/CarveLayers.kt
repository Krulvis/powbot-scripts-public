package org.powbot.krulvis.fletching.tree.leaf

import org.powbot.api.rt4.Components
import org.powbot.api.rt4.Npcs
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.walkAndInteract
import org.powbot.krulvis.api.extensions.Utils.long
import org.powbot.krulvis.api.extensions.Utils.mid
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.krulvis.api.extensions.Utils.waitForDistance
import org.powbot.krulvis.fletching.ANIMALS
import org.powbot.krulvis.fletching.AuburnvaleFletcher
import org.powbot.krulvis.fletching.animalNameForIndex

class CarveLayers(script: AuburnvaleFletcher) : Leaf<AuburnvaleFletcher>(script, "Carve Layers") {

    fun getAnimalComps() = Components.stream(270).nameContains(*ANIMALS).viewable().list()

    override fun execute() {
        var comps = getAnimalComps()
        script.fletching = false
        if (comps.isEmpty()) {
            //Need to open the widget first
            val totem = script.current.key.totem()
            if (walkAndInteract(totem, "Carve")) {
                waitForDistance(totem) {
                    comps = getAnimalComps()
                    comps.isNotEmpty()
                }
            }
        }
        if (comps.isNotEmpty()) {
            val layers = script.current.key.layers()
            val animalsNearMe =
                Npcs.stream().within(20).nameContains(*ANIMALS).list().map { it.name.replace(" spirit", "") }
            val carvedAnimals = layers.filter { it >= 10 }.map { animalNameForIndex(it - 10) }
            script.logger.info("We have near us: [${animalsNearMe.joinToString()}]")
            script.logger.info("We have carved : [${carvedAnimals.joinToString()}]")
            val nextAnimal = animalsNearMe.first { !carvedAnimals.contains(it) }
            script.logger.info("Should carve nw: $nextAnimal")
            val comp = comps.firstOrNull { it.name().contains(nextAnimal) }
            if (comp == null) {
                script.logger.info("Could not find $nextAnimal component")
                return
            }
            if (comp.click()) {
                if (waitFor(mid()) {
                        script.current.key.layers().filter { it >= 10 }.size > carvedAnimals.size
                    }) {
                    if (script.current.key.hasLayers()) {
                        return
                    }
                    waitFor(long()) { getAnimalComps().isNotEmpty() }
                }
            }
        }
    }
}