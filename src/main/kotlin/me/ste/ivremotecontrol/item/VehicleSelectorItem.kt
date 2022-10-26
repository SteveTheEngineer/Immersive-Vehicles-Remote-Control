package me.ste.ivremotecontrol.item

import me.ste.ivremotecontrol.IVRemoteControl
import me.ste.ivremotecontrol.IVRCConfiguration
import me.ste.ivremotecontrol.tab.IVRCCreativeTab
import net.minecraft.client.resources.I18n
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.util.ActionResult
import net.minecraft.util.EnumActionResult
import net.minecraft.util.EnumHand
import net.minecraft.world.World

object VehicleSelectorItem : Item() {
    init {
        this.registryName = IVRemoteControl.resourceLocation("vehicle_selector")
        this.unlocalizedName = this.registryName.toString()
        this.creativeTab = IVRCCreativeTab
        this.maxStackSize = 1
    }

    override fun getUnlocalizedName(stack: ItemStack): String {
        return "${this.unlocalizedName}.${if (this.hasEffect(stack)) "bound" else "unbound"}"
    }

    override fun addInformation(stack: ItemStack, world: World?, tooltip: MutableList<String>, flag: ITooltipFlag) {
        // Add the hint tooltip lines
        tooltip += I18n.format("item.${this.registryName}.tooltip.help0")
        tooltip += I18n.format("item.${this.registryName}.tooltip.help1")

        // Find the NBT tag to get the UUID from
        val name =
            when (IVRCConfiguration.LOOKUP_VALUE) {
                IVRCConfiguration.LookupValue.VEHICLE -> "VehicleUUID"
                IVRCConfiguration.LookupValue.ENTITY -> "EntityUUID"
            }

        // Check whether the stack has NBT data and has the NBT tag set
        if (stack.hasTagCompound() && stack.tagCompound!!.hasKey(name)) {
            // Add the further information lines
            tooltip += ""
            tooltip += I18n.format(
                "item.${this.registryName}.tooltip.uuid",
                stack.tagCompound!!.getString(name)
            )
        }
    }

    override fun hasEffect(stack: ItemStack) =
        // Add the enchantment glint if the item is bound to a vehicle
        stack.hasTagCompound() && stack.tagCompound!!.hasKey(
            when (IVRCConfiguration.LOOKUP_VALUE) {
                IVRCConfiguration.LookupValue.VEHICLE -> "VehicleUUID"
                IVRCConfiguration.LookupValue.ENTITY -> "EntityUUID"
            }
        )

    override fun onItemRightClick(world: World, player: EntityPlayer, hand: EnumHand): ActionResult<ItemStack> {
        val stack = player.getHeldItem(hand)

        // Check whether the player is sneaking and the item has NBT data
        return if (player.isSneaking && stack.hasTagCompound()) {
            // Check whether the code is running on the server side
            if (!world.isRemote) {
                // Remove the NBT data if we're on the server
                stack.tagCompound = null
            }
            ActionResult.newResult(EnumActionResult.SUCCESS, stack)
        } else {
            ActionResult.newResult(EnumActionResult.PASS, stack)
        }
    }
}