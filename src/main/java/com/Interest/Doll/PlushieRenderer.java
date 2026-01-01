package com.Interest.Doll;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

public class PlushieRenderer implements BlockEntityRenderer<PlushieBlockEntity> {
    private final PlushieModel model;

    public PlushieRenderer(BlockEntityRendererProvider.Context context) {
        this.model = new PlushieModel(context.bakeLayer(ModModelLayers.PLUSHIE_LAYER));
    }

    @Override
    public void render(PlushieBlockEntity entity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        GameProfile profile = entity.getOwnerProfile();
        ResourceLocation texture = DefaultPlayerSkin.getDefaultSkin();

        if (profile != null && profile.getName() != null) {
            texture = Minecraft.getInstance().getSkinManager().getInsecureSkinLocation(profile);
        }

        BlockState state = entity.getBlockState();

        poseStack.pushPose();
        poseStack.translate(0.5, 0, 0.5);
        if (state.hasProperty(PlushieBlock.ROTATION)) {
            int rotation = state.getValue(PlushieBlock.ROTATION);
            float angle = (float)(rotation * 360) / 16.0F;
            poseStack.mulPose(Axis.YP.rotationDegrees(-angle));
        }
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        poseStack.translate(0, -1.5F, 0);

        VertexConsumer buffer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(texture));
        this.model.renderToBuffer(poseStack, buffer, packedLight, packedOverlay, 1.0F, 1.0F, 1.0F, 1.0F);

        poseStack.popPose();
    }
}