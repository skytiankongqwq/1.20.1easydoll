package com.Interest.Doll;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class PlushieBlockEntity extends BlockEntity {
    private GameProfile ownerProfile;

    public PlushieBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PLUSHIE_BE.get(), pos, state);
    }

    // 提供给渲染器获取 Profile
    @Nullable
    public GameProfile getOwnerProfile() {
        return ownerProfile;
    }

    public void setOwner(@Nullable GameProfile profile) {
        this.ownerProfile = profile;
        this.updateProfile();
    }

    private void updateProfile() {
        if (this.ownerProfile != null && this.ownerProfile.getName() != null) {
            SkullBlockEntity.updateGameprofile(this.ownerProfile, (profile) -> {
                this.ownerProfile = profile;
                this.setChanged();
                if (level != null && !level.isClientSide) {
                    level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
                }
            });
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (this.ownerProfile != null) {
            CompoundTag profileTag = new CompoundTag();
            NbtUtils.writeGameProfile(profileTag, this.ownerProfile);
            tag.put("Owner", profileTag);
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("Owner", 10)) {
            this.ownerProfile = NbtUtils.readGameProfile(tag.getCompound("Owner"));
        }
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }
}