package me.ste.ivremotecontrol;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.Config.Comment;
import net.minecraftforge.common.config.Config.Name;
import net.minecraftforge.common.config.Config.RangeDouble;

@Config(modid = IVRemoteControl.MOD_ID)
public final class IVRCConfiguration {
    private IVRCConfiguration() {}

    public enum LookupValue {
        ENTITY,
        VEHICLE
    }

    @Comment({
            "The value to identify the vehicles by.",
            "ENTITY means the vehicle will be looked up by it's Entity UUID, which invalidates when the vehicle gets picked up and placed back.",
            "VEHICLE means the vehicle will be looked up by MTS' internal entity ID, which persists in the vehicle's item form.",
            "Leave this on VEHICLE if you are not sure!"
    })
    @Name("Vehicle lookup value")
    public static LookupValue LOOKUP_VALUE = LookupValue.VEHICLE;

    @Comment({
            "If set to true, all vehicles will be able to be remote controlled,",
            "no matter whether they have an antenna or not."
    })
    @Name("Allow to control any vehicle")
    public static boolean BYPASS_ANTENNA_CHECK = false;

    @Comment(
            "The range of the basic antenna. Set to -1 for infinite range (even across dimensions)."
    )
    @RangeDouble(min = -1.0)
    @Name("Basic antenna range")
    public static double BASIC_ANTENNA_RANGE = 64.0;

    @Comment(
            "The range of the advanced antenna. Set to -1 for infinite range (even across dimensions)."
    )
    @RangeDouble(min = -1.0)
    @Name("Advanced antenna range")
    public static double ADVANCED_ANTENNA_RANGE = 1024.0;

    @Comment(
            "The range of the elite antenna. Set to -1 for infinite range (even across dimensions)."
    )
    @RangeDouble(min = -1.0)
    @Name("Elite antenna range")
    public static double ELITE_ANTENNA_RANGE = 32768.0;

    @Comment(
            "The range of the ultimate antenna. Set to -1 for infinite range (even across dimensions)."
    )
    @RangeDouble(min = -1.0)
    @Name("Ultimate antenna range")
    public static double ULTIMATE_ANTENNA_RANGE = 262144.0;

    @Comment(
            "The range of the creative antenna. Set to -1 for infinite range (even across dimensions)."
    )
    @RangeDouble(min = -1.0)
    @Name("Creative antenna range")
    public static double CREATIVE_ANTENNA_RANGE = -1.0;
}
