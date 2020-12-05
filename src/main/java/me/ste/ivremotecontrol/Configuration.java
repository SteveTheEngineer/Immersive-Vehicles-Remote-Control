package me.ste.ivremotecontrol;

import net.minecraftforge.common.config.Config;

@Config(modid = "ivremotecontrol")
public class Configuration {
    public enum VehicleLookupTarget {
        ENTITY,
        VEHICLE
    }

    @Config.Comment({
            "The target for the vehicle lookup when doing operations on it"
    })
    @Config.RequiresMcRestart
    @Config.Name("Vehicle Lookup Target")
    public static VehicleLookupTarget VEHICLE_LOOKUP_TARGET = VehicleLookupTarget.VEHICLE;
}