package me.ste.ivremotecontrol.block.fuelpumpinterface;

import li.cil.oc.api.Network;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.Connector;
import li.cil.oc.api.network.Visibility;
import li.cil.oc.api.prefab.TileEntityEnvironment;
import minecrafttransportsimulator.baseclasses.FluidTank;
import minecrafttransportsimulator.blocks.tileentities.components.ATileEntityBase;
import minecrafttransportsimulator.blocks.tileentities.instances.TileEntityFuelPump;
import minecrafttransportsimulator.mcinterface.BuilderEntity;
import minecrafttransportsimulator.mcinterface.BuilderTileEntity;
import minecrafttransportsimulator.mcinterface.WrapperEntity;
import minecrafttransportsimulator.packets.components.InterfacePacket;
import minecrafttransportsimulator.packets.instances.PacketTileEntityFuelPumpConnection;
import minecrafttransportsimulator.systems.ConfigSystem;
import minecrafttransportsimulator.entities.components.AEntityA_Base;
import minecrafttransportsimulator.entities.instances.EntityVehicleF_Physics;
import minecrafttransportsimulator.entities.instances.APart;
import minecrafttransportsimulator.entities.instances.PartEngine;
import net.minecraft.block.BlockDirectional;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;

import java.lang.reflect.Field;

public class FuelPumpInterfaceTileEntity extends TileEntityEnvironment implements ITickable {
    private boolean hasEnergy = false;

    public FuelPumpInterfaceTileEntity() {
        this.node = Network.newNode(this, Visibility.Network).withConnector().withComponent("fuelpump").create();
    }

    @Override
    public void update() {
        if(this.node != null) {
            this.hasEnergy = ((Connector) this.node).tryChangeBuffer(-0.5D);
        }
    }

    public TileEntityFuelPump getPump() {
        if(!this.hasEnergy) {
            return null;
        }
        BlockPos pumpPos = this.pos.offset(this.world.getBlockState(this.pos).getValue(BlockDirectional.FACING));
        TileEntity tileEntity = this.world.getTileEntity(pumpPos);
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
        if(!(tileEntityBase instanceof TileEntityFuelPump)) {
            return null;
        }
        return (TileEntityFuelPump) tileEntityBase;
    }

    @Callback(direct = true, doc = "function(): boolean -- check whether the pump is available")
    public Object[] isAvailable(Context ctx, Arguments args) {
        return new Object[] {this.getPump() != null};
    }

    @Callback(direct = true, doc = "function(): string,number,number,number,number,number -- get the fuel fluid name, fuel level, max fuel level, fuel explosiveness, fuel weight and the amount of fuel dispensed")
    public Object[] getFuel(Context ctx, Arguments args) {
        TileEntityFuelPump pump = this.getPump();
        if(pump != null) {
            FluidTank fuel = pump.getTank();
            return new Object[] {fuel.getFluid(), fuel.getFluidLevel(), fuel.getMaxLevel(), fuel.getExplosiveness(), fuel.getWeight(), fuel.getAmountDispensed()};
        }
        return new Object[] {};
    }

    @Callback(direct = true, doc = "function(): boolean -- check whether a vehicle is being fueled")
    public Object[] isFueling(Context ctx, Arguments args) {
        TileEntityFuelPump pump = this.getPump();
        if(pump != null) {
            return new Object[] {pump.connectedVehicle != null};
        }
        return new Object[] {};
    }

    @Callback(direct = true, doc = "function(): string,string -- get the entity and vehicle uuid of the vehicle being fueled")
    public Object[] getVehicle(Context ctx, Arguments args) {
        TileEntityFuelPump pump = this.getPump();
        if(pump != null) {
            EntityVehicleF_Physics vehicle = pump.connectedVehicle;
            if(vehicle != null) {
                for (Entity entity : pump.connectedVehicle.world.world.loadedEntityList) {
                    if (entity instanceof BuilderEntity && ((BuilderEntity) entity).entity.uniqueUUID.equals(vehicle.uniqueUUID)) {
                        return new Object[] {entity.getUniqueID().toString(), vehicle.uniqueUUID};
                    }
                }
                return new Object[] {vehicle.uniqueUUID};
            }
        }
        return new Object[] {};
    }

    @Callback(direct = true, doc = "function(): string,string -- get the entity and vehicle uuid of the nearest vehicle to the fuel pump, or nothing if no vehicle is near")
    public Object[] getNearestVehicle(Context ctx, Arguments args) {
        TileEntityFuelPump pump = this.getPump();
        if(pump != null) {
            EntityVehicleF_Physics vehicle = null;
            double distance = 16D;
            for(AEntityA_Base entity : AEntityA_Base.getEntities(pump.world)){
                if(!(entity instanceof EntityVehicleF_Physics)) {
                    continue;
                }
                double vehicleDistance = ((EntityVehicleF_Physics) entity).position.distanceTo(pump.position);
                if(vehicleDistance < distance) {
                    distance = vehicleDistance;
                    vehicle = (EntityVehicleF_Physics) entity;
                }
            }
            if(vehicle != null) {
                for (Entity entity : pump.connectedVehicle.world.world.loadedEntityList) {
                    if (entity instanceof BuilderEntity && ((BuilderEntity) entity).entity.uniqueUUID.equals(vehicle.uniqueUUID)) {
                        return new Object[] {entity.getUniqueID().toString(), vehicle.uniqueUUID};
                    }
                }
                return new Object[] {vehicle.uniqueUUID};
            }
        }
        return new Object[] {};
    }

    @Callback(doc = "function(): boolean,string -- start fueling the nearest vehicle. Returns true if success, or false and the error message otherwise")
    public Object[] start(Context ctx, Arguments args) {
        TileEntityFuelPump pump = this.getPump();
        if(pump != null) {
            if(pump.connectedVehicle == null) {
                EntityVehicleF_Physics vehicle = null;
                double distance = 16D;
                for(AEntityA_Base entity : AEntityA_Base.getEntities(pump.world)){
                    if(!(entity instanceof EntityVehicleF_Physics)) {
                        continue;
                    }
                    double vehicleDistance = ((EntityVehicleF_Physics) entity).position.distanceTo(pump.position);
                    if(vehicleDistance < distance){
                        distance = vehicleDistance;
                        vehicle = (EntityVehicleF_Physics) entity;
                    }
                }
                if(vehicle != null) {
                    if(pump.getTank().getFluidLevel() > 0) {
                        if(vehicle.fuelTank.getFluid().isEmpty() || vehicle.fuelTank.getFluid().equals(pump.getTank().getFluid())) {
                            for(APart part : vehicle.parts) {
                                if(part instanceof PartEngine && ConfigSystem.configObject.fuel.fuels.get(part.definition.engine.fuelType).containsKey(pump.getTank().getFluid())) {
                                    pump.connectedVehicle = vehicle;
                                    vehicle.beingFueled = true;
                                    pump.getTank().resetAmountDispensed();
                                    InterfacePacket.sendToAllClients(new PacketTileEntityFuelPumpConnection(pump, true));
                                    return new Object[] {true};
                                }
                            }
                            return new Object[] {false, "The fuel is not accepted by any of the vehicle engines"};
                        } else {
                            return new Object[] {false, "Fuel fluids cannot be mixed"};
                        }
                    } else {
                        return new Object[] {false, "No fuel in the pump"};
                    }
                } else {
                    return new Object[] {false, "No vehicle nearby"};
                }
            } else {
                return new Object[] {false, "A vehicle is already being fueled"};
            }
        }
        return new Object[] {};
    }

    @Callback(doc = "function() -- stop fueling the vehicle")
    public Object[] stop(Context ctx, Arguments args) {
        TileEntityFuelPump pump = this.getPump();
        if(pump != null && pump.connectedVehicle != null) {
            InterfacePacket.sendToAllClients(new PacketTileEntityFuelPumpConnection(pump, false));
            pump.connectedVehicle.beingFueled = false;
            pump.connectedVehicle = null;
        }
        return new Object[] {};
    }
}