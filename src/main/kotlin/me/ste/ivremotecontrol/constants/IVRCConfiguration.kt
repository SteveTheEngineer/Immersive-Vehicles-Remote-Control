package me.ste.ivremotecontrol.constants

import me.ste.ivremotecontrol.IVRemoteControl
import net.minecraftforge.common.config.Config

@Config(modid = IVRemoteControl.MOD_ID)
object IVRCConfiguration {
    enum class LookupValue {
        ENTITY,
        VEHICLE
    }

    @Config.Comment(
        "The value to identify the vehicles by.",
        "ENTITY means the vehicle will be looked up by it's Entity UUID, which invalidates when the vehicle gets picked up and placed back.",
        "VEHICLE means the vehicle will be looked up by MTS' internal entity ID, which persists in the vehicle's item form."
    )
    @Config.RequiresMcRestart
    @Config.Name("Vehicle lookup value")
    @JvmField
    var LOOKUP_VALUE = LookupValue.VEHICLE
}