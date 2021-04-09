package me.ste.ivremotecontrol.block.decorinterface.decor;

import minecrafttransportsimulator.blocks.components.ABlockBase;
import minecrafttransportsimulator.blocks.tileentities.components.ATileEntityPole_Component;
import minecrafttransportsimulator.blocks.tileentities.instances.TileEntityPole;
import minecrafttransportsimulator.jsondefs.JSONText;
import minecrafttransportsimulator.packets.instances.PacketEntityTextChange;

import java.util.ArrayList;
import java.util.List;

public class TileEntityPoleWrapper implements Decor {
    private TileEntityPole pole;

    public TileEntityPoleWrapper(TileEntityPole pole) {
        this.pole = pole;
    }


    @Override
    public List<String> getTextLines(ABlockBase.Axis axis) {
        ATileEntityPole_Component component = this.pole.components.get(axis);
        if(component != null) {
            return new ArrayList<>(component.text.values());
        } else {
            return null;
        }
    }

    @Override
    public void setTextLines(ABlockBase.Axis axis, List<String> lines) {
        ATileEntityPole_Component component = this.pole.components.get(axis);
        if(component != null) {
            component.updateText(lines);
        }
    }

    @Override
    public PacketEntityTextChange getUpdatePacket(ABlockBase.Axis axis, List<String> lines) {
        ATileEntityPole_Component component = this.pole.components.get(axis);
        if(component != null) {
            return new PacketEntityTextChange(component, lines);
        } else {
            return null;
        }
    }

    @Override
    public List<JSONText> getDefinitionTextObjects(ABlockBase.Axis axis) {
        ATileEntityPole_Component component = this.pole.components.get(axis);
        if(component != null) {
            return component.definition.rendering.textObjects;
        } else {
            return null;
        }
    }

    @Override
    public boolean isPole() {
        return true;
    }
}