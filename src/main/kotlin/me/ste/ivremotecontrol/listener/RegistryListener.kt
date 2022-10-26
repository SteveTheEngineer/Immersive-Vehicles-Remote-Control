package me.ste.ivremotecontrol.listener

import me.ste.ivremotecontrol.block.antenna.AntennaBlock
import me.ste.ivremotecontrol.block.iface.decorinterface.DecorInterfaceBlock
import me.ste.ivremotecontrol.block.iface.decorinterface.DecorInterfaceTileEntity
import me.ste.ivremotecontrol.block.iface.fluidloaderinterface.FluidLoaderInterfaceBlock
import me.ste.ivremotecontrol.block.iface.fluidloaderinterface.FluidLoaderInterfaceTileEntity
import me.ste.ivremotecontrol.block.iface.fuelpumpinterface.FuelPumpInterfaceBlock
import me.ste.ivremotecontrol.block.iface.fuelpumpinterface.FuelPumpInterfaceTileEntity
import me.ste.ivremotecontrol.block.iface.signalcontrollerinterface.SignalControllerInterfaceBlock
import me.ste.ivremotecontrol.block.iface.signalcontrollerinterface.SignalControllerInterfaceTileEntity
import me.ste.ivremotecontrol.block.iface.vehicleremoteinterface.VehicleRemoteInterfaceBlock
import me.ste.ivremotecontrol.block.iface.vehicleremoteinterface.VehicleRemoteInterfaceTileEntity
import me.ste.ivremotecontrol.item.BlockSelectorItem
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
                BlockSelectorItem,

                // Block items
                VehicleRemoteInterfaceBlock.ITEM,
                DecorInterfaceBlock.ITEM,
                FluidLoaderInterfaceBlock.ITEM,
                FuelPumpInterfaceBlock.ITEM,
                SignalControllerInterfaceBlock.ITEM,

                // Antenna
                AntennaBlock.BASIC.item,
                AntennaBlock.ADVANCED.item,
                AntennaBlock.ELITE.item,
                AntennaBlock.ULTIMATE.item,
                AntennaBlock.CREATIVE.item
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
                SignalControllerInterfaceBlock,

                // Antenna
                AntennaBlock.BASIC,
                AntennaBlock.ADVANCED,
                AntennaBlock.ELITE,
                AntennaBlock.ULTIMATE,
                AntennaBlock.CREATIVE
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
        this.addModelLocation(BlockSelectorItem)

        // Block items
        this.addModelLocation(VehicleRemoteInterfaceBlock.ITEM)
        this.addModelLocation(DecorInterfaceBlock.ITEM)
        this.addModelLocation(FluidLoaderInterfaceBlock.ITEM)
        this.addModelLocation(FuelPumpInterfaceBlock.ITEM)
        this.addModelLocation(SignalControllerInterfaceBlock.ITEM)

        // Antenna
        this.addModelLocation(AntennaBlock.BASIC.item)
        this.addModelLocation(AntennaBlock.ADVANCED.item)
        this.addModelLocation(AntennaBlock.ELITE.item)
        this.addModelLocation(AntennaBlock.ULTIMATE.item)
        this.addModelLocation(AntennaBlock.CREATIVE.item)
    }

    private fun addModelLocation(item: Item) =
        // Simplifies the addition of custom model resource locations for items with only one variant
        ModelLoader.setCustomModelResourceLocation(item, 0, ModelResourceLocation(item.registryName!!, "inventory"))
}