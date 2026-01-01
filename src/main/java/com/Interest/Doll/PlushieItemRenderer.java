package com.Interest.Doll;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;

public class PlushieItemRenderer extends BlockEntityWithoutLevelRenderer {
    public static final PlushieItemRenderer INSTANCE = new PlushieItemRenderer();
    private PlushieModel model;

    public PlushieItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack matrix, MultiBufferSource buffer, int light, int overlay) {
        if (this.model == null) {
            this.model = new PlushieModel(Minecraft.getInstance().getEntityModels().bakeLayer(ModModelLayers.PLUSHIE_LAYER));
        }

        // --- 同步正版皮肤 ---
        ResourceLocation skin = DefaultPlayerSkin.getDefaultSkin();
        GameProfile profile = null;
        CompoundTag tag = stack.getTag();

        if (tag != null && tag.contains("Owner", 10)) {
            profile = NbtUtils.readGameProfile(tag.getCompound("Owner"));
        } else if (stack.hasCustomHoverName()) {
            String name = stack.getHoverName().getString();
            profile = new GameProfile(null, name);
        }

        if (profile != null) {
            // 自动加载/获取该名字对应的正版皮肤
            skin = Minecraft.getInstance().getSkinManager().getInsecureSkinLocation(profile);
        }

        matrix.pushPose();

        if (context == ItemDisplayContext.GUI) {
            // --- 逻辑 A：物品栏 2D 面部 ---
            VertexConsumer builder = buffer.getBuffer(RenderType.entityTranslucent(skin));
            Matrix4f mat = matrix.last().pose();

            // 脸部底层 (UV 8,8 -> 16,16)
            drawFaceRect(builder, mat, 0.125f, 0.25f, 0.25f, 0.125f, light, overlay);
            // 脸部外层 (UV 40,8 -> 48,16)
            drawFaceRect(builder, mat, 0.625f, 0.25f, 0.75f, 0.125f, light, overlay);

        } else {
            // --- 逻辑 B：手持/展示框 3D 模型  ---
            if (context == ItemDisplayContext.FIXED) {
                // 展示框
                matrix.translate(0.5, 1.6, 0.5);
                // 2. 放大倍率：保持 1.1f 或稍微减小到 1.0f 防止脚部穿模过深
                float fixedScale = 1.1f;
                matrix.scale(-fixedScale, -fixedScale, fixedScale);
                // 3. 保持 180 度正脸
                matrix.mulPose(Axis.YP.rotationDegrees(180.0F));
            } else {
                // 第一人称、第三人称手持及地面掉落
                matrix.translate(0.5, 1.2, 0.5);
                matrix.scale(-0.4f, -0.4f, 0.4f);
            }

            matrix.mulPose(Axis.YP.rotationDegrees(180.0F));

            VertexConsumer builder = ItemRenderer.getFoilBufferDirect(
                    buffer,
                    RenderType.entityCutoutNoCull(skin),
                    true,
                    stack.hasFoil()
            );
            this.model.renderToBuffer(matrix, builder, light, overlay, 1.0f, 1.0f, 1.0f, 1.0f);
        }

        matrix.popPose();
    }

    /**
     * 2D 矩形绘制助手，确保顶点格式完整
     */
    private void drawFaceRect(VertexConsumer builder, Matrix4f mat, float u0, float v0, float u1, float v1, int light, int overlay) {
        // 顶点顺序：左下 -> 右下 -> 右上 -> 左上
        builder.vertex(mat, 0, 1, 0).color(255, 255, 255, 255).uv(u0, v1).overlayCoords(overlay).uv2(light).normal(0, 0, 1).endVertex();
        builder.vertex(mat, 1, 1, 0).color(255, 255, 255, 255).uv(u1, v1).overlayCoords(overlay).uv2(light).normal(0, 0, 1).endVertex();
        builder.vertex(mat, 1, 0, 0).color(255, 255, 255, 255).uv(u1, v0).overlayCoords(overlay).uv2(light).normal(0, 0, 1).endVertex();
        builder.vertex(mat, 0, 0, 0).color(255, 255, 255, 255).uv(u0, v0).overlayCoords(overlay).uv2(light).normal(0, 0, 1).endVertex();
    }
}