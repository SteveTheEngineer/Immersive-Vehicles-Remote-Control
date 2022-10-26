package me.ste.ivremotecontrol.block.iface.base

import dan200.computercraft.api.peripheral.IPeripheral
import me.ste.ivremotecontrol.IVRemoteControl
import me.ste.ivremotecontrol.block.antenna.AntennaBlock
import me.ste.ivremotecontrol.tab.IVRCCreativeTab
import net.minecraft.block.BlockDirectional
import net.minecraft.block.ITileEntityProvider
import net.minecraft.block.SoundType
import net.minecraft.block.material.Material
import net.minecraft.block.state.BlockStateContainer
import net.minecraft.block.state.IBlockState
import net.minecraft.client.resources.I18n
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.item.EntityItem
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemBlock
import net.minecraft.item.ItemStack
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockAccess
import net.minecraft.world.World
import net.minecraftforge.items.CapabilityItemHandler

abstract class InterfaceBlock(
    private val id: String
) : BlockDirectional(Material.ROCK), ITileEntityProvider {
    companion object {
        val SHAPE_UP_DOWN = AxisAlignedBB(5.0 / 16.0, 0.0, 5.0 / 16.0, 11.0 / 16.0, 1.0, 11.0 / 16.0)
        val SHAPE_NORTH_SOUTH = AxisAlignedBB(5.0 / 16.0, 5.0 / 16.0, 0.0, 11.0 / 16.0, 11.0 / 16.0, 1.0)
        val SHAPE_EAST_WEST = AxisAlignedBB(0.0, 5.0 / 16.0, 5.0 / 16.0, 1.0, 11.0 / 16.0, 11.0 / 16.0)
    }

    // Block setup
    init {
        this.registryName = IVRemoteControl.resourceLocation(this.id)
        this.unlocalizedName = this.registryName.toString()
        this.setHarvestLevel("pickaxe", 2)
        this.soundType = SoundType.STONE
        this.setCreativeTab(IVRCCreativeTab)
        this.blockHardness = 3F
        this.defaultState = this.blockState.baseState.withProperty(FACING, EnumFacing.UP)
    }

    val ITEM = ItemBlock(this).apply {
        this.registryName = this@InterfaceBlock.registryName
        this.unlocalizedName = this@InterfaceBlock.unlocalizedName
        this.creativeTab = this@InterfaceBlock.creativeTabToDisplayOn
    }

    override fun createBlockState() = BlockStateContainer(this, FACING)
    override fun getMetaFromState(state: IBlockState) = state.getValue(FACING).ordinal
    override fun getStateFromMeta(meta: Int): IBlockState =
        this.blockState.baseState.withProperty(FACING, EnumFacing.getFront(meta))

    override fun getStateForPlacement(worldIn: World, pos: BlockPos, facing: EnumFacing, hitX: Float, hitY: Float, hitZ: Float, meta: Int, placer: EntityLivingBase): IBlockState {
        val state = super.getStateForPlacement(worldIn, pos, facing, hitX, hitY, hitZ, meta, placer)
        return state.withProperty(FACING, facing)
    }

    override fun getBoundingBox(state: IBlockState, source: IBlockAccess, pos: BlockPos) =
            when (state.getValue(FACING)) {
                EnumFacing.UP, EnumFacing.DOWN -> SHAPE_UP_DOWN
                EnumFacing.NORTH, EnumFacing.SOUTH -> SHAPE_NORTH_SOUTH
                EnumFacing.EAST, EnumFacing.WEST -> SHAPE_EAST_WEST
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

    override fun isOpaqueCube(state: IBlockState) = false
    override fun isFullCube(state: IBlockState) = false
    override fun shouldSideBeRendered(blockState: IBlockState, blockAccess: IBlockAccess, pos: BlockPos, side: EnumFacing) = true

    override fun isSideSolid(state: IBlockState, world: IBlockAccess, pos: BlockPos, side: EnumFacing): Boolean {
        val facing = state.getValue(FACING)
        if (facing.opposite == side) {
            return true
        }

        return super.isSideSolid(state, world, pos, side)
    }

    override fun onBlockActivated(world: World, pos: BlockPos, state: IBlockState, player: EntityPlayer, hand: EnumHand, facing: EnumFacing, hitX: Float, hitY: Float, hitZ: Float): Boolean {
        val selfFacing = state.getValue(FACING)

        val handStack = player.getHeldItem(hand)
        val handItem = handStack.item as? ItemBlock
        if (handItem != null) {
            val handBlock = handItem.block
            if (handBlock is AntennaBlock && selfFacing == facing) {
                return false
            }
        }

        if (!world.isRemote) {
            player.openGui(IVRemoteControl, this.getGuiId(), world, pos.x, pos.y, pos.z)
        }

        return true
    }

    override fun addInformation(stack: ItemStack, world: World?, tooltip: MutableList<String>, advanced: ITooltipFlag) {
        tooltip += I18n.format("ivremotecontrol.interface.tooltip.help0")
    }

    // ComputerCraft
    fun getPeripheral(state: IBlockState, world: World, pos: BlockPos, side: EnumFacing): IPeripheral? {
        val facing = state.getValue(FACING)

        if (facing.opposite != side) {
            return null
        }

        return world.getTileEntity(pos) as InterfaceTileEntity
    }

    // Utility
    fun getRange(state: IBlockState, world: World, pos: BlockPos): Double {
        val selfFacing = state.getValue(FACING)

        val antennaPos = pos.offset(selfFacing)
        val antennaState = world.getBlockState(antennaPos)
        val antennaBlock = antennaState.block

        if (antennaBlock !is AntennaBlock) {
            return 0.0
        }

        return antennaBlock.getRange(antennaState, world, antennaPos)
    }

    // Abstracts
    abstract fun getGuiId(): Int
}