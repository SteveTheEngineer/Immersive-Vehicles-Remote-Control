package me.ste.ivremotecontrol.block.vehicleremotecontroller;

import com.google.gson.*;
import li.cil.oc.api.Network;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.Connector;
import li.cil.oc.api.network.Visibility;
import li.cil.oc.api.prefab.TileEntityEnvironment;
import me.ste.ivremotecontrol.Configuration;
import me.ste.ivremotecontrol.IVRemoteControl;
import minecrafttransportsimulator.baseclasses.BeaconManager;
import minecrafttransportsimulator.baseclasses.FluidTank;
import minecrafttransportsimulator.jsondefs.JSONText;
import minecrafttransportsimulator.jsondefs.JSONVehicle;
import minecrafttransportsimulator.mcinterface.BuilderEntity;
import minecrafttransportsimulator.mcinterface.WrapperEntity;
import minecrafttransportsimulator.packets.components.InterfacePacket;
import minecrafttransportsimulator.packets.instances.*;
import minecrafttransportsimulator.rendering.components.LightType;
import minecrafttransportsimulator.vehicles.main.AEntityBase;
import minecrafttransportsimulator.vehicles.main.EntityVehicleF_Physics;
import minecrafttransportsimulator.vehicles.parts.APart;
import minecrafttransportsimulator.vehicles.parts.PartEngine;
import minecrafttransportsimulator.vehicles.parts.PartGroundDevice;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.util.*;

public class VehicleRemoteControllerTileEntity extends TileEntityEnvironment implements ITickable {
    private final ItemStackHandler inventory = new ItemStackHandler(1);
    private boolean hasEnergy = false;

    public VehicleRemoteControllerTileEntity() {
        this.node = Network.newNode(this, Visibility.Network).withConnector().withComponent("vehicleremote").create();
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setTag("Inventory", inventory.serializeNBT());
        return super.writeToNBT(compound);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        inventory.deserializeNBT(compound.getCompoundTag("Inventory"));
        super.readFromNBT(compound);
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY ? (T) inventory : super.getCapability(capability, facing);
    }

    @Override
    public void update() {
        if(this.node != null) {
            this.hasEnergy = ((Connector) this.node).tryChangeBuffer(-0.5D);
        }
    }

    public EntityVehicleF_Physics getVehicle() {
        if(!this.hasEnergy) {
            return null;
        }
        if(this.inventory.getStackInSlot(0).isEmpty()) {
            return null;
        }
        ItemStack stack = this.inventory.getStackInSlot(0);
        if(!stack.hasTagCompound()) {
            return null;
        }
        NBTTagCompound compound = stack.getTagCompound();
        if(!compound.hasKey("EntityUUID") || !compound.hasKey("VehicleUUID")) {
            return null;
        }
        if(Configuration.VEHICLE_LOOKUP_TARGET == Configuration.VehicleLookupTarget.ENTITY) {
            for(Entity entity : this.getWorld().loadedEntityList) {
                if(entity.getUniqueID().toString().equals(compound.getString("EntityUUID"))) {
                    try {
                        Field entityField = BuilderEntity.class.getDeclaredField("entity");
                        entityField.setAccessible(true);
                        return (EntityVehicleF_Physics) entityField.get(entity);
                    } catch (NoSuchFieldException | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        } else if(Configuration.VEHICLE_LOOKUP_TARGET == Configuration.VehicleLookupTarget.VEHICLE) {
            for(Entity entity : this.getWorld().loadedEntityList) {
                if(entity instanceof BuilderEntity) {
                    try {
                        Field entityField = BuilderEntity.class.getDeclaredField("entity");
                        entityField.setAccessible(true);
                        EntityVehicleF_Physics vehicle = (EntityVehicleF_Physics) entityField.get(entity);
                        if(vehicle.uniqueUUID.equals(compound.getString("VehicleUUID"))) {
                            return vehicle;
                        }
                    } catch (NoSuchFieldException | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return null;
    }

    @Callback(direct = true, doc = "function(): boolean -- get whether the vehicle is available")
    public Object[] isAvailable(Context ctx, Arguments args) {
        return new Object[] {this.getVehicle() != null};
    }

    @Callback(direct = true, doc = "function(): number,number,number -- get the vehicle x, y and z coordinates")
    public Object[] getPosition(Context ctx, Arguments args) {
        EntityVehicleF_Physics vehicle = this.getVehicle();
        if(vehicle != null) {
            return new Object[] {vehicle.position.x, vehicle.position.y, vehicle.position.z};
        }
        return new Object[] {};
    }

    @Callback(direct = true, doc = "function(): number,number,number,number -- get the vehicle x, y, z and average velocity")
    public Object[] getVelocity(Context ctx, Arguments args) {
        EntityVehicleF_Physics vehicle = this.getVehicle();
        if(vehicle != null) {
            return new Object[] {vehicle.motion.x, vehicle.motion.y, vehicle.motion.z, vehicle.velocity};
        }
        return new Object[] {};
    }

    @Callback(direct = true, doc = "function(): number,number,number -- get the vehicle yaw, pitch and roll")
    public Object[] getRotation(Context ctx, Arguments args) {
        EntityVehicleF_Physics vehicle = this.getVehicle();
        if(vehicle != null) {
            return new Object[] {vehicle.angles.y, vehicle.angles.x, vehicle.angles.z};
        }
        return new Object[] {};
    }

    @Callback(direct = true, doc = "function(): string,string -- get the entity and the vehicle uuid")
    public Object[] getUUID(Context ctx, Arguments args) {
        EntityVehicleF_Physics vehicle = this.getVehicle();
        if(vehicle != null) {
            return new Object[] {this.inventory.getStackInSlot(0).getTagCompound().getString("EntityUUID"), vehicle.uniqueUUID};
        }
        return new Object[] {};
    }

    @Callback(direct = true, doc = "function(): string,number,number,number,number,boolean -- get the vehicle fuel fluid name, fuel level, max fuel level, fuel explosiveness, fuel weight and whether the vehicle is being fueled")
    public Object[] getFuel(Context ctx, Arguments args) {
        EntityVehicleF_Physics vehicle = this.getVehicle();
        if(vehicle != null) {
            FluidTank fuel = vehicle.fuelTank;
            return new Object[] {fuel.getFluid(), fuel.getFluidLevel(), fuel.getMaxLevel(), fuel.getExplosiveness(), fuel.getWeight(), vehicle.beingFueled};
        }
        return new Object[] {};
    }

    @Callback(direct = true, doc = "function(): table -- get the vehicle definition")
    public Object[] getDefinition(Context ctx, Arguments args) {
        EntityVehicleF_Physics vehicle = this.getVehicle();
        if(vehicle != null) {
            return new Object[] {VehicleRemoteControllerTileEntity.jsonToTree(new GsonBuilder().create().toJsonTree(vehicle.definition))};
        }
        return new Object[] {};
    }

    @Callback(direct = true, doc = "function(): string -- get the owner uuid")
    public Object[] getOwner(Context ctx, Arguments args) {
        EntityVehicleF_Physics vehicle = this.getVehicle();
        if(vehicle != null) {
            return new Object[] {vehicle.ownerUUID};
        }
        return new Object[] {};
    }

    @Callback(direct = true, doc = "function(): number -- get the vehicle current mass")
    public Object[] getMass(Context ctx, Arguments args) {
        EntityVehicleF_Physics vehicle = this.getVehicle();
        if(vehicle != null) {
            return new Object[] {vehicle.currentMass, vehicle.definition.general.emptyMass};
        }
        return new Object[] {};
    }

    @Callback(direct = true, doc = "function(): table -- get the vehicle engines")
    public Object[] getEngines(Context ctx, Arguments args) {
        EntityVehicleF_Physics vehicle = this.getVehicle();
        if(vehicle != null) {
            Map<Integer, Map<String, Object>> engines = new HashMap<>();
            for(Map.Entry<Byte, PartEngine> engine : vehicle.engines.entrySet()) {
                PartEngine part = engine.getValue();
                Map<String, Object> properties = new HashMap<>();

                properties.put("definition", VehicleRemoteControllerTileEntity.jsonToTree(new GsonBuilder().create().toJsonTree(part.definition)));
                properties.put("brokenStarter", part.brokenStarter);
                properties.put("currentGear", part.currentGear);
                properties.put("fuelFlow", part.fuelFlow);
                properties.put("fuelLeak", part.fuelLeak);
                properties.put("hours", part.hours);
                properties.put("creative", part.isCreative);
                properties.put("engineLinked", part.linkedEngine != null);
                properties.put("oilLeak", part.oilLeak);
                properties.put("pressure", part.pressure);
                properties.put("propellerGearboxRatio", part.propellerGearboxRatio);
                properties.put("reverseGears", part.reverseGears);
                properties.put("rpm", part.rpm);
                properties.put("position", new Object[] {part.worldPos.x, part.worldPos.y, part.worldPos.z});

                Map<String, Object> state = new HashMap<>();

                state.put("name", part.state.name());
                state.put("ordinal", part.state.ordinal());
                state.put("electricalStarter", part.state.esOn);
                state.put("handStarter", part.state.hsOn);
                state.put("magneto", part.state.magnetoOn);
                state.put("running", part.state.running);

                properties.put("state", state);

                engines.put(Integer.valueOf(engine.getKey()), properties);
            }
            return new Object[] {engines};
        }
        return new Object[] {};
    }

    @Callback(direct = true, doc = "function(): table -- get the vehicle wheels")
    public Object[] getWheels(Context ctx, Arguments args) {
        EntityVehicleF_Physics vehicle = this.getVehicle();
        if(vehicle != null) {
            Set<Object> wheels = new HashSet<>();
            for(PartGroundDevice wheel : vehicle.wheels) {
                Map<String, Object> properties = new HashMap<>();

                properties.put("definition", VehicleRemoteControllerTileEntity.jsonToTree(new GsonBuilder().create().toJsonTree(wheel.definition)));
                properties.put("angularPosition", wheel.angularPosition);
                properties.put("angularVelocity", wheel.angularVelocity);
                properties.put("skipAngularCalcs", wheel.skipAngularCalcs);
                properties.put("position", new Object[] {wheel.worldPos.x, wheel.worldPos.y, wheel.worldPos.z});

                wheels.add(properties);
            }
            return new Object[] {wheels};
        }
        return new Object[] {};
    }

    @Callback(direct = true, doc = "function(): table -- get the vehicle passengers")
    public Object[] getPassengers(Context ctx, Arguments args) {
        EntityVehicleF_Physics vehicle = this.getVehicle();
        if(vehicle != null) {
            List<Map<String, Object>> passengers = new ArrayList<>();
            for(WrapperEntity entity : vehicle.locationRiderMap.values()) {
                Map<String, Object> passenger = new HashMap<>();
                passenger.put("uuid", entity.entity.getUniqueID().toString());
                passenger.put("name", entity.entity.getName());
                passengers.add(passenger);
            }
            return new Object[] {passengers};
        }
        return new Object[] {};
    }

    @Callback(doc = "function(throttle:number) -- set the vehicle throttle. The throttle must be in range from 0 to 1")
    public Object[] setThrottle(Context ctx, Arguments args) {
        EntityVehicleF_Physics vehicle = this.getVehicle();
        if(vehicle != null) {
            vehicle.throttle = (byte) (Math.max(0, Math.min(1, args.checkDouble(0))) * (double) EntityVehicleF_Physics.MAX_THROTTLE);
            InterfacePacket.sendToAllClients(new PacketVehicleControlAnalog(vehicle, PacketVehicleControlAnalog.Controls.THROTTLE, vehicle.throttle, Byte.MAX_VALUE));
        }
        return new Object[] {};
    }

    @Callback(direct = true, doc = "function(): number -- get the vehicle throttle in range from 0 to 1")
    public Object[] getThrottle(Context ctx, Arguments args) {
        EntityVehicleF_Physics vehicle = this.getVehicle();
        if(vehicle != null) {
            return new Object[] {(double) vehicle.throttle / (double) EntityVehicleF_Physics.MAX_THROTTLE};
        }
        return new Object[] {};
    }

    @Callback(doc = "function(brake:number) -- set the vehicle's braking force in range from 0 to 1")
    public Object[] setBrake(Context ctx, Arguments args) {
        EntityVehicleF_Physics vehicle = this.getVehicle();
        if(vehicle != null) {
            vehicle.brake = (byte) (Math.max(0, Math.min(1, args.checkDouble(0))) * (double) EntityVehicleF_Physics.MAX_BRAKE);
            InterfacePacket.sendToAllClients(new PacketVehicleControlAnalog(vehicle, PacketVehicleControlAnalog.Controls.BRAKE, vehicle.brake, Byte.MAX_VALUE));
        }
        return new Object[] {};
    }

    @Callback(direct = true, doc = "function(): number -- get the vehicle braking force in range from 0 to 1")
    public Object[] getBrake(Context ctx, Arguments args) {
        EntityVehicleF_Physics vehicle = this.getVehicle();
        if(vehicle != null) {
            return new Object[] {(double) vehicle.brake / (double) EntityVehicleF_Physics.MAX_BRAKE};
        }
        return new Object[] {};
    }

    @Callback(doc = "function(parkingBrake:boolean) -- set the vehicle parking brake state")
    public Object[] setParkingBrake(Context ctx, Arguments args) {
        EntityVehicleF_Physics vehicle = this.getVehicle();
        if(vehicle != null) {
            vehicle.parkingBrakeOn = args.checkBoolean(0);
            InterfacePacket.sendToAllClients(new PacketVehicleControlDigital(vehicle, PacketVehicleControlDigital.Controls.P_BRAKE, vehicle.parkingBrakeOn));
        }
        return new Object[] {};
    }

    @Callback(direct = true, doc = "function(): boolean -- get the vehicle parking brake state")
    public Object[] getParkingBrake(Context ctx, Arguments args) {
        EntityVehicleF_Physics vehicle = this.getVehicle();
        if(vehicle != null) {
            return new Object[] {vehicle.parkingBrakeOn};
        }
        return new Object[] {};
    }

    @Callback(doc = "function(engineId:number,magneto:boolean) -- set an engine's magneto")
    public Object[] setMagneto(Context ctx, Arguments args) {
        EntityVehicleF_Physics vehicle = this.getVehicle();
        if(vehicle != null) {
            PartEngine engine = vehicle.engines.get((byte) Math.max(0, Math.min(Byte.MAX_VALUE, args.checkInteger(0))));
            if(engine != null) {
                boolean status = args.checkBoolean(1);
                engine.setMagnetoStatus(status);
                InterfacePacket.sendToAllClients(new PacketVehiclePartEngine(engine, status ? PacketVehiclePartEngine.Signal.MAGNETO_ON : PacketVehiclePartEngine.Signal.MAGNETO_OFF));
                if(engine.state.magnetoOn && engine.state.esOn) {
                    engine.setElectricStarterStatus(false);
                    InterfacePacket.sendToAllClients(new PacketVehiclePartEngine(engine, PacketVehiclePartEngine.Signal.ES_OFF));
                }
            }
        }
        return new Object[] {};
    }

    @Callback(doc = "function(engineId:number,starter:boolean) -- set an engine's electric starter")
    public Object[] setStarter(Context ctx, Arguments args) {
        EntityVehicleF_Physics vehicle = this.getVehicle();
        if(vehicle != null) {
            PartEngine engine = vehicle.engines.get((byte) Math.max(0, Math.min(Byte.MAX_VALUE, args.checkInteger(0))));
            if(engine != null) {
                if(engine.state.magnetoOn) {
                    boolean status = args.checkBoolean(1);
                    engine.setElectricStarterStatus(status);
                    InterfacePacket.sendToAllClients(new PacketVehiclePartEngine(engine, status ? PacketVehiclePartEngine.Signal.ES_ON : PacketVehiclePartEngine.Signal.ES_OFF));
                }
            }
        }
        return new Object[] {};
    }

    @Callback(doc = "function() -- shift the vehicle gear up")
    public Object[] shiftUp(Context ctx, Arguments args) {
        EntityVehicleF_Physics vehicle = this.getVehicle();
        if(vehicle != null) {
            boolean shifted = false;
            for(PartEngine engine : vehicle.engines.values()) {
                shifted = engine.shiftUp(false);
            }
            if(shifted) {
                InterfacePacket.sendToAllClients(new PacketVehicleControlDigital(vehicle, PacketVehicleControlDigital.Controls.SHIFT_UP, true));
            }
        }
        return new Object[] {};
    }

    @Callback(doc = "function() -- shift the vehicle gear down")
    public Object[] shiftDown(Context ctx, Arguments args) {
        EntityVehicleF_Physics vehicle = this.getVehicle();
        if(vehicle != null) {
            boolean shifted = false;
            for(PartEngine engine : vehicle.engines.values()) {
                shifted = engine.shiftDown(false);
            }
            if(shifted) {
                InterfacePacket.sendToAllClients(new PacketVehicleControlDigital(vehicle, PacketVehicleControlDigital.Controls.SHIFT_DN, true));
            }
        }
        return new Object[] {};
    }

    @Callback(doc = "function(name:string,state:boolean) -- set a vehicle's light state")
    public Object[] setLight(Context ctx, Arguments args) {
        EntityVehicleF_Physics vehicle = this.getVehicle();
        if(vehicle != null) {
            String name = args.checkString(0);
            boolean state = args.checkBoolean(1);
            try {
                LightType type = LightType.valueOf(name.toUpperCase());
                if (!vehicle.variablesOn.contains(type.lowercaseName) && state) {
                    vehicle.variablesOn.add(type.lowercaseName);
                    InterfacePacket.sendToAllClients(new PacketVehicleVariableToggle(vehicle, type.lowercaseName));
                } else if (vehicle.variablesOn.contains(type.lowercaseName) && !state) {
                    vehicle.variablesOn.remove(type.lowercaseName);
                    InterfacePacket.sendToAllClients(new PacketVehicleVariableToggle(vehicle, type.lowercaseName));
                }
            } catch(IllegalArgumentException ignored) {}
        }
        return new Object[] {};
    }

    @Callback(direct = true, doc = "function(name:string): boolean -- get a vehicle's light state")
    public Object[] getLight(Context ctx, Arguments args) {
        EntityVehicleF_Physics vehicle = this.getVehicle();
        if(vehicle != null) {
            try {
                return new Object[] {vehicle.variablesOn.contains(LightType.valueOf(args.checkString(0)).lowercaseName)};
            } catch(IllegalArgumentException ignored) {}
        }
        return new Object[] {};
    }

    @Callback(direct = true, doc = "function(): table -- get all vehicle's lights that are currently on")
    public Object[] getLightsOn(Context ctx, Arguments args) {
        EntityVehicleF_Physics vehicle = this.getVehicle();
        if(vehicle != null) {
            Set<String> lightsOn = new HashSet<>();
            for(LightType type : LightType.values()) {
                if(vehicle.variablesOn.contains(type.lowercaseName)) {
                    lightsOn.add(type.name());
                }
            }
            return new Object[] {lightsOn};
        }
        return new Object[] {};
    }

    @Callback(doc = "function(name:string,open:boolean) -- set a vehicle's door open state")
    public Object[] setDoor(Context ctx, Arguments args) {
        EntityVehicleF_Physics vehicle = this.getVehicle();
        if(vehicle != null) {
            String name = args.checkString(0);
            boolean open = args.checkBoolean(1);
            if(vehicle.definition.doors != null) {
                for(JSONVehicle.VehicleDoor door : vehicle.definition.doors) {
                    if(door.name.equals(name)) {
                        if (!vehicle.variablesOn.contains(name) && open) {
                            vehicle.variablesOn.add(name);
                            InterfacePacket.sendToAllClients(new PacketVehicleVariableToggle(vehicle, name));
                        } else if (vehicle.variablesOn.contains(name) && !open) {
                            vehicle.variablesOn.remove(name);
                            InterfacePacket.sendToAllClients(new PacketVehicleVariableToggle(vehicle, name));
                        }
                        break;
                    }
                }
            }
        }
        return new Object[] {};
    }

    @Callback(direct = true, doc = "function(name:string): boolean -- get whether a vehicle's door is open")
    public Object[] getDoor(Context ctx, Arguments args) {
        EntityVehicleF_Physics vehicle = this.getVehicle();
        if(vehicle != null) {
            try {
                for(JSONVehicle.VehicleDoor door : vehicle.definition.doors) {
                    if (door.name.equals(args.checkString(0))) {
                        return new Object[] {vehicle.variablesOn.contains(door.name)};
                    }
                }
            } catch(IllegalArgumentException ignored) {}
        }
        return new Object[] {};
    }

    @Callback(direct = true, doc = "function(): table -- get all vehicle's doors that are currently open")
    public Object[] getDoorsOpen(Context ctx, Arguments args) {
        EntityVehicleF_Physics vehicle = this.getVehicle();
        if(vehicle != null) {
            Set<String> doorsOpen = new HashSet<>();
            for(JSONVehicle.VehicleDoor door : vehicle.definition.doors) {
                if(vehicle.variablesOn.contains(door.name)) {
                    doorsOpen.add(door.name);
                }
            }
            return new Object[] {doorsOpen};
        }
        return new Object[] {};
    }

    @Callback(direct = true, doc = "function(): table -- get all doors that the vehicle has")
    public Object[] getAvailableDoors(Context ctx, Arguments args) {
        EntityVehicleF_Physics vehicle = this.getVehicle();
        if(vehicle != null) {
            Set<String> doors = new HashSet<>();
            for(JSONVehicle.VehicleDoor door : vehicle.definition.doors) {
                doors.add(door.name);
            }
            return new Object[] {doors.toArray(new String[0])};
        }
        return new Object[] {};
    }

    @Callback(doc = "function(cruiseControl:boolean) -- set the vehicle cruise control state")
    public Object[] setCruise(Context ctx, Arguments args) {
        EntityVehicleF_Physics vehicle = this.getVehicle();
        if(vehicle != null) {
            vehicle.cruiseControl = args.checkBoolean(0);
            if(vehicle.cruiseControl) {
                vehicle.cruiseControlSpeed = vehicle.velocity;
            }
            InterfacePacket.sendToAllClients(new PacketVehicleControlDigital(vehicle, PacketVehicleControlDigital.Controls.CRUISECONTROL, vehicle.cruiseControl));
        }
        return new Object[] {};
    }

    @Callback(direct = true, doc = "function(): boolean,number -- get the vehicle cruise control state and cruise control speed")
    public Object[] getCruise(Context ctx, Arguments args) {
        EntityVehicleF_Physics vehicle = this.getVehicle();
        if(vehicle != null) {
            return new Object[] {vehicle.cruiseControl, vehicle.cruiseControl ? vehicle.cruiseControlSpeed : 0};
        }
        return new Object[] {};
    }

    @Callback(doc = "function(horn:boolean) -- set the vehicle horn state")
    public Object[] setHorn(Context ctx, Arguments args) {
        EntityVehicleF_Physics vehicle = this.getVehicle();
        if(vehicle != null) {
            vehicle.hornOn = args.checkBoolean(0);
            InterfacePacket.sendToAllClients(new PacketVehicleControlDigital(vehicle, PacketVehicleControlDigital.Controls.HORN, vehicle.hornOn));
        }
        return new Object[] {};
    }

    @Callback(direct = true, doc = "function(): boolean -- get the vehicle horn state")
    public Object[] getHorn(Context ctx, Arguments args) {
        EntityVehicleF_Physics vehicle = this.getVehicle();
        if(vehicle != null) {
            return new Object[] {vehicle.hornOn};
        }
        return new Object[] {};
    }

    @Callback(doc = "function(siren:boolean) -- set the vehicle siren state")
    public Object[] setSiren(Context ctx, Arguments args) {
        EntityVehicleF_Physics vehicle = this.getVehicle();
        if(vehicle != null) {
            vehicle.sirenOn = args.checkBoolean(0);
            InterfacePacket.sendToAllClients(new PacketVehicleControlDigital(vehicle, PacketVehicleControlDigital.Controls.SIREN, vehicle.sirenOn));
        }
        return new Object[] {};
    }

    @Callback(direct = true, doc = "function(): boolean -- get the vehicle siren state")
    public Object[] getSiren(Context ctx, Arguments args) {
        EntityVehicleF_Physics vehicle = this.getVehicle();
        if(vehicle != null) {
            return new Object[] {vehicle.sirenOn};
        }
        return new Object[] {};
    }

    @Callback(doc = "function(reverse:boolean) -- set the vehicle reverse state")
    public Object[] setReverse(Context ctx, Arguments args) {
        EntityVehicleF_Physics vehicle = this.getVehicle();
        if(vehicle != null) {
            boolean state = args.checkBoolean(0);
            if(vehicle.definition.general.isBlimp) {
                for(PartEngine engine : vehicle.engines.values()) {
                    if(state) {
                        engine.shiftDown(false);
                        engine.shiftDown(false);
                    } else {
                        engine.shiftUp(false);
                        engine.shiftUp(false);
                    }
                }
            } else {
                vehicle.reverseThrust = state;
            }
            InterfacePacket.sendToAllClients(new PacketVehicleControlDigital(vehicle, PacketVehicleControlDigital.Controls.REVERSE, state));
        }
        return new Object[] {};
    }

    @Callback(direct = true, doc = "function(): boolean -- get the vehicle reverse thrust state")
    public Object[] getReverseThrust(Context ctx, Arguments args) {
        EntityVehicleF_Physics vehicle = this.getVehicle();
        if(vehicle != null) {
            return new Object[] {vehicle.reverseThrust};
        }
        return new Object[] {};
    }

    @Callback(doc = "function(autopilot:boolean) -- set the vehicle autopilot state")
    public Object[] setAutopilot(Context ctx, Arguments args) {
        EntityVehicleF_Physics vehicle = this.getVehicle();
        if(vehicle != null) {
            vehicle.autopilot = args.checkBoolean(0);
            vehicle.altitudeSetting = vehicle.position.y;
            InterfacePacket.sendToAllClients(new PacketVehicleControlDigital(vehicle, PacketVehicleControlDigital.Controls.AUTOPILOT, vehicle.autopilot));
        }
        return new Object[] {};
    }

    @Callback(direct = true, doc = "function(): boolean,number -- get the vehicle autopilot state and the target height")
    public Object[] getAutopilot(Context ctx, Arguments args) {
        EntityVehicleF_Physics vehicle = this.getVehicle();
        if(vehicle != null) {
            return new Object[] {vehicle.autopilot, vehicle.autopilot ? vehicle.altitudeSetting : 0};
        }
        return new Object[] {};
    }

    @Callback(doc = "function() -- increase the vehicle desired flap angle by 1")
    public Object[] increaseFlaps(Context ctx, Arguments args) {
        EntityVehicleF_Physics vehicle = this.getVehicle();
        if(vehicle != null) {
            vehicle.flapDesiredAngle = (short) Math.min(EntityVehicleF_Physics.MAX_FLAP_ANGLE, vehicle.flapDesiredAngle + IVRemoteControl.FLAPS_STEP);
            InterfacePacket.sendToAllClients(new PacketVehicleControlDigital(vehicle, PacketVehicleControlDigital.Controls.FLAPS, true));
        }
        return new Object[] {};
    }

    @Callback(doc = "function() -- decrease the vehicle desired flap angle by 1")
    public Object[] decreaseFlaps(Context ctx, Arguments args) {
        EntityVehicleF_Physics vehicle = this.getVehicle();
        if(vehicle != null) {
            vehicle.flapDesiredAngle = (short) Math.max(IVRemoteControl.MIN_FLAPS_ANGLE, vehicle.flapDesiredAngle - IVRemoteControl.FLAPS_STEP);
            InterfacePacket.sendToAllClients(new PacketVehicleControlDigital(vehicle, PacketVehicleControlDigital.Controls.FLAPS, true));
        }
        return new Object[] {};
    }

    @Callback(direct = true, doc = "function(): number,number -- get the vehicle flap angle and it's desired angle")
    public Object[] getFlaps(Context ctx, Arguments args) {
        EntityVehicleF_Physics vehicle = this.getVehicle();
        if(vehicle != null) {
            return new Object[] {(double) vehicle.flapCurrentAngle / (double) IVRemoteControl.FLAPS_STEP, (double) vehicle.flapDesiredAngle / (double) IVRemoteControl.FLAPS_STEP};
        }
        return new Object[] {};
    }

    @Callback(doc = "function() -- increase the vehicle aileron trim by 1")
    public Object[] increaseAileronTrim(Context ctx, Arguments args) {
        EntityVehicleF_Physics vehicle = this.getVehicle();
        if(vehicle != null) {
            vehicle.aileronTrim = (short) Math.min(EntityVehicleF_Physics.MAX_AILERON_TRIM, vehicle.aileronTrim + IVRemoteControl.AILERON_TRIM_STEP);
            InterfacePacket.sendToAllClients(new PacketVehicleControlDigital(vehicle, PacketVehicleControlDigital.Controls.TRIM_ROLL, true));
        }
        return new Object[] {};
    }

    @Callback(doc = "function() -- decrease the vehicle aileron trim by 1")
    public Object[] decreaseAileronTrim(Context ctx, Arguments args) {
        EntityVehicleF_Physics vehicle = this.getVehicle();
        if(vehicle != null) {
            vehicle.aileronTrim = (short) Math.max(-EntityVehicleF_Physics.MAX_AILERON_TRIM, vehicle.aileronTrim - IVRemoteControl.AILERON_TRIM_STEP);
            InterfacePacket.sendToAllClients(new PacketVehicleControlDigital(vehicle, PacketVehicleControlDigital.Controls.TRIM_ROLL, false));
        }
        return new Object[] {};
    }

    @Callback(doc = "function(angle:number,cooldown:number) -- set the vehicle aileron angle (degrees) and cooldown (seconds, 6.35 max)")
    public Object[] setAileron(Context ctx, Arguments args) {
        EntityVehicleF_Physics vehicle = this.getVehicle();
        if(vehicle != null) {
            short angle = (short) Math.max(-EntityVehicleF_Physics.MAX_AILERON_ANGLE, Math.min(EntityVehicleF_Physics.MAX_AILERON_ANGLE, args.checkDouble(0) * IVRemoteControl.AILERON_ANGLE_STEP));
            byte cooldown = (byte) Math.max(0, Math.min(Byte.MAX_VALUE, args.checkDouble(1) * IVRemoteControl.SECOND_TICKS));
            short oldAngle = vehicle.aileronAngle;
            vehicle.aileronAngle = angle;
            vehicle.aileronCooldown = cooldown;
            InterfacePacket.sendToAllClients(new PacketVehicleControlAnalog(vehicle, PacketVehicleControlAnalog.Controls.AILERON, vehicle.aileronCooldown != Byte.MAX_VALUE ? (short) (vehicle.aileronAngle - oldAngle) : vehicle.aileronAngle, vehicle.aileronCooldown));
        }
        return new Object[] {};
    }

    @Callback(direct = true, doc = "function(): number,number,number -- get the vehicle aileron angle (degrees), cooldown (seconds) and trim")
    public Object[] getAileron(Context ctx, Arguments args) {
        EntityVehicleF_Physics vehicle = this.getVehicle();
        if(vehicle != null) {
            return new Object[] {
                    (double) vehicle.aileronAngle / (double) IVRemoteControl.AILERON_ANGLE_STEP,
                    (double) vehicle.aileronCooldown / (double) IVRemoteControl.SECOND_TICKS,
                    (double) vehicle.aileronTrim / (double) IVRemoteControl.AILERON_TRIM_STEP
            };
        }
        return new Object[] {};
    }

    @Callback(doc = "function() -- increase the vehicle elevator trim by 1")
    public Object[] increaseElevatorTrim(Context ctx, Arguments args) {
        EntityVehicleF_Physics vehicle = this.getVehicle();
        if(vehicle != null) {
            vehicle.elevatorTrim = (short) Math.min(EntityVehicleF_Physics.MAX_ELEVATOR_TRIM, vehicle.elevatorTrim + IVRemoteControl.ELEVATOR_TRIM_STEP);
            InterfacePacket.sendToAllClients(new PacketVehicleControlDigital(vehicle, PacketVehicleControlDigital.Controls.TRIM_PITCH, true));
        }
        return new Object[] {};
    }

    @Callback(doc = "function() -- decrease the vehicle elevator trim by 1")
    public Object[] decreaseElevatorTrim(Context ctx, Arguments args) {
        EntityVehicleF_Physics vehicle = this.getVehicle();
        if(vehicle != null) {
            vehicle.elevatorTrim = (short) Math.max(-EntityVehicleF_Physics.MAX_ELEVATOR_TRIM, vehicle.elevatorTrim - IVRemoteControl.ELEVATOR_TRIM_STEP);
            InterfacePacket.sendToAllClients(new PacketVehicleControlDigital(vehicle, PacketVehicleControlDigital.Controls.TRIM_PITCH, false));
        }
        return new Object[] {};
    }

    @Callback(doc = "function(angle:number,cooldown:number) -- set the vehicle elevator angle (degrees) and cooldown (seconds, 6.35 max)")
    public Object[] setElevator(Context ctx, Arguments args) {
        EntityVehicleF_Physics vehicle = this.getVehicle();
        if(vehicle != null) {
            short angle = (short) Math.max(-EntityVehicleF_Physics.MAX_ELEVATOR_ANGLE, Math.min(EntityVehicleF_Physics.MAX_ELEVATOR_ANGLE, args.checkDouble(0) * IVRemoteControl.ELEVATOR_ANGLE_STEP));
            byte cooldown = (byte) Math.max(0, Math.min(Byte.MAX_VALUE, args.checkDouble(1) * IVRemoteControl.SECOND_TICKS));
            short oldAngle = vehicle.elevatorAngle;
            vehicle.elevatorAngle = angle;
            vehicle.elevatorCooldown = cooldown;
            InterfacePacket.sendToAllClients(new PacketVehicleControlAnalog(vehicle, PacketVehicleControlAnalog.Controls.ELEVATOR, vehicle.elevatorCooldown != Byte.MAX_VALUE ? (short) (vehicle.elevatorAngle - oldAngle) : vehicle.elevatorAngle, vehicle.elevatorCooldown));
        }
        return new Object[] {};
    }

    @Callback(direct = true, doc = "function(): number,number,number -- get the vehicle elevator angle (degrees), cooldown (seconds) and trim")
    public Object[] getElevator(Context ctx, Arguments args) {
        EntityVehicleF_Physics vehicle = this.getVehicle();
        if(vehicle != null) {
            return new Object[] {
                    (double) vehicle.elevatorAngle / (double) IVRemoteControl.ELEVATOR_ANGLE_STEP,
                    (double) vehicle.elevatorCooldown / (double) IVRemoteControl.SECOND_TICKS,
                    (double) vehicle.elevatorTrim / (double) IVRemoteControl.ELEVATOR_TRIM_STEP
            };
        }
        return new Object[] {};
    }

    @Callback(doc = "function() -- increase the vehicle rudder trim by 1")
    public Object[] increaseRudderTrim(Context ctx, Arguments args) {
        EntityVehicleF_Physics vehicle = this.getVehicle();
        if(vehicle != null) {
            vehicle.rudderTrim = (short) Math.min(EntityVehicleF_Physics.MAX_RUDDER_TRIM, vehicle.rudderTrim + IVRemoteControl.RUDDER_TRIM_STEP);
            InterfacePacket.sendToAllClients(new PacketVehicleControlDigital(vehicle, PacketVehicleControlDigital.Controls.TRIM_YAW, true));
        }
        return new Object[] {};
    }

    @Callback(doc = "function() -- decrease the vehicle rudder trim by 1")
    public Object[] decreaseRudderTrim(Context ctx, Arguments args) {
        EntityVehicleF_Physics vehicle = this.getVehicle();
        if(vehicle != null) {
            vehicle.rudderTrim = (short) Math.max(-EntityVehicleF_Physics.MAX_RUDDER_TRIM, vehicle.rudderTrim - IVRemoteControl.RUDDER_TRIM_STEP);
            InterfacePacket.sendToAllClients(new PacketVehicleControlDigital(vehicle, PacketVehicleControlDigital.Controls.TRIM_YAW, false));
        }
        return new Object[] {};
    }

    @Callback(doc = "function(angle:number,cooldown:number) -- set the vehicle rudder angle (degrees) and cooldown (seconds, 6.35 max)")
    public Object[] setRudder(Context ctx, Arguments args) {
        EntityVehicleF_Physics vehicle = this.getVehicle();
        if(vehicle != null) {
            short angle = (short) Math.max(-EntityVehicleF_Physics.MAX_RUDDER_ANGLE, Math.min(EntityVehicleF_Physics.MAX_RUDDER_ANGLE, args.checkDouble(0) * IVRemoteControl.RUDDER_ANGLE_STEP));
            byte cooldown = (byte) Math.max(0, Math.min(Byte.MAX_VALUE, args.checkDouble(1) * IVRemoteControl.SECOND_TICKS));
            short oldAngle = vehicle.rudderAngle;
            vehicle.rudderAngle = angle;
            vehicle.rudderCooldown = cooldown;
            InterfacePacket.sendToAllClients(new PacketVehicleControlAnalog(vehicle, PacketVehicleControlAnalog.Controls.RUDDER, vehicle.rudderCooldown != Byte.MAX_VALUE ? (short) (vehicle.rudderAngle - oldAngle) : vehicle.rudderAngle, vehicle.rudderCooldown));
        }
        return new Object[] {};
    }

    @Callback(direct = true, doc = "function(): number,number,number -- get the vehicle rudder angle (degrees), cooldown (seconds) and trim")
    public Object[] getRudder(Context ctx, Arguments args) {
        EntityVehicleF_Physics vehicle = this.getVehicle();
        if(vehicle != null) {
            return new Object[] {
                    (double) vehicle.rudderAngle / (double) IVRemoteControl.RUDDER_ANGLE_STEP,
                    (double) vehicle.rudderCooldown / (double) IVRemoteControl.SECOND_TICKS,
                    (double) vehicle.rudderTrim / (double) IVRemoteControl.RUDDER_TRIM_STEP
            };
        }
        return new Object[] {};
    }

    @Callback(doc = "function(gearUp:boolean) -- set the vehicle's gear up command state")
    public Object[] setGearUp(Context ctx, Arguments args) {
        EntityVehicleF_Physics vehicle = this.getVehicle();
        if(vehicle != null) {
            vehicle.gearUpCommand = args.checkBoolean(0);
            InterfacePacket.sendToAllClients(new PacketVehicleControlDigital(vehicle, PacketVehicleControlDigital.Controls.GEAR, vehicle.gearUpCommand));
        }
        return new Object[] {};
    }

    @Callback(doc = "function() -- get the vehicle's gear up command state")
    public Object[] getGearUp(Context ctx, Arguments args) {
        EntityVehicleF_Physics vehicle = this.getVehicle();
        if(vehicle != null) {
            return new Object[] {vehicle.gearUpCommand};
        }
        return new Object[] {};
    }

    @Callback(doc = "function(name:string,state:boolean) -- set a vehicle's custom state")
    public Object[] setCustom(Context ctx, Arguments args) {
        EntityVehicleF_Physics vehicle = this.getVehicle();
        if(vehicle != null) {
            String name = args.checkString(0);
            boolean state = args.checkBoolean(1);
            if(vehicle.definition.rendering.customVariables != null) {
                for(String variable : vehicle.definition.rendering.customVariables) {
                    if(variable.equals(name)) {
                        if (!vehicle.variablesOn.contains(name) && state) {
                            vehicle.variablesOn.add(name);
                            InterfacePacket.sendToAllClients(new PacketVehicleVariableToggle(vehicle, name));
                        } else if (vehicle.variablesOn.contains(name) && !state) {
                            vehicle.variablesOn.remove(name);
                            InterfacePacket.sendToAllClients(new PacketVehicleVariableToggle(vehicle, name));
                        }
                        break;
                    }
                }
            }
        }
        return new Object[] {};
    }

    @Callback(direct = true, doc = "function(name:string): boolean -- get a vehicle's custom state")
    public Object[] getCustom(Context ctx, Arguments args) {
        EntityVehicleF_Physics vehicle = this.getVehicle();
        if(vehicle != null) {
            if(vehicle.definition.rendering.customVariables != null) {
                for(String variable : vehicle.definition.rendering.customVariables) {
                    if (variable.equals(args.checkString(0))) {
                        return new Object[] {vehicle.variablesOn.contains(variable)};
                    }
                }
            }
            return new Object[] {false};
        }
        return new Object[] {};
    }

    @Callback(direct = true, doc = "function(): table -- get all vehicle's active custom states")
    public Object[] getCustomsOn(Context ctx, Arguments args) {
        EntityVehicleF_Physics vehicle = this.getVehicle();
        if(vehicle != null) {
            Set<String> customsOn = new HashSet<>();
            if(vehicle.definition.rendering.customVariables != null) {
                for(String variable : vehicle.definition.rendering.customVariables) {
                    if (variable.equals(args.checkString(0)) && vehicle.variablesOn.contains(variable)) {
                        customsOn.add(variable);
                    }
                }
            }
            return new Object[] {customsOn};
        }
        return new Object[] {};
    }

    @Callback(doc = "function(): boolean,string|table,string? -- attach a trailer to the vehicle. Returns true, trailer entity uuid and trailer uuid if a trailer was attached, or false and the errors otherwise")
    public Object[] attachTrailer(Context ctx, Arguments args) {
        EntityVehicleF_Physics vehicle = this.getVehicle();
        if(vehicle != null) {
            Set<String> errors = new HashSet<>();
            if(vehicle.towedVehicle == null) {
                for(AEntityBase entity : AEntityBase.createdServerEntities) {
                    if(entity instanceof EntityVehicleF_Physics && !entity.equals(vehicle)) {
                        EntityVehicleF_Physics.TrailerConnectionResult result = vehicle.tryToConnect((EntityVehicleF_Physics) entity);
                        if(result == EntityVehicleF_Physics.TrailerConnectionResult.TRAILER_CONNECTED) {
                            return new Object[] {true, vehicle.towedVehicle.uniqueUUID};
                        } else {
                            errors.add(StringUtils.capitalize(result.name().replace('_', ' ').toLowerCase()));
                        }
                    }
                }
            }
            return new Object[] {false, errors};
        }
        return new Object[] {};
    }

    @Callback(doc = "function(): boolean -- detach the vehicle trailer. Returns true if a trailer was detached")
    public Object[] detachTrailer(Context ctx, Arguments args) {
        EntityVehicleF_Physics vehicle = this.getVehicle();
        if(vehicle != null) {
            if(vehicle.towedVehicle != null) {
                vehicle.changeTrailer(null, null, null, null, null);
                return new Object[] {true};
            } else {
                return new Object[] {false};
            }
        }
        return new Object[] {};
    }

    @Callback(direct = true, doc = "function(): table -- get all vehicle text lines")
    public Object[] getText(Context ctx, Arguments args) {
        EntityVehicleF_Physics vehicle = this.getVehicle();
        if(vehicle != null) {
            Map<String, String> lines = new HashMap<>();
            if(vehicle.definition.rendering.textObjects != null) {
                for(JSONText text : vehicle.definition.rendering.textObjects) {
                    lines.put(text.fieldName, vehicle.text.get(text));
                }
            }
            for(APart part : vehicle.parts) {
                if(part.definition.rendering != null && part.definition.rendering.textObjects != null) {
                    for(JSONText text : part.definition.rendering.textObjects) {
                        lines.put(text.fieldName, part.text.get(text));
                    }
                }
            }
            return new Object[] {lines};
        }
        return new Object[] {};
    }

    @Callback(doc = "function(name:string,text:string) -- set a vehicle text line")
    public Object[] setText(Context ctx, Arguments args) {
        EntityVehicleF_Physics vehicle = this.getVehicle();
        if(vehicle != null) {
            String name = args.checkString(0);
            String text = args.checkString(1);

            List<String> textLines = new ArrayList<>();
            if(vehicle.definition.rendering.textObjects != null) {
                for(JSONText textDef : vehicle.definition.rendering.textObjects) {
                    if(textDef.fieldName.equals(name)) {
                        vehicle.text.put(textDef, text.substring(0, Math.min(textDef.maxLength, text.length())));
                    }
                    textLines.add(vehicle.text.get(textDef));
                }
            }
            for(APart part : vehicle.parts) {
                if(part.definition.rendering != null && part.definition.rendering.textObjects != null) {
                    for(JSONText textDef : part.definition.rendering.textObjects) {
                        if(textDef.fieldName.equals(name)) {
                            part.text.put(textDef, text.substring(0, Math.min(textDef.maxLength, text.length())));
                        }
                        textLines.add(vehicle.text.get(textDef));
                    }
                }
            }

            InterfacePacket.sendToAllClients(new PacketVehicleTextChange(vehicle, textLines));
        }
        return new Object[] {};
    }

    @Callback(direct = true, doc = "function(): string -- get the currently selected beacon")
    public Object[] getBeacon(Context ctx, Arguments args) {
        EntityVehicleF_Physics vehicle = this.getVehicle();
        if(vehicle != null) {
            return new Object[] {vehicle.selectedBeaconName};
        }
        return new Object[] {};
    }

    @Callback(doc = "function(name:string) -- set the currently selected beacon")
    public Object[] setBeacon(Context ctx, Arguments args) {
        EntityVehicleF_Physics vehicle = this.getVehicle();
        if(vehicle != null) {
            vehicle.selectedBeaconName = args.checkString(0);
            vehicle.selectedBeacon = BeaconManager.getBeacon(vehicle.world, vehicle.selectedBeaconName);
            InterfacePacket.sendToAllClients(new PacketVehicleBeaconChange(vehicle, vehicle.selectedBeaconName));
        }
        return new Object[] {};
    }

    @Callback(doc = "function(): table -- get all Immersive Vehicles constant values")
    public Object[] getConstants(Context ctx, Arguments args) {
        Map<String, Object> table = new HashMap<>();

        Set<Map<String, Object>> lights = new HashSet<>();
        for(int i = 0; i < LightType.values().length; i++) {
            LightType lightType = LightType.values()[i];
            Map<String, Object> light = new HashMap<>();
            light.put("name", lightType.name());
            light.put("hasBeam", lightType.hasBeam);
            lights.add(light);
        }
        table.put("lights", lights);

        Map<String, Object> aileron = new HashMap<>();
        aileron.put("dampenRate", EntityVehicleF_Physics.AILERON_DAMPEN_RATE / IVRemoteControl.AILERON_ANGLE_STEP);
        aileron.put("trimMin", -EntityVehicleF_Physics.MAX_AILERON_TRIM / IVRemoteControl.AILERON_TRIM_STEP);
        aileron.put("trimMax", EntityVehicleF_Physics.MAX_AILERON_TRIM / IVRemoteControl.AILERON_TRIM_STEP);
        aileron.put("angleMin", -EntityVehicleF_Physics.MAX_AILERON_ANGLE / IVRemoteControl.AILERON_ANGLE_STEP);
        aileron.put("angleMax", EntityVehicleF_Physics.MAX_AILERON_ANGLE / IVRemoteControl.AILERON_ANGLE_STEP);
        table.put("aileron", aileron);

        Map<String, Object> elevator = new HashMap<>();
        elevator.put("dampenRate", EntityVehicleF_Physics.ELEVATOR_DAMPEN_RATE / IVRemoteControl.ELEVATOR_ANGLE_STEP);
        elevator.put("trimMin", -EntityVehicleF_Physics.MAX_ELEVATOR_TRIM / IVRemoteControl.ELEVATOR_TRIM_STEP);
        elevator.put("trimMax", EntityVehicleF_Physics.MAX_ELEVATOR_TRIM / IVRemoteControl.ELEVATOR_TRIM_STEP);
        elevator.put("angleMin", -EntityVehicleF_Physics.MAX_ELEVATOR_ANGLE / IVRemoteControl.ELEVATOR_ANGLE_STEP);
        elevator.put("angleMax", EntityVehicleF_Physics.MAX_ELEVATOR_ANGLE / IVRemoteControl.ELEVATOR_ANGLE_STEP);
        table.put("elevator", elevator);

        Map<String, Object> rudder = new HashMap<>();
        rudder.put("dampenRate", EntityVehicleF_Physics.RUDDER_DAMPEN_RATE / IVRemoteControl.RUDDER_ANGLE_STEP);
        rudder.put("trimMin", -EntityVehicleF_Physics.MAX_RUDDER_TRIM / IVRemoteControl.RUDDER_TRIM_STEP);
        rudder.put("trimMax", EntityVehicleF_Physics.MAX_RUDDER_TRIM / IVRemoteControl.RUDDER_TRIM_STEP);
        rudder.put("angleMin", -EntityVehicleF_Physics.MAX_RUDDER_ANGLE / IVRemoteControl.RUDDER_ANGLE_STEP);
        rudder.put("angleMax", EntityVehicleF_Physics.MAX_RUDDER_ANGLE / IVRemoteControl.RUDDER_ANGLE_STEP);
        table.put("rudder", rudder);

        Map<String, Object> flaps = new HashMap<>();
        flaps.put("angleMin", IVRemoteControl.MIN_FLAPS_ANGLE / IVRemoteControl.FLAPS_STEP);
        flaps.put("angleMax", EntityVehicleF_Physics.MAX_FLAP_ANGLE / IVRemoteControl.FLAPS_STEP);
        table.put("flaps", flaps);

        return new Object[] {table};
    }

    private static Object jsonToTree(JsonElement element) {
        if(element.isJsonObject()) {
            Map<String, Object> tree = new HashMap<>();

            for(Map.Entry<String, JsonElement> entry : element.getAsJsonObject().entrySet()) {
                tree.put(entry.getKey(), VehicleRemoteControllerTileEntity.jsonToTree(entry.getValue()));
            }

            return tree;
        } else if(element.isJsonArray()) {
            Set<Object> objects = new HashSet<>();

            for(JsonElement arrayElement : element.getAsJsonArray()) {
                objects.add(VehicleRemoteControllerTileEntity.jsonToTree(arrayElement));
            }

            return objects;
        } else if(element.isJsonPrimitive()) {
            JsonPrimitive primitive = element.getAsJsonPrimitive();

            if(primitive.isBoolean()) {
                return primitive.getAsBoolean();
            } else if(primitive.isNumber()) {
                return primitive.getAsDouble();
            } else if(primitive.isString()) {
                return primitive.getAsString();
            }
        }
        return null;
    }


}