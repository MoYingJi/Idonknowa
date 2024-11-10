package moyingji.idonknowa

import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.*
import net.minecraft.resources.ResourceLocation

// region 常用类简化别名

typealias Id = ResourceLocation

typealias Text = Component
typealias MutableText = MutableComponent
typealias Formatting = ChatFormatting

// endregion

// region Datagen 相关
typealias DataGener = FabricDataGenerator
typealias DataPack = FabricDataGenerator.Pack
typealias DataFactory<T> = FabricDataGenerator.Pack.Factory<T>
typealias DataFactoryWithReg<T> = FabricDataGenerator.Pack.RegistryDependentFactory<T>
// endregion