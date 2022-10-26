package me.ste.ivremotecontrol

import me.ste.ivremotecontrol.block.iface.base.InterfaceTileEntity
import me.ste.ivremotecontrol.block.iface.base.gui.InterfaceContainer
import me.ste.ivremotecontrol.block.iface.base.gui.InterfaceGUI
import net.minecraft.client.gui.Gui
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.Container
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraftforge.fml.common.network.IGuiHandler

object IVRCGuiHandler : IGuiHandler {
    private val guis: MutableList<Pair<
                (id: Int, player: EntityPlayer, world: World, x: Int, y: Int, z: Int) -> Container,
                (id: Int, player: EntityPlayer, world: World, x: Int, y: Int, z: Int) -> Gui>
            > = ArrayList()

    val VEHICLE_INTERFACE = this.registerGui(
            { _, player, world, x, y, z ->
                InterfaceContainer(
                        player.inventory,
                        world.getTileEntity(BlockPos(x, y, z)) as InterfaceTileEntity
                )
            },
            { id, player, world, x, y, z ->
                InterfaceGUI(
                        this.getServerGuiElement(
                                id,
                                player,
                                world,
                                x,
                                y,
                                z
                        )!!, player.inventory,
                        false
                )
            })

    val BLOCK_INTERFACE = this.registerGui(
            { _, player, world, x, y, z ->
                InterfaceContainer(
                        player.inventory,
                        world.getTileEntity(BlockPos(x, y, z)) as InterfaceTileEntity
                )
            },
            { id, player, world, x, y, z ->
                InterfaceGUI(
                        this.getServerGuiElement(
                                id,
                                player,
                                world,
                                x,
                                y,
                                z
                        )!!, player.inventory,
                        true
                )
            })

    override fun getServerGuiElement(
        id: Int,
        player: EntityPlayer,
        world: World,
        x: Int,
        y: Int,
        z: Int
    ): Container? {
        return this.guis.getOrNull(id)?.first?.invoke(id, player, world, x, y, z)
    }

    override fun getClientGuiElement(id: Int, player: EntityPlayer, world: World, x: Int, y: Int, z: Int): Gui? {
        return this.guis.getOrNull(id)?.second?.invoke(id, player, world, x, y, z)
    }

    fun registerGui(
        container: (id: Int, player: EntityPlayer, world: World, x: Int, y: Int, z: Int) -> Container,
        gui: (id: Int, player: EntityPlayer, world: World, x: Int, y: Int, z: Int) -> Gui
    ): Int {
        this.guis += container to gui
        return this.guis.size - 1
    }
}