package me.ste.ivremotecontrol.block.iface

import me.ste.ivremotecontrol.IVRemoteControl
import me.ste.ivremotecontrol.tab.IVRCCreativeTab
import net.minecraft.block.BlockDirectional
import net.minecraft.block.ITileEntityProvider
import net.minecraft.block.SoundType
import net.minecraft.block.material.Material
import net.minecraft.block.state.BlockStateContainer
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.EntityLivingBase
import net.minecraft.item.ItemBlock
import net.minecraft.item.ItemStack
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

abstract class InterfaceBlock(
    private val id: String
) : BlockDirectional(Material.ROCK), ITileEntityProvider {
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

    override fun onBlockPlacedBy(
        world: World,
        pos: BlockPos,
        state: IBlockState,
        placer: EntityLivingBase,
        stack: ItemStack
    ) {
        world.setBlockState(pos, state.withProperty(FACING, EnumFacing.getDirectionFromEntityLiving(pos, placer)))
        super.onBlockPlacedBy(world, pos, state, placer, stack)
    }
}