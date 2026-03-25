package me.cioco.playerindicatorplus.mixin;

import me.cioco.playerindicatorplus.Main;
import me.cioco.playerindicatorplus.config.PlayerIndicatorConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.Color;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Mixin(EntityRenderer.class)
public abstract class NumericHealthDisplayMixin<T extends Entity, S extends EntityRenderState> {

    @Unique
    private final Map<EntityRenderState, Entity> renderStateCache = new ConcurrentHashMap<>();

    @Inject(
            method = "extractRenderState(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/client/renderer/entity/state/EntityRenderState;F)V",
            at = @At("RETURN")
    )
    private void captureEntityReference(T entity, S renderState, float partialTick, CallbackInfo ci) {
        if (entity instanceof Player) {
            renderStateCache.put(renderState, entity);
        }
    }

    @Inject(
            method = "submit(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
            at = @At("RETURN")
    )
    private void renderHealthArmorInfoEffects(S renderState, PoseStack poseStack,
                                              net.minecraft.client.renderer.SubmitNodeCollector submitNodeCollector,
                                              net.minecraft.client.renderer.state.level.CameraRenderState camera,
                                              CallbackInfo ci) {
        if (!Main.toggled) return;

        Entity cached = renderStateCache.get(renderState);
        if (!(cached instanceof Player player)) return;
        if (shouldSkipRendering(player)) return;

        var bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();

        if (PlayerIndicatorConfig.showPing || PlayerIndicatorConfig.showDistance)
            renderInfoLine(player, poseStack, bufferSource, renderState.lightCoords);

        if (PlayerIndicatorConfig.showMainHand || PlayerIndicatorConfig.showOffHand)
            renderEquipment(player, poseStack, bufferSource, renderState.lightCoords);

        if (PlayerIndicatorConfig.showHealthNumbers)
            displayHealthAbovePlayer(player, poseStack, bufferSource, renderState.lightCoords);

        if (PlayerIndicatorConfig.showArmorPercentages || PlayerIndicatorConfig.showArmorText)
            renderArmorPercentagesAbovePlayer(player, poseStack, bufferSource, renderState.lightCoords);

    }

    @Unique
    private void renderEquipment(Player player, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        StringBuilder sb = new StringBuilder();

        if (PlayerIndicatorConfig.showMainHand) {
            ItemStack main = player.getMainHandItem();
            if (!main.isEmpty()) sb.append(main.getHoverName().getString());
        }

        if (PlayerIndicatorConfig.showOffHand) {
            ItemStack off = player.getOffhandItem();
            if (!off.isEmpty()) {
                if (sb.length() > 0) sb.append(" | ");
                sb.append(off.getHoverName().getString());
            }
        }

        if (sb.length() == 0) return;

        renderTextAtHeight(sb.toString(), player,
                PlayerIndicatorConfig.equipmentHeightAboveHead,
                PlayerIndicatorConfig.equipmentTextSize,
                60f, 1f, 1f,
                poseStack, bufferSource, packedLight);
    }

    @Unique
    private void renderTextAtHeight(String text, Player player, float yOffsetValue, float size,
                                    float h, float s, float b,
                                    PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        Minecraft mc = Minecraft.getInstance();
        Font font = mc.font;

        poseStack.pushPose();
        poseStack.translate(0, player.getBbHeight() + yOffsetValue, 0);

        var cam = mc.gameRenderer.getMainCamera();
        poseStack.mulPose(Axis.YP.rotationDegrees(-cam.yRot()));
        poseStack.mulPose(Axis.XP.rotationDegrees(cam.xRot()));
        poseStack.scale(-size, -size, size);

        int color = 0xFF000000 | Color.HSBtoRGB(h / 360f, s, b);

        font.drawInBatch(text,
                -font.width(text) / 2f, 0,
                color, false,
                poseStack.last().pose(),
                bufferSource,
                Font.DisplayMode.SEE_THROUGH,
                0x50000000, packedLight);

        poseStack.popPose();
    }

    @Unique
    private void renderInfoLine(Player player, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        Minecraft mc = Minecraft.getInstance();
        StringBuilder info = new StringBuilder();

        if (PlayerIndicatorConfig.showDistance && mc.getCameraEntity() != null) {
            double dist = Math.sqrt(player.distanceToSqr(mc.getCameraEntity()));
            info.append(String.format("%.1fm", dist));
        }

        if (PlayerIndicatorConfig.showPing && mc.getConnection() != null) {
            PlayerInfo entry = mc.getConnection().getPlayerInfo(player.getUUID());
            String ping = (entry != null) ? entry.getLatency() + "ms"
                    : (mc.hasSingleplayerServer() ? "Local" : "?");
            if (info.length() > 0) info.append(" | ");
            info.append(ping);
        }

        if (info.isEmpty()) return;

        renderTextAtHeight(info.toString(), player,
                PlayerIndicatorConfig.infoHeightAboveHead,
                PlayerIndicatorConfig.infoTextSize,
                PlayerIndicatorConfig.infoTextHue,
                PlayerIndicatorConfig.infoTextSaturation,
                PlayerIndicatorConfig.infoTextBrightness,
                poseStack, bufferSource, packedLight);
    }

    @Unique
    private void displayHealthAbovePlayer(Player player, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        String text = player.isCreative()
                ? "Creative"
                : (int) Math.ceil(player.getHealth() + player.getAbsorptionAmount()) + "HP";

        renderTextAtHeight(text, player,
                PlayerIndicatorConfig.heightAboveHead,
                PlayerIndicatorConfig.healthTextSize,
                PlayerIndicatorConfig.healthTextHue,
                PlayerIndicatorConfig.healthTextSaturation,
                PlayerIndicatorConfig.healthTextBrightness,
                poseStack, bufferSource, packedLight);
    }

    @Unique
    private void renderArmorPercentagesAbovePlayer(Player player, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        if (!hasAnyEquippedArmor(player)) return;

        StringBuilder armor = new StringBuilder();
        EquipmentSlot[] slots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

        for (EquipmentSlot slot : slots) {
            ItemStack stack = player.getItemBySlot(slot);
            if (stack.isEmpty()) continue;

            String part = "";
            if (PlayerIndicatorConfig.showArmorText) {
                String name = stack.getItem().getDescriptionId().toLowerCase();
                String tier = name.contains("leather") ? "L"
                        : name.contains("chain")     ? "Ch"
                        : name.contains("iron")      ? "I"
                        : name.contains("gold")      ? "G"
                        : name.contains("diamond")   ? "D"
                        : name.contains("netherite") ? "N"
                        : name.contains("elytra")    ? "E" : "?";
                String type = slot == EquipmentSlot.HEAD  ? "H"
                        : slot == EquipmentSlot.CHEST ? "C"
                        : slot == EquipmentSlot.LEGS  ? "L" : "B";
                part = tier + type;
            }

            if (PlayerIndicatorConfig.showArmorPercentages) {
                int pct = stack.isDamageableItem()
                        ? Math.round(((stack.getMaxDamage() - stack.getDamageValue()) / (float) stack.getMaxDamage()) * 100f)
                        : 100;
                part += (part.isEmpty() ? "" : " ") + pct + "%";
            }

            if (!part.isEmpty()) armor.append(part).append(" | ");
        }

        if (armor.length() > 3) armor.setLength(armor.length() - 3);

        renderTextAtHeight(armor.toString(), player,
                PlayerIndicatorConfig.armorheightAboveHead,
                PlayerIndicatorConfig.armorTextSize,
                PlayerIndicatorConfig.armorTextHue,
                PlayerIndicatorConfig.armorTextSaturation,
                PlayerIndicatorConfig.armorTextBrightness,
                poseStack, bufferSource, packedLight);
    }

    @Unique
    private boolean shouldSkipRendering(Player player) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && player.getUUID().equals(mc.player.getUUID())) return true;
        if (mc.getCameraEntity() != null) {
            double distSq = mc.getCameraEntity().distanceToSqr(player);
            if (distSq > PlayerIndicatorConfig.healthVisibilityRange * PlayerIndicatorConfig.healthVisibilityRange) return true;
        }
        return player.isInvisible() && !PlayerIndicatorConfig.showInvisiblePlayers;
    }

    @Unique
    private static boolean hasAnyEquippedArmor(Player player) {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR && !player.getItemBySlot(slot).isEmpty()) return true;
        }
        return false;
    }
}