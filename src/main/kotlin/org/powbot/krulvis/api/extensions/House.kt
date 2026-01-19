package org.powbot.krulvis.api.extensions

import org.powbot.api.rt4.*
import org.powbot.api.rt4.walking.model.Skill
import org.powbot.krulvis.api.ATContext.currentHP
import org.powbot.krulvis.api.ATContext.debug
import org.powbot.krulvis.api.ATContext.maxHP
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.slf4j.LoggerFactory

object House {
    val logger = LoggerFactory.getLogger("House")

    //
    val PARENT = 370

    fun houseOptionsOpen(): Boolean = Widgets.widget(PARENT).component(0).visible()

    //
    fun openHouseOptions(): Boolean {
        if (houseOptionsOpen()) {
            return true
        }
        if (!Game.tab(Game.Tab.SETTINGS)) {
            return false
        }
        val houseOptionButton = Components.stream(116).action("View House Options").firstOrNull()
        if (houseOptionButton != null) {
            houseOptionButton.click()
            return waitFor { houseOptionsOpen() }
        }
        return false
    }

    fun callButton() = Components.stream(PARENT)
        .action("Call Servant").firstOrNull()

    fun callButler(): Boolean {
        if (!openHouseOptions()) {
            return false
        }
        val bttn1 = Components.stream(PARENT)
            .action("Call Servant").firstOrNull()
        val bttn = Widgets.widget(PARENT).component(22)
        debug("Found with action=$bttn1, With index=$bttn, actions=[${bttn.actions().joinToString()}]")
        return bttn.click()
    }


    fun canCall(): Boolean = Objects.stream().action("Ring").isNotEmpty()

    fun isInside() = Objects.stream(40).type(GameObject.Type.INTERACTIVE)
        .name("Portal").action("Lock").isNotEmpty()

    //
    fun inBuildingMode(): Boolean = Varpbits.varpbit(780) == 1


    fun getPool(): GameObject = Objects.stream(30, GameObject.Type.INTERACTIVE).name(*Pool.allPools).first()

    enum class Pool(val poolName: String) {
        RESTORATION("Pool of Restoration"),
        REVITALISATION("Pool of Revitalisation"),
        REJUVENATION("Pool of Rejuvenation"),
        FANCY_REJUVENATION("Fancy pool of Rejuvenation"),
        ORNATE("Ornate pool of Rejuvenation"),
        REFRESHMENT("Pool of Refreshment"),
        ;

        fun shouldRestorePool(): Boolean {
            if (ordinal >= 1 && Movement.energyLevel() <= 90) return true
            if (ordinal >= 2 && Skills.level(Skill.Prayer) < Skills.realLevel(Skill.Prayer)) return true
            if (ordinal >= 4 && currentHP() < maxHP()) return true

            return this != REFRESHMENT && Combat.specialPercentage() < 100
        }

        companion object {
            val allPools = Pool.entries.map { it.poolName }.toTypedArray()

            fun GameObject.pool() = Pool.entries.firstOrNull { it.poolName == name }

            fun shouldRestorePool(poolObj: GameObject): Boolean {
                return poolObj.pool()?.shouldRestorePool() ?: false
            }
        }
    }
}
