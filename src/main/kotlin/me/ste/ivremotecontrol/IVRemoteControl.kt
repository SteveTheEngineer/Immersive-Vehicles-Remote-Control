package me.ste.ivremotecontrol

import dan200.computercraft.api.ComputerCraftAPI
import me.ste.ivremotecontrol.block.peripheral.PeripheralTileEntity
import me.ste.ivremotecontrol.listener.ItemListener
import me.ste.ivremotecontrol.listener.RegistryListener
import net.minecraft.util.ResourceLocation
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import net.minecraftforge.fml.common.network.NetworkRegistry

@Mod(
    modid = IVRemoteControl.MOD_ID,
    name = "IV Remote Control",
    version = "2.4.1",
    dependencies = "required-after:computercraft@[1.89.2,);required-after:forgelin@[1.8.4,);required-after:mts@20.6.0;",
    modLanguageAdapter = "net.shadowfacts.forgelin.KotlinAdapter"
)
object IVRemoteControl {
    const val MOD_ID = "ivremotecontrol"

    @Mod.EventHandler
    fun preInit(event: FMLPreInitializationEvent) {
        // Register the GUI handler
        NetworkRegistry.INSTANCE.registerGuiHandler(this, IVRCGuiHandler)

        // Register the event listeners
        MinecraftForge.EVENT_BUS.register(RegistryListener)
        MinecraftForge.EVENT_BUS.register(ItemListener)

        // Register the peripheral provider
        ComputerCraftAPI.registerPeripheralProvider { world, pos, _ ->
            val tileEntity = world.getTileEntity(pos)
            if (tileEntity is PeripheralTileEntity) { // Check if the tile entity belongs to IVRC and return it
                return@registerPeripheralProvider tileEntity
            }
            return@registerPeripheralProvider null
        }
    }

    fun resourceLocation(key: String) =
        ResourceLocation(MOD_ID, key) // A method simplifying the creation of resource locations of the mod
}