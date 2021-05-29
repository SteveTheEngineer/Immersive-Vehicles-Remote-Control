package me.ste.ivremotecontrol.tab

import me.ste.ivremotecontrol.IVRemoteControl
import me.ste.ivremotecontrol.item.VehicleSelectorItem
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.item.ItemStack

object IVRCCreativeTab : CreativeTabs(
    IVRemoteControl.resourceLocation("tab").toString()
) {
    override fun getTabIconItem() = ItemStack(VehicleSelectorItem)
}