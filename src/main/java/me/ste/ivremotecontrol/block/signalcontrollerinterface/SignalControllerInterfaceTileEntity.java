package me.ste.ivremotecontrol.block.signalcontrollerinterface;

import li.cil.oc.api.Network;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.Connector;
import li.cil.oc.api.network.Visibility;
import li.cil.oc.api.prefab.TileEntityEnvironment;
import mcinterface1122.BuilderTileEntity;
import minecrafttransportsimulator.blocks.tileentities.components.ATileEntityBase;
import minecrafttransportsimulator.blocks.tileentities.instances.TileEntitySignalController;
import minecrafttransportsimulator.mcinterface.MasterLoader;
import minecrafttransportsimulator.packets.instances.PacketTileEntitySignalControllerChange;
import net.minecraft.block.BlockDirectional;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class SignalControllerInterfaceTileEntity extends TileEntityEnvironment implements ITickable {
    private boolean hasEnergy = false;

    public SignalControllerInterfaceTileEntity() {
        this.node = Network.newNode(this, Visibility.Network).withConnector().withComponent("signalcontroller").create();
    }

    @Override
    public void update() {
        if(this.node != null) {
            this.hasEnergy = ((Connector) this.node).tryChangeBuffer(-0.5D);
        }
    }

    public TileEntitySignalController getController() {
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
        if(!(tileEntityBase instanceof TileEntitySignalController)) {
            return null;
        }
        return (TileEntitySignalController) tileEntityBase;
    }

    @Callback(direct = true, doc = "function(): boolean -- check whether the signal controller is available")
    public Object[] isAvailable(Context ctx, Arguments args) {
        return new Object[] {this.getController() != null};
    }

    @Callback(direct = true, doc = "function(): string -- get the controller primary axis. Can be either Z or X")
    public Object[] getAxis(Context ctx, Arguments args) {
        TileEntitySignalController controller = this.getController();
        if(controller != null) {
            return new Object[] {controller.mainDirectionXAxis ? "X" : "Z"};
        }
        return new Object[] {};
    }

    @Callback(doc = "function(axis:string) -- set the controller primary axis. Can be either Z or X")
    public Object[] setAxis(Context ctx, Arguments args) {
        TileEntitySignalController controller = this.getController();
        if(controller != null) {
            String axis = args.checkString(0);
            boolean modified = false;
            if(axis.equalsIgnoreCase("Z")) {
                controller.mainDirectionXAxis = false;
                modified = true;
            } else if(axis.equalsIgnoreCase("X")) {
                controller.mainDirectionXAxis = true;
                modified = true;
            }
            if(modified) {
                controller.updateState(TileEntitySignalController.OpState.GREEN_MAIN_RED_CROSS, true);
                MasterLoader.networkInterface.sendToAllClients(new PacketTileEntitySignalControllerChange(controller));
            }
        }
        return new Object[] {};
    }

    @Callback(direct = true, doc = "function(): string -- get the current controller mode. Can be either TIMED_CYCLE, VEHICLE_TRIGGER, REDSTONE_TRIGGER or REMOTE_CONTROL")
    public Object[] getMode(Context ctx, Arguments args) {
        TileEntitySignalController controller = this.getController();
        if(controller != null) {
            return new Object[] {controller.currentOpMode.name()};
        }
        return new Object[] {};
    }

    @Callback(doc = "function(mode:string) -- set the controller mode. Can be either TIMED_CYCLE, VEHICLE_TRIGGER, REDSTONE_TRIGGER or REMOTE_CONTROL")
    public Object[] setMode(Context ctx, Arguments args) {
        TileEntitySignalController controller = this.getController();
        if(controller != null) {
            try {
                controller.currentOpMode = TileEntitySignalController.OpMode.valueOf(args.checkString(0));
                controller.updateState(TileEntitySignalController.OpState.GREEN_MAIN_RED_CROSS, true);
                MasterLoader.networkInterface.sendToAllClients(new PacketTileEntitySignalControllerChange(controller));
            } catch(IllegalArgumentException ignored) {}
        }
        return new Object[] {};
    }

    @Callback(direct = true, doc = "function(): number -- get the time main signal is green")
    public Object[] getGreenMainTime(Context ctx, Arguments args) {
        TileEntitySignalController controller = this.getController();
        if(controller != null) {
            return new Object[] {controller.greenMainTime};
        }
        return new Object[] {};
    }

    @Callback(doc = "function(time:number) -- set the time main signal is green")
    public Object[] setGreenMainTime(Context ctx, Arguments args) {
        TileEntitySignalController controller = this.getController();
        if(controller != null) {
            controller.greenMainTime = args.checkInteger(0);
            controller.updateState(TileEntitySignalController.OpState.GREEN_MAIN_RED_CROSS, true);
            MasterLoader.networkInterface.sendToAllClients(new PacketTileEntitySignalControllerChange(controller));
        }
        return new Object[] {};
    }

    @Callback(direct = true, doc = "function(): number -- get the time cross signal is green")
    public Object[] getGreenCrossTime(Context ctx, Arguments args) {
        TileEntitySignalController controller = this.getController();
        if(controller != null) {
            return new Object[] {controller.greenCrossTime};
        }
        return new Object[] {};
    }

    @Callback(doc = "function(time:number) -- set the time cross signal is green")
    public Object[] setGreenCrossTime(Context ctx, Arguments args) {
        TileEntitySignalController controller = this.getController();
        if(controller != null) {
            controller.greenCrossTime = args.checkInteger(0);
            controller.updateState(TileEntitySignalController.OpState.GREEN_MAIN_RED_CROSS, true);
            MasterLoader.networkInterface.sendToAllClients(new PacketTileEntitySignalControllerChange(controller));
        }
        return new Object[] {};
    }

    @Callback(direct = true, doc = "function(): number -- get the time main signal is yellow")
    public Object[] getYellowMainTime(Context ctx, Arguments args) {
        TileEntitySignalController controller = this.getController();
        if(controller != null) {
            return new Object[] {controller.yellowMainTime};
        }
        return new Object[] {};
    }

    @Callback(doc = "function(time: number) -- set the time main signal is yellow")
    public Object[] setYellowMainTime(Context ctx, Arguments args) {
        TileEntitySignalController controller = this.getController();
        if(controller != null) {
            controller.yellowMainTime = args.checkInteger(0);
            controller.updateState(TileEntitySignalController.OpState.GREEN_MAIN_RED_CROSS, true);
            MasterLoader.networkInterface.sendToAllClients(new PacketTileEntitySignalControllerChange(controller));
        }
        return new Object[] {};
    }

    @Callback(direct = true, doc = "function(): number -- get the time cross signal is yellow")
    public Object[] getYellowCrossTime(Context ctx, Arguments args) {
        TileEntitySignalController controller = this.getController();
        if(controller != null) {
            return new Object[] {controller.yellowCrossTime};
        }
        return new Object[] {};
    }

    @Callback(doc = "function(time: number) -- set the time cross signal is yellow")
    public Object[] setYellowCrossTime(Context ctx, Arguments args) {
        TileEntitySignalController controller = this.getController();
        if(controller != null) {
            controller.yellowCrossTime = args.checkInteger(0);
            controller.updateState(TileEntitySignalController.OpState.GREEN_MAIN_RED_CROSS, true);
            MasterLoader.networkInterface.sendToAllClients(new PacketTileEntitySignalControllerChange(controller));
        }
        return new Object[] {};
    }

    @Callback(direct = true, doc = "function(): number -- get the time all signals are red")
    public Object[] getAllRedTime(Context ctx, Arguments args) {
        TileEntitySignalController controller = this.getController();
        if(controller != null) {
            return new Object[] {controller.allRedTime};
        }
        return new Object[] {};
    }

    @Callback(doc = "function(time: number) -- set the time all signals are red")
    public Object[] setAllRedTime(Context ctx, Arguments args) {
        TileEntitySignalController controller = this.getController();
        if(controller != null) {
            controller.allRedTime = args.checkInteger(0);
            controller.updateState(TileEntitySignalController.OpState.GREEN_MAIN_RED_CROSS, true);
            MasterLoader.networkInterface.sendToAllClients(new PacketTileEntitySignalControllerChange(controller));
        }
        return new Object[] {};
    }

    @Callback(direct = true, doc = "function(): boolean -- get whether the street lights are on")
    public Object[] getLights(Context ctx, Arguments args) {
        TileEntitySignalController controller = this.getController();
        if(controller != null) {
            return new Object[] {controller.lightsOn};
        }
        return new Object[] {};
    }

    @Callback(direct = true, doc = "function(): table -- get the current signal controller state")
    public Object[] getState(Context ctx, Arguments args) {
        TileEntitySignalController controller = this.getController();
        if(controller != null) {
            Map<String, Object> state = new HashMap<>();
            state.put("name", controller.currentOpState.name());
            state.put("mainSignal", controller.currentOpState.mainSignalState.name());
            state.put("crossSignal", controller.currentOpState.crossSignalState.name());
            return new Object[] {state};
        }
        return new Object[] {};
    }

    @Callback(direct = true, doc = "function(): number -- get the time since the last operation has started")
    public Object[] getTimeOperationStarted(Context ctx, Arguments args) {
        TileEntitySignalController controller = this.getController();
        if(controller != null) {
            return new Object[] {controller.timeOperationStarted};
        }
        return new Object[] {};
    }
}