package me.ste.ivremotecontrol.block.fluidloaderinterface;

import li.cil.oc.api.Network;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.Connector;
import li.cil.oc.api.network.Visibility;
import li.cil.oc.api.prefab.TileEntityEnvironment;
import minecrafttransportsimulator.baseclasses.FluidTank;
import minecrafttransportsimulator.blocks.tileentities.components.ATileEntityBase;
import minecrafttransportsimulator.blocks.tileentities.instances.TileEntityFluidLoader;
import minecrafttransportsimulator.mcinterface.BuilderTileEntity;
import minecrafttransportsimulator.vehicles.main.EntityVehicleF_Physics;
import net.minecraft.block.BlockDirectional;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;

import java.lang.reflect.Field;

public class FluidLoaderInterfaceTileEntity extends TileEntityEnvironment implements ITickable {
    private boolean hasEnergy = false;

    public FluidLoaderInterfaceTileEntity() {
        this.node = Network.newNode(this, Visibility.Network).withConnector().withComponent("fluidloader").create();
    }

    @Override
    public void update() {
        if(this.node != null) {
            this.hasEnergy = ((Connector) this.node).tryChangeBuffer(-0.5D);
        }
    }

    public TileEntityFluidLoader getLoader() {
        if(!this.hasEnergy) {
            return null;
        }
        BlockPos loaderPos = this.pos.offset(this.world.getBlockState(this.pos).getValue(BlockDirectional.FACING));
        TileEntity tileEntity = this.world.getTileEntity(loaderPos);
        if(!(tileEntity instanceof BuilderTileEntity)) {
            return null;
        }
        ATileEntityBase<?> tileEntityBase;
        try {
            Field field = BuilderTileEntity.class.getDeclaredField("tileEntity");
            field.setAccessible(true);
            tileEntityBase = (ATileEntityBase<?>) field.get(tileEntity);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            return null;
        }
        if(!(tileEntityBase instanceof TileEntityFluidLoader)) {
            return null;
        }
        return (TileEntityFluidLoader) tileEntityBase;
    }

    @Callback(direct = true, doc = "function(): boolean -- check whether the fluid loader is available")
    public Object[] isAvailable(Context ctx, Arguments args) {
        return new Object[] {this.getLoader() != null};
    }

    @Callback(direct = true, doc = "function(): string -- get the loader mode. Can be either LOAD or UNLOAD")
    public Object[] getMode(Context ctx, Arguments args) {
        TileEntityFluidLoader loader = this.getLoader();
        if(loader != null) {
            return new Object[] {loader.unloadMode ? "UNLOAD" : "LOAD"};
        }
        return new Object[] {};
    }

    @Callback(doc = "function(mode:string) -- set the loader mode. Can be either LOAD or UNLOAD")
    public Object[] setMode(Context ctx, Arguments args) {
        TileEntityFluidLoader loader = this.getLoader();
        if(loader != null) {
            String mode = args.checkString(0);
            if(mode.equalsIgnoreCase("LOAD")) {
                loader.unloadMode = false;
            } else if(mode.equalsIgnoreCase("UNLOAD")) {
                loader.unloadMode = true;
            }
        }
        return new Object[] {};
    }

    @Callback(direct = true, doc = "function(): string,number,number -- get the loader fluid name, fluid level and max fluid level")
    public Object[] getFluid(Context ctx, Arguments args) {
        TileEntityFluidLoader loader = this.getLoader();
        if(loader != null) {
            FluidTank tank = loader.getTank();
            return new Object[] {tank.getFluid(), tank.getFluidLevel(), tank.getMaxLevel()};
        }
        return new Object[] {};
    }

    @Callback(direct = true, doc = "function(): boolean -- get whether a part is connected to the loader")
    public Object[] isConnected(Context ctx, Arguments args) {
        TileEntityFluidLoader loader = this.getLoader();
        if(loader != null) {
            return new Object[] {loader.connectedPart != null};
        }
        return new Object[] {};
    }

    @Callback(direct = true, doc = "function(): string,string -- get the entity and the vehicle uuid of the connected vehicle")
    public Object[] getConnectedVehicle(Context ctx, Arguments args) {
        TileEntityFluidLoader loader = this.getLoader();
        if(loader != null && loader.connectedPart != null) {
            EntityVehicleF_Physics vehicle = loader.connectedPart.vehicle;
            return new Object[] {vehicle.wrapper.entity.getUniqueID().toString(), vehicle.uniqueUUID};
        }
        return new Object[] {};
    }
}