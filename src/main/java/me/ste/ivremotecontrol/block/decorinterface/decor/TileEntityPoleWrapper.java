package me.ste.ivremotecontrol.block.decorinterface.decor;

import minecrafttransportsimulator.blocks.components.ABlockBase;
import minecrafttransportsimulator.blocks.tileentities.components.ATileEntityPole_Component;
import minecrafttransportsimulator.blocks.tileentities.instances.TileEntityPole;
import minecrafttransportsimulator.jsondefs.JSONText;
import minecrafttransportsimulator.packets.instances.PacketTileEntityPoleChange;

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
            return component.getTextLines();
        } else {
            return null;
        }
    }

    @Override
    public void setTextLines(ABlockBase.Axis axis, List<String> lines) {
        ATileEntityPole_Component component = this.pole.components.get(axis);
        if(component != null) {
            component.setTextLines(lines);
        }
    }

    @Override
    public PacketTileEntityPoleChange getUpdatePacket(ABlockBase.Axis axis, List<String> lines) {
        return new PacketTileEntityPoleChange(this.pole, axis, null, lines, false);
    }

    @Override
    public List<JSONText> getDefinitionTextObjects(ABlockBase.Axis axis) {
        ATileEntityPole_Component component = this.pole.components.get(axis);
        if(component != null) {
            return component.definition.general.textObjects;
        } else {
            return null;
        }
    }

    @Override
    public boolean isPole() {
        return true;
    }
}