package me.ste.ivremotecontrol.util

import minecrafttransportsimulator.entities.components.AEntityA_Base
import minecrafttransportsimulator.mcinterface.BuilderEntityExisting
import net.minecraft.entity.Entity

object MTSUtil {
    // Gets the minecraft entity by it's MTS variant
    fun getEntity(base: AEntityA_Base): Entity? {
        for (entity in base.world.world.loadedEntityList) {
            if (entity is BuilderEntityExisting && entity.entity.uniqueUUID == base.uniqueUUID) {
                return entity
            }
        }
        return null
    }
}