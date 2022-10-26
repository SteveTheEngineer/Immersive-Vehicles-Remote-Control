package me.ste.ivremotecontrol.block.iface.base.gui

import me.ste.ivremotecontrol.block.iface.base.InterfaceTileEntity
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.player.InventoryPlayer
import net.minecraft.inventory.Container
import net.minecraft.inventory.Slot
import net.minecraft.item.ItemStack
import net.minecraft.util.EnumFacing
import net.minecraftforge.items.CapabilityItemHandler
import net.minecraftforge.items.SlotItemHandler

class InterfaceContainer(playerInv: InventoryPlayer, val tileEntity: InterfaceTileEntity) : Container() {
    init {
        val inventory = tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.UP)!!

        for (y in 0..2) {
            for (x in 0..2) {
                addSlotToContainer(object : SlotItemHandler(inventory, x + y * 3, 62 + x * 18, 17 + y * 18) {
                    override fun onSlotChanged() = tileEntity.markDirty()
                    override fun isItemValid(stack: ItemStack) = super.isItemValid(stack) && inventory.isItemValid(0, stack)
                })
            }
        }

        for (y in 0..2) {
            for (x in 0..8) {
                this.addSlotToContainer(
                    Slot(
                        playerInv,
                        x + y * 9 + 9,
                        8 + x * 18,
                        84 + y * 18
                    )
                )
            }
        }

        for (x in 0..8) {
            this.addSlotToContainer(
                Slot(
                    playerInv,
                    x,
                    8 + x * 18,
                    142
                )
            )
        }
    }

    override fun canInteractWith(player: EntityPlayer) = true

    override fun transferStackInSlot(player: EntityPlayer, index: Int): ItemStack {
        var stack = ItemStack.EMPTY
        val slot = this.inventorySlots[index]

        if (slot != null && slot.hasStack) {
            val stack2 = slot.stack
            stack = stack2.copy()
            val containerSlots: Int = this.inventorySlots.size - player.inventory.mainInventory.size
            if (index < containerSlots) {
                if (!mergeItemStack(stack2, containerSlots, this.inventorySlots.size, true)) {
                    return ItemStack.EMPTY
                }
            } else if (!mergeItemStack(stack2, 0, containerSlots, false)) {
                return ItemStack.EMPTY
            }
            if (stack2.count == 0) {
                slot.putStack(ItemStack.EMPTY)
            } else {
                slot.onSlotChanged()
            }
            if (stack2.count == stack.count) {
                return ItemStack.EMPTY
            }
            slot.onTake(player, stack2)
        }

        return stack
    }
}