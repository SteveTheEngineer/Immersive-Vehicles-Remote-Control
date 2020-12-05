package me.ste.ivremotecontrol.item;

import me.ste.ivremotecontrol.Configuration;
import me.ste.ivremotecontrol.ModCreativeTab;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

public class VehicleSelectorItem extends Item {
    public static final VehicleSelectorItem INSTANCE = new VehicleSelectorItem();

    private VehicleSelectorItem() {
        this.setRegistryName(new ResourceLocation("ivremotecontrol", "vehicle_selector"));
        this.setUnlocalizedName(this.getRegistryName().toString());
        this.setCreativeTab(ModCreativeTab.INSTANCE);
        this.setMaxStackSize(1);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        tooltip.add(I18n.format("item." + Objects.requireNonNull(this.getRegistryName()).toString() + ".tooltip.help0"));
        tooltip.add(I18n.format("item." + Objects.requireNonNull(this.getRegistryName()).toString() + ".tooltip.help1"));
        if(Configuration.VEHICLE_LOOKUP_TARGET == Configuration.VehicleLookupTarget.ENTITY) {
            if(stack.hasTagCompound() && stack.getTagCompound().hasKey("EntityUUID")) {
                tooltip.add("");
                tooltip.add(I18n.format("item." + Objects.requireNonNull(this.getRegistryName()).toString() + ".tooltip.entity", stack.getTagCompound().getString("EntityUUID")));
            }
        } else if(Configuration.VEHICLE_LOOKUP_TARGET == Configuration.VehicleLookupTarget.VEHICLE) {
            if(stack.hasTagCompound() && stack.getTagCompound().hasKey("VehicleUUID")) {
                tooltip.add("");
                tooltip.add(I18n.format("item." + Objects.requireNonNull(this.getRegistryName()).toString() + ".tooltip.vehicle", stack.getTagCompound().getString("VehicleUUID")));
            }
        }
    }
}