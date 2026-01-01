package com.Interest.Doll;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class PlushieBlock extends BaseEntityBlock {
    // 1. 定义旋转属性 (0-15，模仿头颅的 16 个朝向)
    public static final IntegerProperty ROTATION = BlockStateProperties.ROTATION_16;
    protected static final VoxelShape SHAPE = Shapes.box(0.2, 0.0, 0.2, 0.8, 0.9, 0.8);

    public PlushieBlock(Properties properties) {
        super(properties);
        // 默认状态设为 0
        this.registerDefaultState(this.stateDefinition.any().setValue(ROTATION, 0));
    }

    // 2. 核心：放置时计算玩家面朝的方向
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        // 计算玩家偏航角并映射到 0-15 的整数
        return this.defaultBlockState().setValue(ROTATION, Mth.floor((double) (context.getRotation() * 16.0F / 360.0F) + 0.5D) & 15);
    }

    // 3. 注册属性
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ROTATION);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PlushieBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        BlockEntity blockentity = level.getBlockEntity(pos);
        if (blockentity instanceof PlushieBlockEntity plushieBE) {
            if (!level.isClientSide && !player.isCreative()) {
                ItemStack itemstack = new ItemStack(this);
                com.mojang.authlib.GameProfile profile = plushieBE.getOwnerProfile();
                if (profile != null) {
                    CompoundTag tag = itemstack.getOrCreateTag();
                    CompoundTag ownerTag = new CompoundTag();
                    net.minecraft.nbt.NbtUtils.writeGameProfile(ownerTag, profile);
                    tag.put("Owner", ownerTag);
                    itemstack.setHoverName(net.minecraft.network.chat.Component.literal(profile.getName()));
                }
                ItemEntity itementity = new ItemEntity(level,
                        pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, itemstack);
                itementity.setDefaultPickUpDelay();
                level.addFreshEntity(itementity);
            }
        }
        super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        return Collections.emptyList();
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof PlushieBlockEntity plushieBE) {
            if (stack.hasCustomHoverName()) {
                String name = stack.getHoverName().getString();
                plushieBE.setOwner(new com.mojang.authlib.GameProfile(null, name));
            } else {
                plushieBE.setOwner(null);
            }
        }
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter reader, BlockPos pos) {
        return true;
    }
}