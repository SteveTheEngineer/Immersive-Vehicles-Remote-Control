package me.ste.ivremotecontrol

import com.google.gson.JsonParser
import dan200.computercraft.api.ComputerCraftAPI
import me.ste.ivremotecontrol.block.iface.base.InterfaceBlock
import me.ste.ivremotecontrol.listener.ItemListener
import me.ste.ivremotecontrol.listener.RegistryListener
import minecrafttransportsimulator.packloading.PackParser
import net.minecraft.util.ResourceLocation
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.Loader
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import net.minecraftforge.fml.common.network.NetworkRegistry
import org.apache.logging.log4j.LogManager
import java.io.File
import java.util.jar.JarFile

@Mod(
    modid = IVRemoteControl.MOD_ID,
    dependencies = "required:forgelin@[1.8.4,);",
    modLanguageAdapter = "net.shadowfacts.forgelin.KotlinAdapter"
)
object IVRemoteControl {
    // Constants
    const val MOD_ID = "ivremotecontrol"
    const val MTS_VERSION = "22.6.0"
    const val FLAP_STEP: Short = 50

    // Mod support states
    var OPENCOMPUTERS_ENABLED = false
    var COMPUTERCRAFT_ENABLED = false

    // Collected metadata
    val ANTENNA_VEHICLES = mutableSetOf<String>()
    val ANTENNA_PARTS = mutableSetOf<String>()

    val LOGGER = LogManager.getLogger()

    @Mod.EventHandler
    fun preInit(event: FMLPreInitializationEvent) {
        // Make sure that the version of MTS is compatible
        val mtsModContainer = Loader.instance().indexedModList["mts"] ?: throw IllegalStateException("Immersive Vehicles (Minecraft Transport Simulator) is required for this mod to run. Please install SPECIFICALLY version $MTS_VERSION.")
        if (mtsModContainer.version != MTS_VERSION) {
            throw IllegalStateException("The currently installed Immersive Vehicles version ${mtsModContainer.version} is not compatible with the version of Immersive Vehicles Remote Control. Please use version $MTS_VERSION.")
        }

        // Make sure at least one supported computer mod is installed
        OPENCOMPUTERS_ENABLED = Loader.isModLoaded("opencomputers")
        COMPUTERCRAFT_ENABLED = Loader.isModLoaded("computercraft")

        if (!OPENCOMPUTERS_ENABLED && !COMPUTERCRAFT_ENABLED) {
            throw IllegalStateException("Either OpenComputers or ComputerCraft (CC: Tweaked) is required for this mod to run. Please install one (or both) of those and try again.")
        }

        // Register the GUI handler
        NetworkRegistry.INSTANCE.registerGuiHandler(this, IVRCGuiHandler)

        // Register the event listeners
        MinecraftForge.EVENT_BUS.register(RegistryListener)
        MinecraftForge.EVENT_BUS.register(ItemListener)

        // Register the peripheral provider
        if (COMPUTERCRAFT_ENABLED) {
            ComputerCraftAPI.registerPeripheralProvider { world, pos, side ->
                val state = world.getBlockState(pos)
                val block = state.block as? InterfaceBlock ?: return@registerPeripheralProvider null

                return@registerPeripheralProvider block.getPeripheral(state, world, pos, side)
            }
        }
    }

    @Mod.EventHandler
    fun postInit(event: FMLPostInitializationEvent) {
        // Get the pack map
        val field = PackParser::class.java.getDeclaredField("packJarMap")
        field.isAccessible = true
        val packJarMap = field.get(null) as Map<String, File>

        // Find manifests in packs
        for ((id, file) in packJarMap) {
            try {
                // Check for metadata
                val jarFile = JarFile(file)
                val entry = jarFile.entries().toList().find { it.name == "assets/$id/ivremotecontrol.json" } ?: continue
                LOGGER.debug("Found IVRC metadata in pack ID $id")

                // Parse metadata as JSON
                val stream = jarFile.getInputStream(entry)
                val obj = JsonParser().parse(stream.reader()).asJsonObject

                // Check the version and store metadata
                val version = obj.get("version").asInt
                if (version == 0) {
                    val parts = obj.getAsJsonArray("parts")
                    val vehicles = obj.getAsJsonArray("vehicles")

                    ANTENNA_PARTS += parts.map { it.asString }
                    ANTENNA_VEHICLES += vehicles.map { it.asString }
                } else {
                    LOGGER.error("Unsupported IVRC metadata version for pack ID $id: version $version. Your version of IVRC might be outdated")
                }
            } catch (t: Throwable) {
                LOGGER.error("An error has occurred while reading the IVRC metadata of pack ID $id", t)
            }
        }
    }

    fun resourceLocation(key: String) = ResourceLocation(MOD_ID, key) // A method simplifying the creation of mod resource locations
}