package me.ste.ivremotecontrol;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;

@Mod(modid = "ivremotecontrol", name = "IV Remote Control", version = "1.8", acceptedMinecraftVersions = "1.12.2", dependencies = "required-after:mts@19.14.0;required-after:opencomputers@[1.7.5.192,);")
public class IVRemoteControl {
    public static final short MIN_FLAPS_ANGLE = 0;
    public static final short AILERON_TRIM_STEP = 1;
    public static final short ELEVATOR_TRIM_STEP = 1;
    public static final short RUDDER_TRIM_STEP = 1;
    public static final short AILERON_ANGLE_STEP = 10;
    public static final short ELEVATOR_ANGLE_STEP = 10;
    public static final short RUDDER_ANGLE_STEP = 10;
    public static final short FLAPS_STEP = 50;
    public static final int SECOND_TICKS = 20;

    public static IVRemoteControl INSTANCE;

    public IVRemoteControl() {
        IVRemoteControl.INSTANCE = this;
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        NetworkRegistry.INSTANCE.registerGuiHandler(this, new ModGuiHandler());
    }
}