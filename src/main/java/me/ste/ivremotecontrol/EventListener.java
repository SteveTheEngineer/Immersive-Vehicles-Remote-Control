package me.ste.ivremotecontrol;

import me.ste.ivremotecontrol.block.decorinterface.DecorInterfaceBlock;
import me.ste.ivremotecontrol.block.decorinterface.DecorInterfaceTileEntity;
import me.ste.ivremotecontrol.block.fluidloaderinterface.FluidLoaderInterfaceBlock;
import me.ste.ivremotecontrol.block.fluidloaderinterface.FluidLoaderInterfaceTileEntity;
import me.ste.ivremotecontrol.block.fuelpumpinterface.FuelPumpInterfaceBlock;
import me.ste.ivremotecontrol.block.fuelpumpinterface.FuelPumpInterfaceTileEntity;
import me.ste.ivremotecontrol.block.signalcontrollerinterface.SignalControllerInterfaceBlock;
import me.ste.ivremotecontrol.block.signalcontrollerinterface.SignalControllerInterfaceTileEntity;
import me.ste.ivremotecontrol.block.vehicleremotecontroller.VehicleRemoteControllerBlock;
import me.ste.ivremotecontrol.block.vehicleremotecontroller.VehicleRemoteControllerTileEntity;
import me.ste.ivremotecontrol.item.VehicleSelectorItem;
import minecrafttransportsimulator.mcinterface.BuilderEntity;
import minecrafttransportsimulator.vehicles.main.AEntityBase;
import minecrafttransportsimulator.vehicles.main.EntityVehicleF_Physics;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.server.permission.PermissionAPI;

import java.lang.reflect.Field;
import java.util.Objects;

@Mod.EventBusSubscriber
public class EventListener {
    @SubscribeEvent
    public static void onItemRegistry(RegistryEvent.Register<Item> event) {
        event.getRegistry().registerAll(
                VehicleSelectorItem.INSTANCE,
                VehicleRemoteControllerBlock.ITEM_INSTANCE,
                FuelPumpInterfaceBlock.ITEM_INSTANCE,
                FluidLoaderInterfaceBlock.ITEM_INSTANCE,
                SignalControllerInterfaceBlock.ITEM_INSTANCE,
                DecorInterfaceBlock.ITEM_INSTANCE
        );
    }

    @SubscribeEvent
    public static void onBlockRegistry(RegistryEvent.Register<Block> event) {
        event.getRegistry().registerAll(
                VehicleRemoteControllerBlock.INSTANCE,
                FuelPumpInterfaceBlock.INSTANCE,
                FluidLoaderInterfaceBlock.INSTANCE,
                SignalControllerInterfaceBlock.INSTANCE,
                DecorInterfaceBlock.INSTANCE
        );
        GameRegistry.registerTileEntity(VehicleRemoteControllerTileEntity.class, new ResourceLocation("ivremotecontrol", "vehicle_remote_controller"));
        GameRegistry.registerTileEntity(FuelPumpInterfaceTileEntity.class, new ResourceLocation("ivremotecontrol", "fuel_pump_interface"));
        GameRegistry.registerTileEntity(FluidLoaderInterfaceTileEntity.class, new ResourceLocation("ivremotecontrol", "fluid_loader_interface"));
        GameRegistry.registerTileEntity(SignalControllerInterfaceTileEntity.class, new ResourceLocation("ivremotecontrol", "signal_controller_interface"));
        GameRegistry.registerTileEntity(DecorInterfaceTileEntity.class, new ResourceLocation("ivremotecontrol", "decor_interface"));
    }

    @SubscribeEvent
    public static void onModelRegistry(ModelRegistryEvent event) {
        ModelLoader.setCustomModelResourceLocation(VehicleSelectorItem.INSTANCE, 0, new ModelResourceLocation(Objects.requireNonNull(VehicleSelectorItem.INSTANCE.getRegistryName()), "inventory"));
        ModelLoader.setCustomModelResourceLocation(VehicleRemoteControllerBlock.ITEM_INSTANCE, 0, new ModelResourceLocation(Objects.requireNonNull(VehicleRemoteControllerBlock.ITEM_INSTANCE.getRegistryName()), "inventory"));
        ModelLoader.setCustomModelResourceLocation(FuelPumpInterfaceBlock.ITEM_INSTANCE, 0, new ModelResourceLocation(Objects.requireNonNull(FuelPumpInterfaceBlock.ITEM_INSTANCE.getRegistryName()), "inventory"));
        ModelLoader.setCustomModelResourceLocation(FluidLoaderInterfaceBlock.ITEM_INSTANCE, 0, new ModelResourceLocation(Objects.requireNonNull(FluidLoaderInterfaceBlock.ITEM_INSTANCE.getRegistryName()), "inventory"));
        ModelLoader.setCustomModelResourceLocation(SignalControllerInterfaceBlock.ITEM_INSTANCE, 0, new ModelResourceLocation(Objects.requireNonNull(SignalControllerInterfaceBlock.ITEM_INSTANCE.getRegistryName()), "inventory"));
        ModelLoader.setCustomModelResourceLocation(DecorInterfaceBlock.ITEM_INSTANCE, 0, new ModelResourceLocation(Objects.requireNonNull(DecorInterfaceBlock.ITEM_INSTANCE.getRegistryName()), "inventory"));
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if(!event.getWorld().isRemote && event.getEntityPlayer().isSneaking() && event.getItemStack().getItem() instanceof VehicleSelectorItem && event.getItemStack().hasTagCompound()) {
            event.getEntityPlayer().sendMessage(new TextComponentTranslation("chat.ivremotecontrol:vehicle_selector.deselected"));
            event.getItemStack().setTagCompound(null);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if(!event.getWorld().isRemote && event.getItemStack().getItem() instanceof VehicleSelectorItem && event.getTarget() instanceof BuilderEntity) {
            try {
                Field entity = BuilderEntity.class.getDeclaredField("entity");
                entity.setAccessible(true);
                AEntityBase entityVehicle = (AEntityBase) entity.get(event.getTarget());
                if (entityVehicle instanceof EntityVehicleF_Physics) {
                    EntityVehicleF_Physics vehicle = (EntityVehicleF_Physics) entityVehicle;

                    if(PermissionAPI.hasPermission(event.getEntityPlayer(), "ivremotecontrol.force") || vehicle.ownerUUID.length() <= 0 || vehicle.ownerUUID.equals(event.getEntityPlayer().getUniqueID().toString())) {
                        NBTTagCompound compound = event.getItemStack().hasTagCompound() ? event.getItemStack().getTagCompound() : new NBTTagCompound();
                        assert compound != null;

                        compound.setString("EntityUUID", event.getTarget().getUniqueID().toString());
                        compound.setString("VehicleUUID", vehicle.uniqueUUID);

                        event.getEntityPlayer().sendMessage(new TextComponentTranslation("chat.ivremotecontrol:vehicle_selector.selected"));
                        event.getItemStack().setTagCompound(compound);
                        event.setCancellationResult(EnumActionResult.SUCCESS);
                    } else {
                        event.getEntityPlayer().sendMessage(new TextComponentTranslation("chat.ivremotecontrol:vehicle_selector.select_failed"));
                        event.setCancellationResult(EnumActionResult.FAIL);
                    }

                    event.setCanceled(true);
                }
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }
}