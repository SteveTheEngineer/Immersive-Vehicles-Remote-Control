package me.ste.ivremotecontrol;

import me.ste.ivremotecontrol.block.vehicleremotecontroller.VehicleRemoteControllerContainer;
import me.ste.ivremotecontrol.block.vehicleremotecontroller.VehicleRemoteControllerGUI;
import me.ste.ivremotecontrol.block.vehicleremotecontroller.VehicleRemoteControllerTileEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class ModGuiHandler implements IGuiHandler {
    public static final int VEHICLE_REMOTE_CONTROLLER = 0;

    @Override
    public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        switch(id) {
            case ModGuiHandler.VEHICLE_REMOTE_CONTROLLER:
                return new VehicleRemoteControllerContainer(player.inventory, (VehicleRemoteControllerTileEntity) world.getTileEntity(new BlockPos(x, y, z)));
            default:
                return null;
        }
    }

    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        switch(id) {
            case ModGuiHandler.VEHICLE_REMOTE_CONTROLLER:
                return new VehicleRemoteControllerGUI((Container) this.getServerGuiElement(id, player, world, x, y, z), player.inventory);
            default:
                return null;
        }
    }
}