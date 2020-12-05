package me.ste.ivremotecontrol.block.decorinterface.decor;

import minecrafttransportsimulator.blocks.components.ABlockBase;
import minecrafttransportsimulator.blocks.tileentities.instances.TileEntityDecor;
import minecrafttransportsimulator.jsondefs.JSONText;
import minecrafttransportsimulator.packets.instances.PacketTileEntityDecorTextChange;

import java.util.List;

public class TileEntityDecorWrapper implements Decor {
    private TileEntityDecor decor;

    public TileEntityDecorWrapper(TileEntityDecor decor) {
        this.decor = decor;
    }


    @Override
    public List<String> getTextLines(ABlockBase.Axis axis) {
        return this.decor.getTextLines();
    }

    @Override
    public void setTextLines(ABlockBase.Axis axis, List<String> lines) {
        this.decor.setTextLines(lines);
    }

    @Override
    public PacketTileEntityDecorTextChange getUpdatePacket(ABlockBase.Axis axis, List<String> lines) {
        return new PacketTileEntityDecorTextChange(this.decor, lines);
    }

    @Override
    public List<JSONText> getDefinitionTextObjects(ABlockBase.Axis axis) {
        return this.decor.definition.general.textObjects;
    }

    @Override
    public boolean isPole() {
        return false;
    }
}