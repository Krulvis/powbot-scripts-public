package org.powbot.krulvis.api.extensions.items

import org.powbot.api.rt4.Bank
import org.powbot.api.rt4.Inventory
import org.powbot.api.rt4.Item
import org.powbot.krulvis.api.ATContext.getCount
import org.powbot.krulvis.api.utils.Utils.waitFor

interface Item {

    val ids: IntArray

    val id: Int
        get() = ids[0]

    fun getNotedIds(): IntArray = ids.map { it + 1 }.toIntArray()

    fun notedInBank(): Boolean = Bank.stream().id(*getNotedIds()).isNotEmpty()

    fun inInventory(): Boolean = Inventory.stream().id(*ids).isNotEmpty()

    fun hasWith(): Boolean

    fun inBank(): Boolean = Bank.stream().id(*ids).isNotEmpty()

    fun getBankId(worse: Boolean = false): Int {
        val ids = if (worse) ids.reversed().toIntArray() else ids
        val bankIds = Bank.stream().filtered { it.id() in ids }.map { it.id }
        val bankItem = ids.firstOrNull { it in bankIds }
        return bankItem ?: -1
    }

    fun getInvItem(worse: Boolean = true): Item? {
        return if (worse) {
            val items = Inventory.stream().list()
            ids.reversed().forEach { id ->
                if (items.any { it.id == id }) {
                    return items.firstOrNull { it.id == id }
                }
            }
            return null
        } else
            Inventory.stream().id(*ids).firstOrNull()
    }

    fun getInventoryCount(countNoted: Boolean = true): Int {
        return if (countNoted) Inventory.stream()
                .filtered { ids.contains(it.id()) || getNotedIds().contains(it.id()) }
                .sumOf { if (it.stack <= 0) 1 else it.stack }
        else Inventory.stream().id(*ids).count().toInt()
    }

    fun getCount(countNoted: Boolean = true): Int

    fun withdrawExact(amount: Int, worse: Boolean = false, wait: Boolean = true): Boolean {
        val currentAmount = Inventory.getCount(*ids)
        if (currentAmount == amount) {
            return true
        } else if (currentAmount > amount) {
            if (Bank.deposit(Inventory.stream().id(*ids).first().id, currentAmount - amount)) {
                return !wait || waitFor { Inventory.getCount(*ids) == amount }
            }
        } else if (currentAmount < amount) {
            val id = getBankId(worse)
            if (Bank.withdraw(id, amount - currentAmount)) {
                return !wait || waitFor { Inventory.getCount(*ids) == amount }
            }
        }
        return false
    }

//    fun itemName(): String = CacheItemConfig.load(id).name

    companion object {
        const val HAMMER = 2347
        const val CANNONBALL = 2
        const val AMMO_MOULD = 4
        const val AMMO_MOULD_DOUBLE = 27012
        const val GRIMY_GUAM = 199
        const val SAW = 8794
        const val BOND_CHARGED = 13190
        const val BOND_UNCHARGED = 13192
        const val BRONZE_AXE = 1351
        const val MITHRIL_AXE = 1355
        const val EMPTY_BUCKET = 1925
        const val FISHING_NET = 303
        const val COOKED_SHRIMP = 315
        const val EMPTY_POT = 1931
        const val BREAD = 2309
        const val BRONZE_PICKAXE = 1265
        const val BRONZE_DAGGER = 1205
        const val BRONZE_SWORD = 1277
        const val WOODEN_SHIELD = 1171
        const val SHORTBOW = 841
        const val BRONZE_ARROW = 882
        const val BALL_OF_WOOL = 1759
        const val REDBERRIES = 1951
        const val POT_OF_FLOUR = 1933
        const val PINK_SKIRT = 1013
        const val BEER = 1917
        const val BIRD_SNARE = 10006
        const val BOX_TRAP = 10008
        const val BUCKET_OF_WATER = 1929
        const val ASHES = 592
        const val BRONZE_BAR = 2349
        const val RING_OF_FORGING = 2568
        const val SOFT_CLAY = 1791
        const val YELLOW_DYE = 1765
        const val ROPE = 954
        const val COINS = 995
        const val TINDERBOX = 590
        const val BAG_OF_SALT = 4161
        const val WILLOW_LOG = 1519
        const val OAK_LOGS = 1521
        const val BUCKET_OF_SAP = 4687
        const val RAW_KARAMBWAN = 3142
        const val RAW_KARAMBWANJI = 3150
        const val TRADING_STICK = 6306
        const val JADE = 1611
        const val OPAL = 1609
        const val VIAL = 229
        const val PIE_DISH = 2313
        const val JUG = 1935
        const val TAI_BWO_WANNAI_TELEPORT = 12409
        const val ANTI_DRAGON_SHIELD = 1540
        const val BRONZE_PLATELEGS = 1075
        const val BRONZE_PLATESKIRT = 1087
        const val BRONZE_CHAINBODY = 1103
        const val BRONZE_PLATEBODY = 1117
        const val BRONZE_FULLHELM = 1155
        const val BRONZE_SQSHIELD = 1173
        const val BRONZE_KITESHIELD = 1189
        const val BRONZE_LONGSWORD = 1291
        const val BRONZE_2HSWORD = 1307
        const val BRONZE_SCIMITAR = 1321
        const val BRONZE_WARHAMMER = 1337
        const val BRONZE_BATTLEAXE = 1375
        const val BRONZE_CLAWS = 3095
        const val IRON_PLATELEGS = 1067
        const val IRON_PLATESKIRT = 1081
        const val IRON_CHAINBODY = 1101
        const val IRON_PLATEBODY = 1115
        const val IRON_FULLHELM = 1153
        const val IRON_SQSHIELD = 1175
        const val IRON_KITESHIELD = 1191
        const val IRON_LONGSWORD = 1293
        const val IRON_2HSWORD = 1309
        const val IRON_SCIMITAR = 1323
        const val IRON_WARHAMMER = 1335
        const val IRON_BATTLEAXE = 1363
        const val IRON_CLAWS = 3096
        const val STEEL_PLATELEGS = 1069
        const val STEEL_PLATESKIRT = 1083
        const val STEEL_CHAINBODY = 1105
        const val STEEL_PLATEBODY = 1119
        const val STEEL_FULLHELM = 1157
        const val STEEL_SQSHIELD = 1177
        const val STEEL_KITESHIELD = 1193
        const val STEEL_LONGSWORD = 1295
        const val STEEL_2HSWORD = 1311
        const val STEEL_SCIMITAR = 1325
        const val STEEL_WARHAMMER = 1339
        const val STEEL_BATTLEAXE = 1365
        const val STEEL_CLAWS = 3097
        const val GOLD_BRACELET = 11069
        const val GOBLIN_STAFF = 11709
        const val MITHRIL_PLATELEGS = 1071
        const val MITHRIL_PLATESKIRT = 1085
        const val MITHRIL_CHAINBODY = 1109
        const val MITHRIL_PLATEBODY = 1121
        const val MITHRIL_FULLHELM = 1159
        const val MITHRIL_SQSHIELD = 1181
        const val MITHRIL_KITESHIELD = 1197
        const val MITHRIL_LONGSWORD = 1299
        const val MITHRIL_2HSWORD = 1315
        const val MITHRIL_SCIMITAR = 1329
        const val MITHRIL_WARHAMMER = 1343
        const val MITHRIL_BATTLEAXE = 1369
        const val MITHRIL_CLAWS = 3099
        const val ADAMANT_PLATELEGS = 1073
        const val ADAMANT_PLATESKIRT = 1091
        const val ADAMANT_CHAINBODY = 1111
        const val ADAMANT_PLATEBODY = 1123
        const val ADAMANT_FULLHELM = 1161
        const val ADAMANT_SQSHIELD = 1183
        const val ADAMANT_KITESHIELD = 1199
        const val ADAMANT_LONGSWORD = 1301
        const val ADAMANT_2HSWORD = 1317
        const val ADAMANT_SCIMITAR = 1331
        const val ADAMANT_WARHAMMER = 1345
        const val ADAMANT_BATTLEAXE = 1371
        const val ADAMANT_CLAWS = 3100
        const val ADAMANT_BOOTS = 4129
        const val RUNE_PLATELEGS = 1079
        const val RUNE_PLATESKIRT = 1093
        const val RUNE_CHAINBODY = 1113
        const val RUNE_PLATEBODY = 1127
        const val RUNE_FULLHELM = 1163
        const val RUNE_SQSHIELD = 1185
        const val RUNE_KITESHIELD = 1201
        const val RUNE_LONGSWORD = 1303
        const val RUNE_2HSWORD = 1319
        const val RUNE_SCIMITAR = 1333
        const val RUNE_WARHAMMER = 1347
        const val RUNE_BATTLEAXE = 1373
        const val RUNE_CLAWS = 3101

        const val STEEL_NAIL = 1539
        const val DRAMEN_STAFF = 772
        const val SILVER_SICKLE_B = 2963
        const val SALVE_TELEPORT = 19619
        const val HOUSE_TELEPORT = 8013
        const val CAMELOT_TELEPORT = 8010
        const val BOLT_OF_CLOTH = 8790
        const val MORT_MYRE_FUNGUS = 2970
        const val BURNT_PAGE = 20718
        const val EMPTY_TOME = 20716
        const val EMPTY_SEAS = 11908
        const val KNIFE = 946
        const val DARK_KEY = 25244
        const val SHIELD_LEFT_HALF = 2366
        const val DRAGON_MED_HELM = 1149
        const val DRAGON_SPEAR = 1249
        const val HERB_SACK_OPEN = 24478
        const val RUNE_POUCH = 12791
        const val SEED_BOX_OPEN = 24482
        const val IMCANDO_HAMMER = 25644
        const val BLESSED_BONE_SHARD = 29381
    }

}