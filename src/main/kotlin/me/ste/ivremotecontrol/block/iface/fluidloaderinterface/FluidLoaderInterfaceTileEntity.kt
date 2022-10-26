package me.ste.ivremotecontrol.block.iface.fluidloaderinterface

import mcinterface1122.BuilderTileEntity
import me.ste.ivremotecontrol.block.iface.base.InterfaceTileEntity
import me.ste.ivremotecontrol.block.iface.base.context.CallContext
import me.ste.ivremotecontrol.item.BlockSelectorItem
import me.ste.ivremotecontrol.util.MTSUtil
import me.ste.ivremotecontrol.util.mtsTileEntity
import minecrafttransportsimulator.blocks.tileentities.instances.TileEntityFluidLoader
import net.minecraft.item.ItemStack


class FluidLoaderInterfaceTileEntity : InterfaceTileEntity("fluidloader") {
    override fun isItemValid(stack: ItemStack) = stack.item is BlockSelectorItem

    private fun getLoader() = this.getMTSTileEntity<TileEntityFluidLoader>()

    private fun isAvailable(ctx: CallContext): Array<Any?> =
        arrayOf(this.getLoader() != null)

    private fun getFluid(ctx: CallContext): Array<Any?>? =
        this.getLoader()?.let { arrayOf(it.tank.fluid, it.tank.fluidLevel, it.tank.maxLevel) }

    private fun isConnected(ctx: CallContext): Array<Any?>? =
        this.getLoader()?.let { arrayOf(it.connectedPart != null) }

    private fun getConnectedVehicle(ctx: CallContext): Array<Any?>? {
        val loader = this.getLoader() ?: return null
        val part = loader.connectedPart ?: return null
        val vehicle = part.vehicleOn ?: return null
        val entity = MTSUtil.getEntity(vehicle)

        return arrayOf(entity?.uniqueID?.toString() ?: "unknown", vehicle.uniqueUUID)
    }

    @Deprecated("Use getType instead")
    private fun getMode(ctx: CallContext): Array<Any?>? {
        val loader = this.getLoader() ?: return null
        return arrayOf(if (loader.isUnloader) "unload" else "load")
    }

    private fun getType(ctx: CallContext): Array<Any?>? {
        val loader = this.getLoader() ?: return null
        return arrayOf(if (loader.isUnloader) "unloader" else "loader")
    }

    @Deprecated("Removed")
    private fun setMode(ctx: CallContext): Array<Any?>? = null

    init {
        this.methods["isAvailable"] = this::isAvailable
        this.methods["getFluid"] = this::getFluid
        this.methods["isConnected"] = this::isConnected
        this.methods["getConnectedVehicle"] = this::getConnectedVehicle
        this.methods["getType"] = this::getType

        this.methods["getMode"] = this::getMode
        this.methods["setMode"] = this::setMode
    }
}