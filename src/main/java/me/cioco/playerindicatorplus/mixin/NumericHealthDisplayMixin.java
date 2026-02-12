package me.cioco.playerindicatorplus.mixin;

import me.cioco.playerindicatorplus.config.PlayerIndicatorConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.RotationAxis;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Mixin(LivingEntityRenderer.class)
public abstract class NumericHealthDisplayMixin {

    @Unique
    private final Map<LivingEntityRenderState, LivingEntity> renderStateCache = new ConcurrentHashMap<>();

    @Inject(
            method = "updateRenderState(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;F)V",
            at = @At("RETURN")
    )
    private void captureEntityReference(LivingEntity livingEntity, LivingEntityRenderState renderState, float partialTick, CallbackInfo ci) {
        renderStateCache.put(renderState, livingEntity);
    }

    @Inject(
            method = "render(Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;Lnet/minecraft/client/render/state/CameraRenderState;)V",
            at = @At("RETURN")
    )
    private void renderHealthArmorInfoEffects(LivingEntityRenderState renderState, MatrixStack matrices,
                                              net.minecraft.client.render.command.OrderedRenderCommandQueue renderQueue,
                                              net.minecraft.client.render.state.CameraRenderState cameraState,
                                              CallbackInfo ci) {
        if (!(renderState instanceof PlayerEntityRenderState)) return;

        LivingEntity cachedEntity = renderStateCache.get(renderState);
        if (!(cachedEntity instanceof PlayerEntity player)) return;
        if (shouldSkipRendering(player)) return;

        MinecraftClient client = MinecraftClient.getInstance();
        VertexConsumerProvider.Immediate bufferSource = client.getBufferBuilders().getEntityVertexConsumers();
        int lightLevel = renderState.light;

        if (PlayerIndicatorConfig.showPing || PlayerIndicatorConfig.showDistance)
            renderInfoLine(player, matrices, bufferSource, lightLevel);

        if (PlayerIndicatorConfig.showMainHand || PlayerIndicatorConfig.showOffHand)
            renderEquipment(player, matrices, bufferSource, lightLevel);

        if (PlayerIndicatorConfig.showHealthNumbers)
            displayHealthAbovePlayer(player, matrices, bufferSource, lightLevel);

        if (PlayerIndicatorConfig.showArmorPercentages || PlayerIndicatorConfig.showArmorText)
            renderArmorPercentagesAbovePlayer(player, matrices, bufferSource, lightLevel);

        bufferSource.draw();
    }

    @Unique
    private void renderEquipment(PlayerEntity player, MatrixStack matrices, VertexConsumerProvider vertexProvider, int packedLight) {
        StringBuilder sb = new StringBuilder();

        if (PlayerIndicatorConfig.showMainHand) {
            ItemStack main = player.getMainHandStack();
            if (!main.isEmpty()) sb.append(main.getName().getString());
        }

        if (PlayerIndicatorConfig.showOffHand) {
            ItemStack off = player.getOffHandStack();
            if (!off.isEmpty()) {
                if (sb.length() > 0) sb.append(" | ");
                sb.append(off.getName().getString());
            }
        }

        if (sb.length() == 0) return;

        renderTextAtHeight(sb.toString(), player, PlayerIndicatorConfig.equipmentHeightAboveHead, PlayerIndicatorConfig.equipmentTextSize,
                60f, 1f, 1f, matrices, vertexProvider, packedLight);
    }

    @Unique
    private void renderTextAtHeight(String text, PlayerEntity player, float yOffsetValue, float size, float h, float s, float b,
                                    MatrixStack matrices, VertexConsumerProvider vertexProvider, int packedLight) {
        MinecraftClient mc = MinecraftClient.getInstance();
        TextRenderer font = mc.textRenderer;

        matrices.push();
        float yOffset = player.getHeight() + yOffsetValue;
        matrices.translate(0, yOffset, 0);

        var cam = mc.gameRenderer.getCamera();
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-cam.getYaw()));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(cam.getPitch()));

        matrices.scale(-size, -size, size);

        int color = 0xFF000000 | Color.HSBtoRGB(h / 360f, s, b);

        font.draw(text, -font.getWidth(text) / 2f, 0, color, false,
                matrices.peek().getPositionMatrix(), vertexProvider,
                TextRenderer.TextLayerType.SEE_THROUGH, 0x50000000, packedLight);

        matrices.pop();
    }

    @Unique
    private void renderInfoLine(PlayerEntity player, MatrixStack matrices, VertexConsumerProvider vertexProvider, int packedLight) {
        MinecraftClient mc = MinecraftClient.getInstance();
        StringBuilder infoBuilder = new StringBuilder();

        if (PlayerIndicatorConfig.showDistance && mc.getCameraEntity() != null) {
            double dist = Math.sqrt(player.squaredDistanceTo(mc.getCameraEntity()));
            infoBuilder.append(String.format("%.1fm", dist));
        }

        if (PlayerIndicatorConfig.showPing && mc.getNetworkHandler() != null) {
            PlayerListEntry entry = mc.getNetworkHandler().getPlayerListEntry(player.getUuid());
            String pingText = (entry != null) ? entry.getLatency() + "ms" : (mc.isIntegratedServerRunning() ? "Local" : "?");
            if (infoBuilder.length() > 0) infoBuilder.append(" | ");
            infoBuilder.append(pingText);
        }

        String text = infoBuilder.toString();
        if (text.isEmpty()) return;

        renderTextAtHeight(text, player, PlayerIndicatorConfig.infoHeightAboveHead, PlayerIndicatorConfig.infoTextSize,
                PlayerIndicatorConfig.infoTextHue, PlayerIndicatorConfig.infoTextSaturation, PlayerIndicatorConfig.infoTextBrightness,
                matrices, vertexProvider, packedLight);
    }

    @Unique
    private void displayHealthAbovePlayer(PlayerEntity player, MatrixStack matrices, VertexConsumerProvider vertexProvider, int packedLight) {
        String displayText = player.isCreative() ? "Creative" : (int) Math.ceil(player.getHealth() + player.getAbsorptionAmount()) + "HP";

        renderTextAtHeight(displayText, player, PlayerIndicatorConfig.heightAboveHead, PlayerIndicatorConfig.healthTextSize,
                PlayerIndicatorConfig.healthTextHue, PlayerIndicatorConfig.healthTextSaturation, PlayerIndicatorConfig.healthTextBrightness,
                matrices, vertexProvider, packedLight);
    }

    @Unique
    private void renderArmorPercentagesAbovePlayer(PlayerEntity player, MatrixStack matrices, VertexConsumerProvider vertexProvider, int packedLight) {
        if (!hasAnyEquippedArmor(player)) return;

        StringBuilder armorDisplay = new StringBuilder();
        EquipmentSlot[] slots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

        for (EquipmentSlot slot : slots) {
            ItemStack stack = player.getEquippedStack(slot);
            if (stack.isEmpty()) continue;

            String partInfo = "";
            if (PlayerIndicatorConfig.showArmorText) {
                String name = stack.getItem().getTranslationKey().toLowerCase();
                String tier = name.contains("leather") ? "L" : name.contains("chain") ? "Ch" : name.contains("iron") ? "I" :
                        name.contains("gold") ? "G" : name.contains("diamond") ? "D" : name.contains("netherite") ? "N" :
                                name.contains("elytra") ? "E" : "?";
                String type = slot == EquipmentSlot.HEAD ? "H" : slot == EquipmentSlot.CHEST ? "C" : slot == EquipmentSlot.LEGS ? "L" : "B";
                partInfo = tier + type;
            }

            if (PlayerIndicatorConfig.showArmorPercentages) {
                int percent = stack.isDamageable() ? Math.round(((stack.getMaxDamage() - stack.getDamage()) / (float) stack.getMaxDamage()) * 100f) : 100;
                partInfo += (partInfo.isEmpty() ? "" : " ") + percent + "%";
            }
            if (!partInfo.isEmpty()) armorDisplay.append(partInfo).append(" | ");
        }

        if (armorDisplay.length() > 3) armorDisplay.setLength(armorDisplay.length() - 3);

        renderTextAtHeight(armorDisplay.toString(), player, PlayerIndicatorConfig.armorheightAboveHead, PlayerIndicatorConfig.armorTextSize,
                PlayerIndicatorConfig.armorTextHue, PlayerIndicatorConfig.armorTextSaturation, PlayerIndicatorConfig.armorTextBrightness,
                matrices, vertexProvider, packedLight);
    }

    @Unique
    private boolean shouldSkipRendering(PlayerEntity player) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null && player.getUuid().equals(mc.player.getUuid())) return true;
        if (mc.getCameraEntity() != null) {
            double distSq = mc.getCameraEntity().squaredDistanceTo(player);
            if (distSq > PlayerIndicatorConfig.healthVisibilityRange * PlayerIndicatorConfig.healthVisibilityRange) return true;
        }
        return player.isInvisible() && !PlayerIndicatorConfig.showInvisiblePlayers;
    }

    @Unique
    private static boolean hasAnyEquippedArmor(PlayerEntity player) {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR && !player.getEquippedStack(slot).isEmpty()) return true;
        }
        return false;
    }
}