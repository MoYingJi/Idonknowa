package moyingji.idonknowa.poi

import moyingji.idonknowa.Idonknowa.id
import moyingji.idonknowa.all.*
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.item.*

object ModVillager {
    // level: 新手 学徒 老手 专家 大师

    val genshiner = SimpleVillagerPoi("genshiner".id,
        SoundEvents.VILLAGER_WORK_TOOLSMITH,
        ModBlock.ISEKAI_RESEARCH_TABLE
    ).reg().tagAcquirable().listen {
        priceMod {
            arg(1, 3, 5, 20)
            priceA = Items.EMERALD to 16..21
            priceB = Items.AMETHYST_CLUSTER to 3..7
            sale = ModItem.PRIMOGEM to 2..5
        } // {1} 绿宝石[16,21] 紫水晶簇[3,7] -> ( 原石[2,5] ) 3t 5e 20p
        priceMod {
            arg(1, 3, 5, 20)
            price = ModItem.PRIMOGEM to 1..3
            sale = Items.EMERALD to 16..21
        } // {1} 原石[1,3] -> ( 绿宝石[7,16] ) 3t 5e 20p
        priceMod {
            arg(1, 3, 5, 20)
            priceA = Items.EMERALD to 1..3
            priceB = Items.GOLD_INGOT to 6
            sale = ModItem.MORA to 6
        } // {1} 绿宝石[2,7] 金锭[6] -> ( 摩拉[6] ) 3t 5e 20p
        priceMod {
            arg(2, 3, 15, 20)
            priceA = Items.EMERALD to 3..7
            priceB = ModItem.PRIMOGEM to 16
            sale = ModItem.INTERTWINED_FATE to 1
        } // {2} 绿宝石[3,7] 原石[16] -> ( 纠缠之缘 ) 3t 15e 5p
    }

}