package me.ste.ivremotecontrol.block.decorinterface;

import li.cil.oc.api.Network;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.Connector;
import li.cil.oc.api.network.Visibility;
import li.cil.oc.api.prefab.TileEntityEnvironment;
import me.ste.ivremotecontrol.block.decorinterface.decor.Decor;
import me.ste.ivremotecontrol.block.decorinterface.decor.TileEntityDecorWrapper;
import me.ste.ivremotecontrol.block.decorinterface.decor.TileEntityPoleWrapper;
import minecrafttransportsimulator.blocks.components.ABlockBase;
import minecrafttransportsimulator.blocks.tileentities.components.ATileEntityBase;
import minecrafttransportsimulator.blocks.tileentities.instances.TileEntityDecor;
import minecrafttransportsimulator.blocks.tileentities.instances.TileEntityPole;
import minecrafttransportsimulator.jsondefs.JSONText;
import minecrafttransportsimulator.mcinterface.BuilderTileEntity;
import minecrafttransportsimulator.packets.components.InterfacePacket;
import net.minecraft.block.BlockDirectional;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DecorInterfaceTileEntity extends TileEntityEnvironment implements ITickable {
    private boolean hasEnergy = false;

    public DecorInterfaceTileEntity() {
        this.node = Network.newNode(this, Visibility.Network).withConnector().withComponent("decor").create();
    }

    @Override
    public void update() {
        if(this.node != null) {
            this.hasEnergy = ((Connector) this.node).tryChangeBuffer(-0.5D);
        }
    }

    public Decor getDecor() {
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
        if(tileEntityBase instanceof TileEntityDecor) {
            return new TileEntityDecorWrapper((TileEntityDecor) tileEntityBase);
        } else if(tileEntityBase instanceof TileEntityPole) {
            return new TileEntityPoleWrapper((TileEntityPole) tileEntityBase);
        } else {
            return null;
        }
    }

    @Callback(direct = true, doc = "function(): boolean -- check whether the decor is available")
    public Object[] isAvailable(Context ctx, Arguments args) {
        return new Object[] {this.getDecor() != null};
    }

    @Callback(direct = true, doc = "function(axis:string): table -- get the decor's text lines. The axis can be either NONE, UP, DOWN, NORTH, SOUTH, EAST or WEST")
    public Object[] getText(Context ctx, Arguments args) {
        Decor decor = this.getDecor();
        if(decor != null) {
            String axisString = args.checkString(0);
            Map<String, String> lines = new HashMap<>();
            ABlockBase.Axis axis;
            try {
                axis = ABlockBase.Axis.valueOf(axisString);
            } catch(IllegalArgumentException e) {
                return new Object[] {lines};
            }
            int i = 0;
            List<String> textLines = decor.getTextLines(axis);
            List<JSONText> objects = decor.getDefinitionTextObjects(axis);
            if(objects == null) {
                if(!decor.isPole()) {
                    objects = new ArrayList<>();

                    JSONText text = new JSONText();
                    text.fieldName = "Beacon Name";
                    text.maxLength = 5;
                    objects.add(text);

                    JSONText text2 = new JSONText();
                    text2.fieldName = "Glide Slope (Deg)";
                    text2.maxLength = 5;
                    objects.add(text2);

                    JSONText text3 = new JSONText();
                    text3.fieldName = "Bearing (Deg)";
                    text3.maxLength = 5;
                    objects.add(text3);
                } else {
                    return new Object[] {lines};
                }
            }
            for(JSONText text : objects) {
                lines.put(text.fieldName, textLines.get(i++));
            }
            return new Object[] {lines};
        }
        return new Object[] {};
    }

    @Callback(doc = "function(axis:string,name:string,text:string) -- set a decor's text line. The axis can be either NONE, UP, DOWN, NORTH, SOUTH, EAST or WEST")
    public Object[] setText(Context ctx, Arguments args) {
        Decor decor = this.getDecor();
        if(decor != null) {
            String axisString = args.checkString(0);
            String name = args.checkString(1);
            String text = args.checkString(2);
            ABlockBase.Axis axis;
            try {
                axis = ABlockBase.Axis.valueOf(axisString);
            } catch(IllegalArgumentException e) {
                return new Object[] {};
            }
            List<JSONText> objects = decor.getDefinitionTextObjects(axis);
            if(objects == null) {
                if(!decor.isPole()) {
                    objects = new ArrayList<>();

                    JSONText text1 = new JSONText();
                    text1.fieldName = "Beacon Name";
                    text1.maxLength = 5;
                    objects.add(text1);

                    JSONText text2 = new JSONText();
                    text2.fieldName = "Glide Slope (Deg)";
                    text2.maxLength = 5;
                    objects.add(text2);

                    JSONText text3 = new JSONText();
                    text3.fieldName = "Bearing (Deg)";
                    text3.maxLength = 5;
                    objects.add(text3);
                } else {
                    return new Object[] {};
                }
            }
            List<String> textLines = decor.getTextLines(axis);
            int i = 0;
            for(JSONText textDef : objects) {
                if(textDef.fieldName.equals(name)) {
                    textLines.set(i++, text.substring(0, Math.min(textDef.maxLength, text.length())));
                }
            }
            InterfacePacket.sendToAllClients(decor.getUpdatePacket(axis, textLines));
        }
        return new Object[] {};
    }
}