package me.ste.ivremotecontrol

import me.ste.ivremotecontrol.block.vehicleremoteinterface.VehicleRemoteInterfaceContainer
import me.ste.ivremotecontrol.block.vehicleremoteinterface.VehicleRemoteInterfaceGUI
import me.ste.ivremotecontrol.block.vehicleremoteinterface.VehicleRemoteInterfaceTileEntity
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

    val VEHICLE_REMOTE_CONTROLLER = this.registerGui(
        { _, player, world, x, y, z ->
            VehicleRemoteInterfaceContainer(
                player.inventory,
                world.getTileEntity(BlockPos(x, y, z)) as VehicleRemoteInterfaceTileEntity
            )
        },
        { id, player, world, x, y, z ->
            VehicleRemoteInterfaceGUI(
                this.getServerGuiElement(
                    id,
                    player,
                    world,
                    x,
                    y,
                    z
                )!!, player.inventory
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