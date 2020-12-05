package me.ste.ivremotecontrol;

import me.ste.ivremotecontrol.item.VehicleSelectorItem;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class ModCreativeTab extends CreativeTabs {
    public static final ModCreativeTab INSTANCE = new ModCreativeTab();

    private ModCreativeTab() {
        super(new ResourceLocation("ivremotecontrol", "tab").toString());
    }

    @Override
    public ItemStack getTabIconItem() {
        return new ItemStack(VehicleSelectorItem.INSTANCE);
    }
}