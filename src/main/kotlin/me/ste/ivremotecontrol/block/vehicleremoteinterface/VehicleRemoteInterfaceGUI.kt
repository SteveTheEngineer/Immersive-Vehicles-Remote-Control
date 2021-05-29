package me.ste.ivremotecontrol.block.vehicleremoteinterface

import me.ste.ivremotecontrol.IVRemoteControl
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.resources.I18n
import net.minecraft.entity.player.InventoryPlayer
import net.minecraft.inventory.Container

class VehicleRemoteInterfaceGUI(container: Container, private val playerInv: InventoryPlayer) :
    GuiContainer(container) {
    override fun drawGuiContainerBackgroundLayer(partialTicks: Float, mouseX: Int, mouseY: Int) {
        this.drawDefaultBackground()

        GlStateManager.color(1F, 1F, 1F, 1F)
        this.mc.textureManager.bindTexture(IVRemoteControl.resourceLocation("textures/gui/vehicle_remote_controller.png"))
        this.drawTexturedModalRect(
            (this.width - this.xSize) / 2,
            (this.height - this.ySize) / 2,
            0, 0, this.xSize, this.ySize
        )
    }

    override fun drawGuiContainerForegroundLayer(mouseX: Int, mouseY: Int) {
        val title = I18n.format("${VehicleRemoteInterfaceBlock.unlocalizedName}.name")
        this.fontRenderer.drawString(title, this.xSize / 2 - this.fontRenderer.getStringWidth(title) / 2, 6, 0x404040)
        this.fontRenderer.drawString(this.playerInv.displayName.unformattedText, 8, this.ySize - 94, 0x404040)
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        super.drawScreen(mouseX, mouseY, partialTicks)
        this.renderHoveredToolTip(mouseX, mouseY)
    }
}