package org.powbot.krulvis.api.extensions.items

import org.powbot.api.rt4.*
import org.powbot.api.rt4.magic.Rune
import org.powbot.krulvis.api.extensions.ScrollHelper
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("RunepouchLoadout")

enum class RunePouchLoadout(
    val rune1: Int,
    val rune2: Int,
    val rune3: Int,
    val rune4: Int,
    val amount1: Int,
    val amount2: Int,
    val amount3: Int,
    val amount4: Int
) {
    A(
        LOADOUT_A_RUNE1_BITSA,
        LOADOUT_A_RUNE2_BITSA,
        LOADOUT_A_RUNE3,
        LOADOUT_A_RUNE4,
        LOADOUT_A_CAP1,
        LOADOUT_A_CAP2,
        LOADOUT_A_CAP3,
        LOADOUT_A_CAP4
    ),
    B(
        LOADOUT_B_RUNE1,
        LOADOUT_B_RUNE2,
        LOADOUT_B_RUNE3,
        LOADOUT_B_RUNE4,
        LOADOUT_B_CAP1,
        LOADOUT_B_CAP2,
        LOADOUT_B_CAP3,
        LOADOUT_B_CAP4
    ),
    C(
        LOADOUT_C_RUNE1,
        LOADOUT_C_RUNE2,
        LOADOUT_C_RUNE3,
        LOADOUT_C_RUNE4,
        LOADOUT_C_CAP1,
        LOADOUT_C_CAP2,
        LOADOUT_C_CAP3,
        LOADOUT_C_CAP4
    ),
    D(
        LOADOUT_D_RUNE1,
        LOADOUT_D_RUNE2,
        LOADOUT_D_RUNE3,
        LOADOUT_D_RUNE4,
        LOADOUT_D_CAP1,
        LOADOUT_D_CAP2,
        LOADOUT_D_CAP3_BITSA,
        LOADOUT_D_CAP4
    ),
    E(
        LOADOUT_E_RUNE1_BITSA,
        LOADOUT_E_RUNE2_BITSA,
        LOADOUT_E_RUNE3,
        LOADOUT_E_RUNE4,
        LOADOUT_E_CAP1,
        LOADOUT_E_CAP2,
        LOADOUT_E_CAP3,
        LOADOUT_E_CAP4
    ),
    F(
        LOADOUT_F_RUNE1,
        LOADOUT_F_RUNE2,
        LOADOUT_F_RUNE3,
        LOADOUT_F_RUNE4,
        LOADOUT_F_CAP1,
        LOADOUT_F_CAP2,
        LOADOUT_F_CAP3,
        LOADOUT_F_CAP4
    ),
    G(
        LOADOUT_G_RUNE1,
        LOADOUT_G_RUNE2,
        LOADOUT_G_RUNE3,
        LOADOUT_G_RUNE4,
        LOADOUT_G_CAP1,
        LOADOUT_G_CAP2,
        LOADOUT_G_CAP3,
        LOADOUT_G_CAP4
    ),
    H(
        LOADOUT_H_RUNE1,
        LOADOUT_H_RUNE2,
        LOADOUT_H_RUNE3,
        LOADOUT_H_RUNE4,
        LOADOUT_H_CAP1,
        LOADOUT_H_CAP2,
        LOADOUT_H_CAP3_BITSA,
        LOADOUT_H_CAP4
    ),
    I(
        LOADOUT_I_RUNE1_BITSA,
        LOADOUT_I_RUNE2_BITSA,
        LOADOUT_I_RUNE3,
        LOADOUT_I_RUNE4,
        LOADOUT_I_CAP1,
        LOADOUT_I_CAP2,
        LOADOUT_I_CAP3,
        LOADOUT_I_CAP4
    ),
    J(
        LOADOUT_J_RUNE1,
        LOADOUT_J_RUNE2,
        LOADOUT_J_RUNE3,
        LOADOUT_J_RUNE4,
        LOADOUT_J_CAP1,
        LOADOUT_J_CAP2,
        LOADOUT_J_CAP3,
        LOADOUT_J_CAP4
    );

    fun getLoadout(): List<Pair<Rune, Int>> {
        return listOf(
            Pair(runeFromVarp(rune1), amountFromVarp(amount1)),
            Pair(runeFromVarp(rune2), amountFromVarp(amount2)),
            Pair(runeFromVarp(rune3), amountFromVarp(amount3)),
            Pair(runeFromVarp(rune4), amountFromVarp(amount4)),
        ).filter { it.first != Rune.NIL }
    }

    fun setLoadout(): Boolean {
        val loadButtons = loadoutButtons()
        if (loadButtons.isEmpty()) {
            return false
        }
        loadButtons.forEach {
            logger.warn("Found loadout button at index ${it.index()}")
        }
        val buttonIndex = loadButtons[ordinal].index()
        if (!ScrollHelper.scrollTo(
                { Widgets.component(RunePouch.ROOT_ID, buttonIndex) },
                { Widgets.component(RunePouch.ROOT_ID, 33) },
                { Widgets.component(RunePouch.ROOT_ID, 32) })
        ) {
            logger.warn("Failed to scroll")
            return false
        }
        return Widgets.component(RunePouch.ROOT_ID, buttonIndex).click()
    }

    override fun toString(): String {
        return getLoadout().joinToString { "${it.first}(${it.second})" }
    }

    companion object {
        fun loadoutButtons(): List<Component> {
            return Components.stream(15).filter { it.actions().contains("Load") }.toList()
        }

        fun runeFromVarp(index: Int): Rune {
            return Rune.entries[Varpbits.value(index)]
        }

        fun amountFromVarp(index: Int): Int {
            val amount = Varpbits.value(index)
            return if (amount == 0) 16000 else amount
        }

        fun loadoutForRunes(requiredRunes: List<Pair<Rune, Int>>): RunePouchLoadout? {
            return entries.firstOrNull { loadout ->
                val runes = loadout.getLoadout()
                requiredRunes.all { rq -> runes.any { rq.first == it.first && it.second >= rq.second } }
            }
        }
    }

}

const val LOADOUT_A_RUNE3 = 9681

const val LOADOUT_A_RUNE4 = 9682

const val LOADOUT_B_RUNE1 = 9685

const val LOADOUT_A_CAP1 = 9686

const val LOADOUT_B_RUNE2 = 9687

const val LOADOUT_B_RUNE3 = 9688

const val LOADOUT_B_RUNE4 = 9689

const val LOADOUT_A_CAP2 = 9690

const val LOADOUT_C_RUNE1 = 9691

const val LOADOUT_C_RUNE2 = 9694

const val LOADOUT_C_RUNE3 = 9695

const val LOADOUT_A_CAP3 = 9696

const val LOADOUT_C_RUNE4 = 9697

const val LOADOUT_D_RUNE1 = 9698

const val LOADOUT_D_RUNE2 = 9699

const val LOADOUT_A_CAP4 = 9700

const val LOADOUT_D_RUNE3 = 9703

const val LOADOUT_D_RUNE4 = 9704

const val LOADOUT_B_CAP1 = 9705

const val LOADOUT_D_CAP3_BITSA = 9706

const val LOADOUT_B_CAP2 = 9707

const val LOADOUT_B_CAP3 = 9708

const val LOADOUT_D_CAP3_BITSB = 9709

const val LOADOUT_B_CAP4 = 9712

const val LOADOUT_C_CAP1 = 9713

const val LOADOUT_D_CAP3_BITSC = 9714

const val LOADOUT_C_CAP2 = 9715

const val LOADOUT_C_CAP3 = 9716

const val LOADOUT_A_RUNE1_BITSA = 9717

const val LOADOUT_C_CAP4 = 9718

const val LOADOUT_D_CAP1 = 9721

const val LOADOUT_A_RUNE2_BITSA = 9722

const val LOADOUT_D_CAP2 = 9723

const val LOADOUT_D_CAP4 = 9724

const val LOADOUT_A_RUNE1_BITSB = 9725

const val LOADOUT_A_RUNE2_BITSB = 9726


const val LOADOUT_E_RUNE3 = 17969

const val LOADOUT_E_RUNE4 = 17970

const val LOADOUT_F_RUNE1 = 17971

const val LOADOUT_E_CAP1 = 17972

const val LOADOUT_F_RUNE2 = 17973

const val LOADOUT_F_RUNE3 = 17974

const val LOADOUT_F_RUNE4 = 17975

const val LOADOUT_E_CAP2 = 17976

const val LOADOUT_G_RUNE1 = 17977

const val LOADOUT_G_RUNE2 = 17978

const val LOADOUT_G_RUNE3 = 17979

const val LOADOUT_E_CAP3 = 17980

const val LOADOUT_G_RUNE4 = 17981

const val LOADOUT_H_RUNE1 = 17982

const val LOADOUT_H_RUNE2 = 17983

const val LOADOUT_E_CAP4 = 17984

const val LOADOUT_H_RUNE3 = 17985

const val LOADOUT_H_RUNE4 = 17986

const val LOADOUT_F_CAP1 = 17987

const val LOADOUT_H_CAP3_BITSA = 17988

const val LOADOUT_F_CAP2 = 17989

const val LOADOUT_F_CAP3 = 17990

const val LOADOUT_H_CAP3_BITSB = 17991

const val LOADOUT_F_CAP4 = 17992

const val LOADOUT_G_CAP1 = 17993

const val LOADOUT_H_CAP3_BITSC = 17994

const val LOADOUT_G_CAP2 = 17995

const val LOADOUT_G_CAP3 = 17996

const val LOADOUT_E_RUNE1_BITSA = 17997

const val LOADOUT_G_CAP4 = 17998

const val LOADOUT_H_CAP1 = 17999

const val LOADOUT_E_RUNE2_BITSA = 18000

const val LOADOUT_H_CAP2 = 18001

const val LOADOUT_H_CAP4 = 18002

const val LOADOUT_E_RUNE1_BITSB = 18003

const val LOADOUT_E_RUNE2_BITSB = 18004

const val LOADOUT_I_RUNE3 = 18005

const val LOADOUT_I_RUNE4 = 18006

const val LOADOUT_J_RUNE1 = 18007

const val LOADOUT_I_CAP1 = 18008

const val LOADOUT_J_RUNE2 = 18009

const val LOADOUT_J_RUNE3 = 18010

const val LOADOUT_J_RUNE4 = 18011

const val LOADOUT_I_CAP2 = 18012

const val LOADOUT_I_CAP3 = 18013

const val LOADOUT_I_CAP4 = 18014

const val LOADOUT_J_CAP2 = 18015

const val LOADOUT_J_CAP3 = 18016

const val LOADOUT_J_CAP4 = 18017

const val LOADOUT_J_CAP1 = 18018

const val LOADOUT_I_RUNE2_BITSA = 18019

const val LOADOUT_I_RUNE1_BITSA = 18020

const val LOADOUT_I_RUNE1_BITSB = 18021

const val LOADOUT_I_RUNE2_BITSB = 18022

