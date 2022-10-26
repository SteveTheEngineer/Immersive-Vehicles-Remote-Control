package me.ste.ivremotecontrol.block.iface.base.gui

import me.ste.ivremotecontrol.IVRemoteControl
import me.ste.ivremotecontrol.block.antenna.AntennaBlock
import me.ste.ivremotecontrol.block.iface.vehicleremoteinterface.VehicleRemoteInterfaceBlock
import net.minecraft.block.BlockDirectional
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.resources.I18n
import net.minecraft.entity.player.InventoryPlayer
import net.minecraft.inventory.Container
import net.minecraft.util.ResourceLocation
import net.minecraft.util.text.TextFormatting
import net.minecraftforge.items.SlotItemHandler

class InterfaceGUI(container: Container, private val playerInv: InventoryPlayer, private val isBlock: Boolean) : GuiContainer(container) {
    private fun getProblems(): List<String> {
        val problems = mutableListOf<String>()

        // Antenna
        val container = this.inventorySlots as InterfaceContainer
        val tile = container.tileEntity
        val state = tile.world.getBlockState(tile.pos)
        val facing = state.getValue(BlockDirectional.FACING)

        val antennaPos = tile.pos.offset(facing)
        val antennaBlock = tile.world.getBlockState(antennaPos).block
        if (antennaBlock !is AntennaBlock) {
            problems += TextFormatting.RED.toString() + I18n.format("ivremotecontrol.interfaceGui.problems.antennaMissing")
        }

        // Selectors
        if (container.inventorySlots.filterIsInstance<SlotItemHandler>().all { !it.hasStack }) {
            problems += TextFormatting.GOLD.toString() + if (this.isBlock) {
                I18n.format("ivremotecontrol.interfaceGui.problems.selectorMissing.block")
            } else {
                I18n.format("ivremotecontrol.interfaceGui.problems.selectorMissing.vehicle")
            }
        }

        return problems
    }

    override fun drawGuiContainerBackgroundLayer(partialTicks: Float, mouseX: Int, mouseY: Int) {
        this.drawDefaultBackground()

        // Draw background
        GlStateManager.color(1F, 1F, 1F, 1F)
        this.mc.textureManager.bindTexture(
                if (this.isBlock) {
                    IVRemoteControl.resourceLocation("textures/gui/block_interface.png")
                } else {
                    IVRemoteControl.resourceLocation("textures/gui/vehicle_interface.png")
                }
        )
        this.drawTexturedModalRect(
            (this.width - this.xSize) / 2,
            (this.height - this.ySize) / 2,
            0, 0, this.xSize, this.ySize
        )

        // Draw a warning icon if there are problems
        if (this.getProblems().isNotEmpty()) {
            this.drawTexturedModalRect(
                    (this.width - this.xSize) / 2 + 20,
                    (this.height - this.ySize) / 2 + 30,
                    this.xSize, 0, 21, 21
            )
        }
    }

    override fun drawGuiContainerForegroundLayer(mouseX: Int, mouseY: Int) {
        val container = this.inventorySlots as InterfaceContainer
        val tile = container.tileEntity
        val block = tile.world.getBlockState(tile.pos).block

        val title = I18n.format(block.localizedName)

        // Draw title
        GlStateManager.pushMatrix()
        GlStateManager.scale(0.8, 0.8, 0.8)
        this.drawCenteredString(this.fontRenderer, title, (this.xSize / 2 * 1.25).toInt(), 6, 0xFFFFFF)
        GlStateManager.popMatrix()
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        super.drawScreen(mouseX, mouseY, partialTicks)
        this.renderHoveredToolTip(mouseX, mouseY)

        // Draw a tooltip if the mouse is hovering over the warning icon
        val startX = (this.width - this.xSize) / 2 + 20
        val startY = (this.height - this.ySize) / 2 + 30
        val endX = startX + 21
        val endY = startY + 21

        val problems = this.getProblems()
        if (mouseX >= startX && mouseY >= startY && mouseX < endX && mouseY < endY && problems.isNotEmpty()) {
            this.drawHoveringText(problems, mouseX, mouseY)
        }
    }
}