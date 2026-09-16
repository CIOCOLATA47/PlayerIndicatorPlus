package me.cioco.playerindicatorplus.mixin;

import me.cioco.playerindicatorplus.config.PlayerIndicatorConfig;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.AtlasManager;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import org.joml.Matrix4f;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.Color;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Mixin(EntityRenderer.class)
public abstract class PlayerIndicatorMixin<T extends Entity, S extends EntityRenderState> {

    @Unique
    private static final Map<EntityRenderState, Entity> renderStateCache = new ConcurrentHashMap<>();

    @Unique
    private static final Set<EntityRenderState> renderedThisTick =
            Collections.newSetFromMap(new ConcurrentHashMap<>());

    @Unique
    private static long lastTick = -1L;

    @Unique
    private static void ensureTickReset() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        long tick = mc.level.getGameTime();
        if (tick != lastTick) {
            lastTick = tick;
            renderedThisTick.clear();
        }
    }

    @Inject(
            method = "extractRenderState(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/client/renderer/entity/state/EntityRenderState;F)V",
            at = @At("RETURN")
    )
    private void captureEntityReference(T entity, S state, float partialTicks, CallbackInfo ci) {
        if (entity instanceof Player) {
            renderStateCache.put(state, entity);
        }
    }

    @Inject(
            method = "submit(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
            at = @At("RETURN")
    )
    private void renderAllIndicators(
            S state,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            CameraRenderState camera,
            CallbackInfo ci
    ) {
        if (!PlayerIndicatorConfig.toggled) {
            return;
        }

        ensureTickReset();

        if (!renderedThisTick.add(state)) {
            return;
        }

        Entity cached = renderStateCache.get(state);
        if (!(cached instanceof Player player)) {
            return;
        }

        if (shouldSkipRendering(player)) {
            return;
        }

        int packedLight = state.lightCoords;

        if (PlayerIndicatorConfig.showPing || PlayerIndicatorConfig.showDistance) {
            renderInfoLine(player, poseStack, submitNodeCollector, packedLight);
        }

        if (PlayerIndicatorConfig.showMainHand || PlayerIndicatorConfig.showOffHand) {
            renderEquipment(player, poseStack, submitNodeCollector, packedLight);
        }

        if (PlayerIndicatorConfig.showHealthNumbers) {
            displayHealthAbovePlayer(player, poseStack, submitNodeCollector, packedLight);
        }

        if (PlayerIndicatorConfig.showArmorPercentages || PlayerIndicatorConfig.showArmorText) {
            renderArmorPercentagesAbovePlayer(player, poseStack, submitNodeCollector, packedLight);
        }

        if (PlayerIndicatorConfig.showHearts) {
            renderHeartsAbovePlayer(player, poseStack, submitNodeCollector, packedLight);
        }

        if (PlayerIndicatorConfig.showArmorBars) {
            renderArmorBarAbovePlayer(player, poseStack, submitNodeCollector, packedLight);
        }
    }

    @Unique
    private boolean shouldSkipRendering(Player player) {
        Minecraft mc = Minecraft.getInstance();

        if (mc.player != null && player.getUUID().equals(mc.player.getUUID())) {
            return true;
        }

        if (mc.getCameraEntity() != null) {
            double distanceSquared = mc.getCameraEntity().distanceToSqr(player);
            double maxDistance = PlayerIndicatorConfig.healthVisibilityRange;
            if (distanceSquared > maxDistance * maxDistance) {
                return true;
            }
        }

        return player.isInvisible() && !PlayerIndicatorConfig.showInvisiblePlayers;
    }

    @Unique
    private void renderTextAtHeight(
            String text,
            Player player,
            float yOffsetValue,
            float size,
            float h,
            float s,
            float b,
            PoseStack poseStack,
            SubmitNodeCollector collector,
            int packedLight
    ) {
        Minecraft mc = Minecraft.getInstance();
        Font font = mc.font;

        poseStack.pushPose();
        poseStack.translate(0.0F, player.getBbHeight() + yOffsetValue, 0.0F);

        var camera = mc.gameRenderer.mainCamera();
        poseStack.rotateDegrees(Axis.YP, -camera.yRot());
        poseStack.rotateDegrees(Axis.XP, camera.xRot());
        poseStack.scale(-size, -size, size);

        int color = 0xFF000000 | Color.HSBtoRGB(h / 360.0F, s, b);
        float x = -font.width(text) / 2.0F;
        Component component = Component.literal(text);
        var visualOrder = font.split(component, Integer.MAX_VALUE).getFirst();

        collector.submitText(
                poseStack,
                x,
                0.0F,
                visualOrder,
                false,
                Font.DisplayMode.SEE_THROUGH,
                packedLight,
                color,
                0x50000000,
                0
        );

        poseStack.popPose();
    }

    @Unique
    private void renderInfoLine(
            Player player,
            PoseStack poseStack,
            SubmitNodeCollector collector,
            int packedLight
    ) {
        Minecraft mc = Minecraft.getInstance();
        StringBuilder info = new StringBuilder();

        if (PlayerIndicatorConfig.showDistance && mc.getCameraEntity() != null) {
            double distance = Math.sqrt(player.distanceToSqr(mc.getCameraEntity()));
            info.append(String.format("%.1fm", distance));
        }

        if (PlayerIndicatorConfig.showPing && mc.getConnection() != null) {
            PlayerInfo entry = mc.getConnection().getPlayerInfo(player.getUUID());
            String ping = entry != null ? entry.getLatency() + "ms" : (mc.hasSingleplayerServer() ? "Local" : "?");

            if (!info.isEmpty()) {
                info.append(" | ");
            }
            info.append(ping);
        }

        if (info.isEmpty()) {
            return;
        }

        renderTextAtHeight(
                info.toString(),
                player,
                PlayerIndicatorConfig.infoHeightAboveHead,
                PlayerIndicatorConfig.infoTextSize,
                PlayerIndicatorConfig.infoTextHue,
                PlayerIndicatorConfig.infoTextSaturation,
                PlayerIndicatorConfig.infoTextBrightness,
                poseStack,
                collector,
                packedLight
        );
    }

    @Unique
    private void renderEquipment(
            Player player,
            PoseStack poseStack,
            SubmitNodeCollector collector,
            int packedLight
    ) {
        StringBuilder text = new StringBuilder();

        if (PlayerIndicatorConfig.showMainHand) {
            ItemStack main = player.getMainHandItem();
            if (!main.isEmpty()) {
                text.append(main.getHoverName().getString());
            }
        }

        if (PlayerIndicatorConfig.showOffHand) {
            ItemStack off = player.getOffhandItem();
            if (!off.isEmpty()) {
                if (!text.isEmpty()) {
                    text.append(" | ");
                }
                text.append(off.getHoverName().getString());
            }
        }

        if (text.isEmpty()) {
            return;
        }

        renderTextAtHeight(
                text.toString(),
                player,
                PlayerIndicatorConfig.equipmentHeightAboveHead,
                PlayerIndicatorConfig.equipmentTextSize,
                60.0F,
                1.0F,
                1.0F,
                poseStack,
                collector,
                packedLight
        );
    }

    @Unique
    private float getTabListHealth(Player player) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return -1.0F;
        }

        var scoreboard = mc.level.getScoreboard();
        var objective = scoreboard.getDisplayObjective(net.minecraft.world.scores.DisplaySlot.LIST);
        if (objective == null) {
            return -1.0F;
        }

        var score = scoreboard.getPlayerScoreInfo(player, objective);
        if (score == null) {
            return -1.0F;
        }

        return score.value();
    }

    @Unique
    private void displayHealthAbovePlayer(
            Player player,
            PoseStack poseStack,
            SubmitNodeCollector collector,
            int packedLight
    ) {
        String text;

        if (player.isCreative()) {
            text = "Creative";
        } else if (PlayerIndicatorConfig.useTabListHealth) {
            float tabHealth = getTabListHealth(player);
            if (tabHealth >= 0.0F) {
                text = (int) Math.ceil(tabHealth) + "HP";
            } else {
                text = (int) Math.ceil(player.getHealth() + player.getAbsorptionAmount()) + "HP";
            }
        } else {
            text = (int) Math.ceil(player.getHealth() + player.getAbsorptionAmount()) + "HP";
        }

        renderTextAtHeight(
                text,
                player,
                PlayerIndicatorConfig.heightAboveHead,
                PlayerIndicatorConfig.healthTextSize,
                PlayerIndicatorConfig.healthTextHue,
                PlayerIndicatorConfig.healthTextSaturation,
                PlayerIndicatorConfig.healthTextBrightness,
                poseStack,
                collector,
                packedLight
        );
    }

    @Unique
    private String extractArmorTier(String name) {
        if (name.contains("leather")) return "L";
        if (name.contains("chain")) return "Ch";
        if (name.contains("iron")) return "I";
        if (name.contains("gold")) return "G";
        if (name.contains("diamond")) return "D";
        if (name.contains("netherite")) return "N";
        if (name.contains("elytra")) return "E";
        return "?";
    }

    @Unique
    private void renderArmorPercentagesAbovePlayer(
            Player player,
            PoseStack poseStack,
            SubmitNodeCollector collector,
            int packedLight
    ) {
        if (!hasAnyEquippedArmor(player)) {
            return;
        }

        StringBuilder armor = new StringBuilder();
        EquipmentSlot[] slots = {
                EquipmentSlot.HEAD,
                EquipmentSlot.CHEST,
                EquipmentSlot.LEGS,
                EquipmentSlot.FEET
        };

        for (EquipmentSlot slot : slots) {
            ItemStack stack = player.getItemBySlot(slot);
            if (stack.isEmpty()) {
                continue;
            }

            String part = "";
            if (PlayerIndicatorConfig.showArmorText) {
                String name = stack.getItem().getDescriptionId().toLowerCase();
                String tier = extractArmorTier(name);

                String type = slot == EquipmentSlot.HEAD ? "H"
                        : slot == EquipmentSlot.CHEST ? "C"
                          : slot == EquipmentSlot.LEGS ? "L"
                            : "B";

                part = tier + type;
            }

            if (PlayerIndicatorConfig.showArmorPercentages) {
                int percentage;
                if (stack.isDamageableItem()) {
                    percentage = Math.round(
                            ((stack.getMaxDamage() - stack.getDamageValue()) / (float) stack.getMaxDamage()) * 100.0F
                    );
                } else {
                    percentage = 100;
                }

                part += (part.isEmpty() ? "" : " ") + percentage + "%";
            }

            if (!part.isEmpty()) {
                armor.append(part).append(" | ");
            }
        }

        if (armor.length() > 3) {
            armor.setLength(armor.length() - 3);
        }

        renderTextAtHeight(
                armor.toString(),
                player,
                PlayerIndicatorConfig.armorheightAboveHead,
                PlayerIndicatorConfig.armorTextSize,
                PlayerIndicatorConfig.armorTextHue,
                PlayerIndicatorConfig.armorTextSaturation,
                PlayerIndicatorConfig.armorTextBrightness,
                poseStack,
                collector,
                packedLight
        );
    }

    @Unique
    private static boolean hasAnyEquippedArmor(Player player) {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR && !player.getItemBySlot(slot).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    @Unique
    private void renderHeartsAbovePlayer(
            Player player,
            PoseStack poseStack,
            SubmitNodeCollector collector,
            int packedLight
    ) {
        float health = player.getHealth();
        float maxHealth = player.getMaxHealth();
        float absorption = player.getAbsorptionAmount();

        int healthPoints = (int) Math.ceil(health);
        int maxHealthPoints = (int) Math.ceil(maxHealth);
        int absorptionPoints = (int) Math.ceil(absorption);

        int normalHearts = (int) Math.ceil(maxHealthPoints / 2.0F);
        int redHearts = (int) Math.ceil(healthPoints / 2.0F);
        int absorptionHearts = (int) Math.ceil(absorptionPoints / 2.0F);
        int totalHearts = normalHearts + absorptionHearts;

        if (totalHearts <= 0) {
            return;
        }

        int heartsPerRow = Math.max(1, PlayerIndicatorConfig.heartsPerRow);

        final float HEART_PIXELS = 8.0F;
        final float ROW_HEIGHT = 10.0F;

        poseStack.pushPose();
        poseStack.translate(0.0F, player.getBbHeight() + PlayerIndicatorConfig.heartHeightOffset, 0.0F);

        var camera = Minecraft.getInstance().gameRenderer.mainCamera();
        poseStack.rotateDegrees(Axis.YP, -camera.yRot());
        poseStack.rotateDegrees(Axis.XP, camera.xRot());

        float scale = Math.max(0.005F, PlayerIndicatorConfig.heartSize);
        poseStack.scale(-scale, -scale, scale);

        int maxCols = Math.min(totalHearts, heartsPerRow);
        float totalWidth = maxCols * HEART_PIXELS;
        float startX = -totalWidth / 2.0F;

        for (int heart = 0; heart < totalHearts; heart++) {
            int row = heart / heartsPerRow;
            int col = heart % heartsPerRow;

            float px = startX + col * HEART_PIXELS;
            float py = row * ROW_HEIGHT;

            String containerPath = "minecraft:hud/heart/container";
            String fillPath = null;

            if (heart < redHearts) {
                if (heart == redHearts - 1 && (healthPoints % 2 != 0)) {
                    fillPath = "minecraft:hud/heart/half";
                } else {
                    fillPath = "minecraft:hud/heart/full";
                }
            } else if (heart >= normalHearts) {
                int absIndex = heart - normalHearts;
                int absRedHearts = (int) Math.ceil(absorptionPoints / 2.0F);
                if (absIndex < absRedHearts) {
                    if (absIndex == absRedHearts - 1 && (absorptionPoints % 2 != 0)) {
                        fillPath = "minecraft:hud/heart/absorbing_half";
                    } else {
                        fillPath = "minecraft:hud/heart/absorbing_full";
                    }
                }
            }

            renderSpriteIconPair(
                    poseStack,
                    collector,
                    px,
                    py,
                    containerPath,
                    fillPath,
                    packedLight
            );
        }

        poseStack.popPose();
    }

    @Unique
    private void renderArmorBarAbovePlayer(
            Player player,
            PoseStack poseStack,
            SubmitNodeCollector collector,
            int packedLight
    ) {
        int armorPoints = player.getArmorValue();

        if (armorPoints <= 0 && !hasAnyEquippedArmor(player)) {
            return;
        }

        final int ARMOR_ICONS = 10;
        final float ICON_PIXELS = 9.0F;

        poseStack.pushPose();
        poseStack.translate(0.0F, player.getBbHeight() + PlayerIndicatorConfig.armorBarHeightOffset, 0.0F);

        var camera = Minecraft.getInstance().gameRenderer.mainCamera();

        poseStack.rotate(camera.rotation());

        float scale = Math.max(0.005F, PlayerIndicatorConfig.armorBarSize);
        poseStack.scale(-scale, -scale, scale);

        float totalWidth = ARMOR_ICONS * ICON_PIXELS;
        float startX = -totalWidth / 2.0F;

        for (int i = 0; i < ARMOR_ICONS; i++) {
            float px = startX + i * ICON_PIXELS;
            float py = 0.0F;

            String containerPath = "minecraft:hud/armor_empty";
            String fillPath = null;

            int currentArmorThreshold = i * 2;
            if (armorPoints >= currentArmorThreshold + 2) {
                fillPath = "minecraft:hud/armor_full";
            } else if (armorPoints == currentArmorThreshold + 1) {
                fillPath = "minecraft:hud/armor_half";
            }

            renderSpriteIconPair(
                    poseStack,
                    collector,
                    px,
                    py,
                    containerPath,
                    fillPath,
                    packedLight
            );
        }

        poseStack.popPose();
    }

    @Unique
    private void renderSpriteIconPair(
            PoseStack poseStack,
            SubmitNodeCollector collector,
            float x,
            float y,
            String containerPath,
            String fillPath,
            int packedLight
    ) {
        Minecraft mc = Minecraft.getInstance();
        AtlasManager atlasManager = mc.getAtlasManager();
        var guiAtlas = atlasManager.getAtlasOrThrow(Identifier.withDefaultNamespace("gui"));

        TextureAtlasSprite container = containerPath != null
                ? guiAtlas.getSprite(Identifier.parse(containerPath))
                : null;

        TextureAtlasSprite fill = fillPath != null
                ? guiAtlas.getSprite(Identifier.parse(fillPath))
                : null;

        if (container == null && fill == null) {
            return;
        }

        final float SIZE = 8.0F;
        Matrix4f model = new Matrix4f(poseStack.last().pose());
        RenderType renderType = RenderTypes.entityCutout(container != null ? container.atlasLocation() : fill.atlasLocation());

        collector.submitCustomGeometry(
                poseStack,
                renderType,
                (unusedPose, vertexConsumer) -> {
                    if (container != null) {
                        addSpriteQuad(
                                vertexConsumer,
                                model,
                                x,
                                y,
                                SIZE,
                                container.getU0(),
                                container.getV0(),
                                container.getU1(),
                                container.getV1(),
                                packedLight
                        );
                    }

                    if (fill != null) {
                        addSpriteQuad(
                                vertexConsumer,
                                model,
                                x,
                                y,
                                SIZE,
                                fill.getU0(),
                                fill.getV0(),
                                fill.getU1(),
                                fill.getV1(),
                                packedLight
                        );
                    }
                }
        );
    }

    @Unique
    private static void addSpriteQuad(
            VertexConsumer vertexConsumer,
            Matrix4f model,
            float x,
            float y,
            float size,
            float u0,
            float v0,
            float u1,
            float v1,
            int packedLight
    ) {
        float z = 0.0F;

        vertexConsumer.addVertex(model, x, y, z)
                .setColor(255, 255, 255, 255)
                .setUv(u0, v0)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(packedLight)
                .setNormal(0.0F, 0.0F, 1.0F);

        vertexConsumer.addVertex(model, x + size, y, z)
                .setColor(255, 255, 255, 255)
                .setUv(u1, v0)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(packedLight)
                .setNormal(0.0F, 0.0F, 1.0F);

        vertexConsumer.addVertex(model, x + size, y + size, z)
                .setColor(255, 255, 255, 255)
                .setUv(u1, v1)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(packedLight)
                .setNormal(0.0F, 0.0F, 1.0F);

        vertexConsumer.addVertex(model, x, y + size, z)
                .setColor(255, 255, 255, 255)
                .setUv(u0, v1)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(packedLight)
                .setNormal(0.0F, 0.0F, 1.0F);
    }
}