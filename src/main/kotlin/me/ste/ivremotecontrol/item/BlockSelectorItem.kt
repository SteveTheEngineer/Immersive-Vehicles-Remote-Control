package me.ste.ivremotecontrol.item

import me.ste.ivremotecontrol.IVRemoteControl
import me.ste.ivremotecontrol.tab.IVRCCreativeTab
import net.minecraft.client.resources.I18n
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.ActionResult
import net.minecraft.util.EnumActionResult
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

object BlockSelectorItem : Item() {
    init {
        this.registryName = IVRemoteControl.resourceLocation("block_selector")
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

        // Check whether the stack has NBT data and has the NBT tag set
        if (this.hasEffect(stack)) {
            // Add the further information lines
            tooltip += ""
            tooltip += I18n.format(
                    "item.${this.registryName}.tooltip.block",
                    stack.tagCompound!!.getInteger("BlockX"),
                    stack.tagCompound!!.getInteger("BlockY"),
                    stack.tagCompound!!.getInteger("BlockZ"),
                    stack.tagCompound!!.getInteger("BlockDimension")
            )
        }
    }

    override fun hasEffect(stack: ItemStack) =
            // Add the enchantment glint if the item is bound to a vehicle
            stack.hasTagCompound()
                    && stack.tagCompound!!.hasKey("BlockX")
                    && stack.tagCompound!!.hasKey("BlockY")
                    && stack.tagCompound!!.hasKey("BlockZ")
                    && stack.tagCompound!!.hasKey("BlockDimension")

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

    override fun onItemUse(player: EntityPlayer, world: World, pos: BlockPos, hand: EnumHand, facing: EnumFacing, hitX: Float, hitY: Float, hitZ: Float): EnumActionResult {
        if (world.isRemote) {
            return EnumActionResult.SUCCESS
        }

        val stack = player.getHeldItem(hand)

        val tag = stack.tagCompound ?: NBTTagCompound()

        tag.setInteger("BlockX", pos.x)
        tag.setInteger("BlockY", pos.y)
        tag.setInteger("BlockZ", pos.z)
        tag.setInteger("BlockDimension", world.provider.dimension)

        stack.tagCompound = tag

        return EnumActionResult.SUCCESS
    }
}