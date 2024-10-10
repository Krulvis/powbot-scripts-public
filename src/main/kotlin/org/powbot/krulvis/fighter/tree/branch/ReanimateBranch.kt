package org.powbot.krulvis.fighter.tree.branch

import org.powbot.api.rt4.*
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.krulvis.fighter.Fighter


enum class ReanimateHead(val spell: Magic.MagicSpell, vararg val names: String) {
    BASIC(Magic.ArceuusSpell.BASIC_REANIMATION, "goblin", "monkey", "imp", "minotaur", "scorpion", "bear", "unicorn"),
    ADEPT(Magic.ArceuusSpell.ADEPT_REANIMATION, "dog", "chaos", "ogre", "giant", "elf", "troll", "horror"),
    EXPERT(Magic.ArceuusSpell.EXPERT_REANIMATION, "kalphite", "dagannoth", "bloodveld", "tzhaar", "demon", "hellhound"),
    MASTER(Magic.ArceuusSpell.MASTER_REANIMATION, "aviansie", "abyssal", "dragon");

    fun getInvItem() = Inventory.stream().firstOrNull { item ->
        item.name().startsWith("Ensouled") && names.any { n -> item.name().contains(n, true) }
    }
}

class ShouldReanimate(script: Fighter) : Branch<Fighter>(script, "Should Reanimate?") {
    override val failedComponent: TreeComponent<Fighter> = IsWaitingForLoot(script)
    override val successComponent: TreeComponent<Fighter> = SimpleLeaf(script, "Reanimating") {
        if (!spellHead!!.first.casting()) {
            if (!spellHead!!.first.cast()) {
                waitFor { spellHead!!.first.casting() && Inventory.opened() }
                script.logger.info("Inv open=${Inventory.opened()}, casting=${spellHead!!.first.casting()}")
            }
        }
        if (spellHead!!.first.casting()) {
            spellHead!!.second.interact("Cast")
            var reanimated: Npc? = null
            waitFor(10000) {
                reanimated = Npcs.stream().firstOrNull {
                    it.name.contains("reanimated", true)
                }
                return@waitFor reanimated != null
            }
            if (reanimated?.interact("Attack") == true) {
                waitFor(2000) { me.interacting() == reanimated }
            }
        }
    }

    var spellHead: Pair<Magic.MagicSpell, Item>? = null

    override fun validate(): Boolean {
        ReanimateHead.values().forEach {
            val invItem = it.getInvItem()
//            script.logger.info("Inv ensouled head for ${it.name} = ${invItem}")
            if (invItem != null) {
                spellHead = Pair(it.spell, invItem)
                return it.spell.canCast()
            }
        }

        return false
    }
}