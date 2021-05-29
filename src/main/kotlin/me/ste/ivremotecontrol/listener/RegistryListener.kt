package me.ste.ivremotecontrol.listener

import me.ste.ivremotecontrol.block.decorinterface.DecorInterfaceBlock
import me.ste.ivremotecontrol.block.decorinterface.DecorInterfaceTileEntity
import me.ste.ivremotecontrol.block.fluidloaderinterface.FluidLoaderInterfaceBlock
import me.ste.ivremotecontrol.block.fluidloaderinterface.FluidLoaderInterfaceTileEntity
import me.ste.ivremotecontrol.block.fuelpumpinterface.FuelPumpInterfaceBlock
import me.ste.ivremotecontrol.block.fuelpumpinterface.FuelPumpInterfaceTileEntity
import me.ste.ivremotecontrol.block.signalcontrollerinterface.SignalControllerInterfaceBlock
import me.ste.ivremotecontrol.block.signalcontrollerinterface.SignalControllerInterfaceTileEntity
import me.ste.ivremotecontrol.block.vehicleremoteinterface.VehicleRemoteInterfaceBlock
import me.ste.ivremotecontrol.block.vehicleremoteinterface.VehicleRemoteInterfaceTileEntity
import me.ste.ivremotecontrol.item.VehicleSelectorItem
import net.minecraft.block.Block
import net.minecraft.client.renderer.block.model.ModelResourceLocation
import net.minecraft.item.Item
import net.minecraftforge.client.event.ModelRegistryEvent
import net.minecraftforge.client.model.ModelLoader
import net.minecraftforge.event.RegistryEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.registry.GameRegistry

object RegistryListener {
    @SubscribeEvent
    fun onItemRegistry(event: RegistryEvent.Register<Item>) {
        // Register the mod's items
        event.registry.registerAll(
            // Regular items
            VehicleSelectorItem,

            // Block items
            VehicleRemoteInterfaceBlock.ITEM,
            DecorInterfaceBlock.ITEM,
            FluidLoaderInterfaceBlock.ITEM,
            FuelPumpInterfaceBlock.ITEM,
            SignalControllerInterfaceBlock.ITEM
        )
    }

    @SubscribeEvent
    fun onBlockRegistry(event: RegistryEvent.Register<Block>) {
        // Register the mod's blocks
        event.registry.registerAll(
            VehicleRemoteInterfaceBlock,
            DecorInterfaceBlock,
            FluidLoaderInterfaceBlock,
            FuelPumpInterfaceBlock,
            SignalControllerInterfaceBlock
        )

        // Register the tile entities
        GameRegistry.registerTileEntity(
            VehicleRemoteInterfaceTileEntity::class.java,
            VehicleRemoteInterfaceBlock.registryName
        )
        GameRegistry.registerTileEntity(
            DecorInterfaceTileEntity::class.java,
            DecorInterfaceBlock.registryName
        )
        GameRegistry.registerTileEntity(
            FluidLoaderInterfaceTileEntity::class.java,
            FluidLoaderInterfaceBlock.registryName
        )
        GameRegistry.registerTileEntity(
            FuelPumpInterfaceTileEntity::class.java,
            FuelPumpInterfaceBlock.registryName
        )
        GameRegistry.registerTileEntity(
            SignalControllerInterfaceTileEntity::class.java,
            SignalControllerInterfaceBlock.registryName
        )
    }

    @SubscribeEvent
    fun onModelRegistry(event: ModelRegistryEvent) {
        // Regular items
        this.addModelLocation(VehicleSelectorItem)

        // Block items
        this.addModelLocation(VehicleRemoteInterfaceBlock.ITEM)
        this.addModelLocation(DecorInterfaceBlock.ITEM)
        this.addModelLocation(FluidLoaderInterfaceBlock.ITEM)
        this.addModelLocation(FuelPumpInterfaceBlock.ITEM)
        this.addModelLocation(SignalControllerInterfaceBlock.ITEM)
    }

    private fun addModelLocation(item: Item) =
        // Simplifies the addition of custom model resource locations for items with only one variant
        ModelLoader.setCustomModelResourceLocation(item, 0, ModelResourceLocation(item.registryName!!, "inventory"))
}