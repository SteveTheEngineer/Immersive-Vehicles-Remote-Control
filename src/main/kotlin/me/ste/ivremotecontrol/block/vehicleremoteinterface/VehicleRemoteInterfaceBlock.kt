package me.ste.ivremotecontrol.block.vehicleremoteinterface

import me.ste.ivremotecontrol.IVRCGuiHandler
import me.ste.ivremotecontrol.IVRemoteControl
import me.ste.ivremotecontrol.tab.IVRCCreativeTab
import net.minecraft.block.Block
import net.minecraft.block.ITileEntityProvider
import net.minecraft.block.SoundType
import net.minecraft.block.material.Material
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.item.EntityItem
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemBlock
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraftforge.items.CapabilityItemHandler

object VehicleRemoteInterfaceBlock : Block(Material.ROCK), ITileEntityProvider {
    init {
        this.registryName = IVRemoteControl.resourceLocation("vehicle_remote_controller")
        this.unlocalizedName = this.registryName.toString()
        this.setHarvestLevel("pickaxe", 2)
        this.soundType = SoundType.STONE
        this.setCreativeTab(IVRCCreativeTab)
        this.blockHardness = 3F
    }

    val ITEM = ItemBlock(this).apply {
        this.registryName = this@VehicleRemoteInterfaceBlock.registryName
        this.unlocalizedName = this@VehicleRemoteInterfaceBlock.unlocalizedName
        this.creativeTab = this@VehicleRemoteInterfaceBlock.creativeTabToDisplayOn
    }

    override fun createNewTileEntity(worldIn: World, meta: Int): TileEntity? = VehicleRemoteInterfaceTileEntity()

    override fun onBlockActivated(
        world: World,
        pos: BlockPos,
        state: IBlockState?,
        playerIn: EntityPlayer,
        hand: EnumHand?,
        facing: EnumFacing?,
        hitX: Float,
        hitY: Float,
        hitZ: Float
    ): Boolean {
        if (!world.isRemote) {
            playerIn.openGui(IVRemoteControl, IVRCGuiHandler.VEHICLE_REMOTE_CONTROLLER, world, pos.x, pos.y, pos.z)
        }
        return true
    }

    override fun breakBlock(world: World, pos: BlockPos, state: IBlockState?) {
        val tile = world.getTileEntity(pos)!!
        val stack: ItemStack =
            tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null)!!.getStackInSlot(0)
        if (!stack.isEmpty) {
            world.spawnEntity(EntityItem(world, pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble(), stack))
        }
        super.breakBlock(world, pos, state)
    }
}