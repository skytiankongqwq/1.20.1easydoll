package com.Interest.Doll;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.resources.SkinManager;

import java.util.function.Consumer;
import java.util.UUID;

public class PlushieItem extends BlockItem {
    public PlushieItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return PlushieItemRenderer.INSTANCE;
            }
        });
    }

    /**
     * 修复版皮肤获取逻辑
     */
    public static ResourceLocation getPlayerSkin(ItemStack stack) {
        if (stack.hasCustomHoverName()) {
            String name = stack.getHoverName().getString();

            // 排除默认名
            if (name.isEmpty() || name.equalsIgnoreCase("plushie_doll")) {
                return DefaultPlayerSkin.getDefaultSkin();
            }

            Minecraft minecraft = Minecraft.getInstance();
            // 为名字生成一个确定的离线 UUID，这是获取正版皮肤缓存的关键
            UUID uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(java.nio.charset.StandardCharsets.UTF_8));
            GameProfile profile = new GameProfile(uuid, name);

            // 使用更稳定的 registerSkins 或 getInsecureSkinInformation
            // 这里我们采用最保险的逻辑：让 SkinManager 预取
            return minecraft.getSkinManager().getInsecureSkinInformation(profile)
                    .getOrDefault(com.mojang.authlib.minecraft.MinecraftProfileTexture.Type.SKIN,
                            null) != null
                    ? minecraft.getSkinManager().registerTexture(
                    minecraft.getSkinManager().getInsecureSkinInformation(profile).get(com.mojang.authlib.minecraft.MinecraftProfileTexture.Type.SKIN),
                    com.mojang.authlib.minecraft.MinecraftProfileTexture.Type.SKIN)
                    : DefaultPlayerSkin.getDefaultSkin(uuid);
        }

        return DefaultPlayerSkin.getDefaultSkin();
    }
}