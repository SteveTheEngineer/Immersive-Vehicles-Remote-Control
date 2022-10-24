package me.ste.ivremotecontrol.util

import mcinterface1122.BuilderEntityExisting
import mcinterface1122.BuilderTileEntity
import mcinterface1122.WrapperEntity
import mcinterface1122.WrapperWorld
import minecrafttransportsimulator.blocks.tileentities.components.ATileEntityBase
import minecrafttransportsimulator.entities.components.AEntityA_Base
import minecrafttransportsimulator.entities.components.AEntityB_Existing
import net.minecraft.entity.Entity
import net.minecraft.world.World

object MTSUtil {
    // Gets the minecraft entity by its MTS variant
    fun getEntity(base: AEntityA_Base): Entity? {
        for (entity in (base.world as WrapperWorld).mcWorld.loadedEntityList) {
            if (entity is BuilderEntityExisting) {
                val mtsEntity = entity.mtsEntity
                if (mtsEntity != null && mtsEntity.uniqueUUID == base.uniqueUUID) {
                    return entity
                }
            }
        }
        return null
    }
}

val BuilderEntityExisting.mtsEntity: AEntityB_Existing?
    get() {
        val field = BuilderEntityExisting::class.java.getDeclaredField("entity")
        field.isAccessible = true
        return field.get(this) as? AEntityB_Existing
    }

val WrapperEntity.minecraftEntity: Entity
    get() {
        val field = WrapperEntity::class.java.getDeclaredField("entity")
        field.isAccessible = true
        return field.get(this) as Entity
    }

val BuilderTileEntity<*>.mtsTileEntity: ATileEntityBase<*>?
    get() {
        val field = BuilderTileEntity::class.java.getDeclaredField("tileEntity")
        field.isAccessible = true
        return field.get(this) as? ATileEntityBase<*>
    }

val WrapperWorld.mcWorld: World
    get() {
        val field = WrapperWorld::class.java.getDeclaredField("world")
        field.isAccessible = true
        return field.get(this) as World
    }
