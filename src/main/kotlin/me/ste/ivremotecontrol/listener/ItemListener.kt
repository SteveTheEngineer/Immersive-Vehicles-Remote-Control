package me.ste.ivremotecontrol.listener

import mcinterface1122.BuilderEntityExisting
import me.ste.ivremotecontrol.item.VehicleSelectorItem
import me.ste.ivremotecontrol.util.mtsEntity
import minecrafttransportsimulator.entities.components.AEntityA_Base
import minecrafttransportsimulator.entities.components.AEntityB_Existing
import minecrafttransportsimulator.entities.instances.EntityVehicleF_Physics
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumActionResult
import net.minecraftforge.event.entity.player.PlayerInteractEvent
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.server.permission.PermissionAPI
import kotlin.reflect.full.memberProperties

@Mod.EventBusSubscriber
object ItemListener {
    @SubscribeEvent(priority = EventPriority.HIGH)
    fun onEntityInteract(event: PlayerInteractEvent.EntityInteract) {
        // Handle the interactions with vehicles
        if (event.itemStack.item is VehicleSelectorItem && event.target is BuilderEntityExisting) { // Check whether the item is a selector item, and the entity is an MTS entity
            val entity = (event.target as BuilderEntityExisting).mtsEntity
            if (entity is EntityVehicleF_Physics) { // Check whether the entity is a vehicle
                if (!event.world.isRemote) { // Continue with binding the vehicle only if we're running the code at the server side
                    if (PermissionAPI.hasPermission( // Check whether the player has ivremotecontrol.force permission.
                            event.entityPlayer,
                            "ivremotecontrol.force"
                        ) || entity.ownerUUID == null || entity.ownerUUID == event.entityPlayer.uniqueID
                    // If not, check whether the vehicle's owned and whether the interacting player is it's owner
                    ) {
                        event.itemStack.tagCompound = (event.itemStack.tagCompound ?: NBTTagCompound()).apply {
                            this.setString("EntityUUID", event.target.uniqueID.toString())
                            this.setString("VehicleUUID", entity.uniqueUUID.toString())
                        } // Change the item's NBT data

                        event.cancellationResult = EnumActionResult.SUCCESS
                    } else {
                        event.cancellationResult = EnumActionResult.FAIL
                    }
                } else {
                    // Swing the arm if we're on the client
                    event.entityPlayer.swingArm(event.hand)
                }
            }
            event.isCanceled = true // Always cancel the event, no matter whether it's run on the client or the server
        }
    }
}