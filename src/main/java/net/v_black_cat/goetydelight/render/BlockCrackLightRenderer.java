package net.v_black_cat.goetydelight.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.v_black_cat.goetydelight.render.test.ModShaderReg;
import net.v_black_cat.goetydelight.visual.ActiveEntityVisualEffect;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public final class BlockCrackLightRenderer {
    private static final double DEFAULT_RADIUS = 6.0D;
    private static final double MAX_RADIUS = 8.0D;
    private static final double FACE_EPSILON = 0.004D;
    private static final int MAX_CRACKS = 190;
    private static final int MAX_SPARKS = 90;

    private BlockCrackLightRenderer() {
    }

    public static void render(RenderLevelStageEvent event, Entity entity, ActiveEntityVisualEffect effect) {
        ClientLevel level = (ClientLevel) entity.level();
        float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(true);
        float renderTime = entity.level().getGameTime() + partialTick;
        Vec3 center = entity.getPosition(partialTick).add(0.0D, entity.getBbHeight() * 0.36D, 0.0D);
        double radius = radius(entity, effect);

        ShaderInstance shader = ModShaderReg.getBlockCrackLightShader();
        shader.safeGetUniform("iTime").set(renderTime / 20.0F);
        shader.safeGetUniform("intensity").set(intensity(effect));

        Matrix4f oldProjection = new Matrix4f(RenderSystem.getProjectionMatrix());
        VertexSorting oldSorting = RenderSystem.getVertexSorting();
        RenderSystem.setProjectionMatrix(event.getProjectionMatrix(), VertexSorting.DISTANCE_TO_ORIGIN);

        RenderSystem.setShader(() -> shader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(770, 1, 1, 1);
        RenderSystem.disableCull();

        shader.safeGetUniform("EffectMode").set(0);
        drawCracks(event, level, center, radius, renderTime);

        shader.safeGetUniform("EffectMode").set(1);
        drawSparks(event, level, center, radius, renderTime);

        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
        RenderSystem.setProjectionMatrix(oldProjection, oldSorting);
    }

    private static void drawCracks(RenderLevelStageEvent event, ClientLevel level, Vec3 center, double radius, float renderTime) {
        Camera camera = event.getCamera();
        Vec3 cameraPos = camera.getPosition();
        Matrix4f matrix = event.getPoseStack().last().pose();
        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);

        int cracks = 0;
        int blockRadius = Mth.ceil(radius);
        BlockPos origin = BlockPos.containing(center);
        BlockPos min = origin.offset(-blockRadius, -Mth.ceil(radius * 0.72D), -blockRadius);
        BlockPos max = origin.offset(blockRadius, Mth.ceil(radius * 0.55D), blockRadius);

        for (BlockPos mutablePos : BlockPos.betweenClosed(min, max)) {
            if (cracks >= MAX_CRACKS) {
                break;
            }

            BlockPos pos = mutablePos.immutable();
            if (!level.isLoaded(pos)) {
                continue;
            }

            BlockState state = level.getBlockState(pos);
            if (state.isAir()) {
                continue;
            }

            for (Direction face : Direction.values()) {
                if (cracks >= MAX_CRACKS) {
                    break;
                }

                if (!isVisibleFace(level, pos, state, face)) {
                    continue;
                }

                Vec3 faceCenter = faceCenter(pos, face);
                double distance = faceCenter.distanceTo(center);
                if (distance > radius) {
                    continue;
                }

                int seed = hash(pos.getX(), pos.getY(), pos.getZ(), face.ordinal());
                float fade = distanceFade(distance, radius);
                float chance = 0.22F + fade * 0.72F;
                if (random01(seed) > chance) {
                    continue;
                }

                addCrack(buffer, matrix, cameraPos, pos, face, faceCenter, fade, seed, renderTime);
                cracks++;

                if (fade > 0.42F && random01(seed ^ 0x4a5f31) < 0.34F && cracks < MAX_CRACKS) {
                    addCrack(buffer, matrix, cameraPos, pos, face, faceCenter, fade * 0.72F, seed ^ 0x4a5f31, renderTime + 3.7F);
                    cracks++;
                }
            }
        }

        BufferUploader.drawWithShader(buffer.buildOrThrow());
    }

    private static void drawSparks(RenderLevelStageEvent event, ClientLevel level, Vec3 center, double radius, float renderTime) {
        Camera camera = event.getCamera();
        Vec3 cameraPos = camera.getPosition();
        Vector3f leftVector = camera.getLeftVector();
        Vector3f upVector = camera.getUpVector();
        Vec3 cameraLeft = new Vec3(leftVector.x(), leftVector.y(), leftVector.z());
        Vec3 cameraUp = new Vec3(upVector.x(), upVector.y(), upVector.z());
        Matrix4f matrix = event.getPoseStack().last().pose();
        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);

        int sparks = 0;
        int blockRadius = Mth.ceil(radius);
        BlockPos origin = BlockPos.containing(center);
        BlockPos min = origin.offset(-blockRadius, -Mth.ceil(radius * 0.62D), -blockRadius);
        BlockPos max = origin.offset(blockRadius, Mth.ceil(radius * 0.62D), blockRadius);

        for (BlockPos mutablePos : BlockPos.betweenClosed(min, max)) {
            if (sparks >= MAX_SPARKS) {
                break;
            }

            BlockPos pos = mutablePos.immutable();
            if (!level.isLoaded(pos)) {
                continue;
            }

            BlockState state = level.getBlockState(pos);
            if (state.isAir()) {
                continue;
            }

            for (Direction face : Direction.values()) {
                if (sparks >= MAX_SPARKS) {
                    break;
                }

                if (!isVisibleFace(level, pos, state, face)) {
                    continue;
                }

                Vec3 faceCenter = faceCenter(pos, face);
                double distance = faceCenter.distanceTo(center);
                if (distance > radius) {
                    continue;
                }

                int seed = hash(pos.getX(), pos.getY(), pos.getZ(), face.ordinal() ^ 0x65);
                float fade = distanceFade(distance, radius);
                if (random01(seed) > fade * 0.38F) {
                    continue;
                }

                addSpark(buffer, matrix, cameraLeft, cameraUp, cameraPos, faceCenter, face, fade, seed, renderTime);
                sparks++;
            }
        }

        BufferUploader.drawWithShader(buffer.buildOrThrow());
    }

    private static void addCrack(BufferBuilder buffer, Matrix4f matrix, Vec3 cameraPos, BlockPos pos, Direction face, Vec3 faceCenter, float fade, int seed, float renderTime) {
        FaceBasis basis = basis(face);
        boolean verticalFace = face.getAxis().isHorizontal();
        boolean longVertical = verticalFace && random01(seed ^ 0x7129) < 0.72F;
        Vec3 longAxis = longVertical ? basis.v : (random01(seed ^ 0x15d1) < 0.5F ? basis.u : basis.v);
        Vec3 sideAxis = longAxis == basis.u ? basis.v : basis.u;

        double longOffset = (random01(seed ^ 0x3f4d) - 0.5D) * 0.52D;
        double sideRandom = random01(seed ^ 0x2157);
        double sideOffset = edgeBiasedOffset(sideRandom);
        double maxHalfLength = Math.max(0.10D, 0.49D - Math.abs(longOffset));
        double halfLength = Math.min(maxHalfLength, (0.12D + 0.44D * fade) * (0.72D + random01(seed ^ 0x4431) * 0.55D));
        double halfWidth = (0.008D + 0.040D * fade) * (0.72D + random01(seed ^ 0x19bf) * 0.50D);
        int alpha = (int) (Mth.clamp(fade * fade * 225.0F, 16.0F, 230.0F));
        int color = color(renderTime, seed, alpha);
        Vec3 normal = normal(face);
        Vec3 start = faceCenter.add(normal.scale(FACE_EPSILON))
                .add(longAxis.scale(longOffset - halfLength))
                .add(sideAxis.scale(sideOffset));
        Vec3 end = faceCenter.add(normal.scale(FACE_EPSILON))
                .add(longAxis.scale(longOffset + halfLength))
                .add(sideAxis.scale(sideOffset));
        Vec3 side = sideAxis.scale(halfWidth);

        putVertex(buffer, matrix, start.subtract(side).subtract(cameraPos), color, 0.0F, 0.0F);
        putVertex(buffer, matrix, start.add(side).subtract(cameraPos), color, 0.0F, 1.0F);
        putVertex(buffer, matrix, end.add(side).subtract(cameraPos), color, 1.0F, 1.0F);
        putVertex(buffer, matrix, end.subtract(side).subtract(cameraPos), color, 1.0F, 0.0F);
    }

    private static void addSpark(BufferBuilder buffer, Matrix4f matrix, Vec3 cameraLeft, Vec3 cameraUp, Vec3 cameraPos, Vec3 faceCenter, Direction face, float fade, int seed, float renderTime) {
        Vec3 normal = normal(face);
        double drift = 0.035D + 0.16D * fade * random01(seed ^ 0x6c19);
        Vec3 center = faceCenter.add(normal.scale(FACE_EPSILON + drift));
        center = center.add(basis(face).u.scale((random01(seed ^ 0x3a13) - 0.5D) * 0.72D));
        center = center.add(basis(face).v.scale((random01(seed ^ 0x711b) - 0.5D) * 0.72D));

        float size = (float) ((0.022D + 0.105D * fade) * (0.70D + random01(seed ^ 0x9d2d) * 0.55D));
        int alpha = (int) (fade * fade * 155.0F);
        int color = color(renderTime + 4.0F, seed ^ 0x35d1, alpha);
        Vec3 relative = center.subtract(cameraPos);
        Vec3 dx = cameraLeft.scale(size);
        Vec3 dy = cameraUp.scale(size);

        putVertex(buffer, matrix, relative.subtract(dx).subtract(dy), color, 0.0F, 1.0F);
        putVertex(buffer, matrix, relative.add(dx).subtract(dy), color, 1.0F, 1.0F);
        putVertex(buffer, matrix, relative.add(dx).add(dy), color, 1.0F, 0.0F);
        putVertex(buffer, matrix, relative.subtract(dx).add(dy), color, 0.0F, 0.0F);
    }

    private static boolean isVisibleFace(ClientLevel level, BlockPos pos, BlockState state, Direction face) {
        if (!state.isFaceSturdy(level, pos, face)) {
            return false;
        }

        BlockPos neighborPos = pos.relative(face);
        BlockState neighbor = level.getBlockState(neighborPos);
        return neighbor.isAir() || !neighbor.isFaceSturdy(level, neighborPos, face.getOpposite());
    }

    private static double radius(Entity entity, ActiveEntityVisualEffect effect) {
        double radius = effect.data().contains("Radius") ? effect.data().getDouble("Radius") : DEFAULT_RADIUS + Math.max(0.0D, entity.getBbWidth() - 0.6D);
        return Mth.clamp(radius, 2.0D, MAX_RADIUS);
    }

    private static float intensity(ActiveEntityVisualEffect effect) {
        return effect.data().contains("Intensity") ? effect.data().getFloat("Intensity") : 1.12F;
    }

    private static float distanceFade(double distance, double radius) {
        float fade = 1.0F - (float) (distance / radius);
        fade = Mth.clamp(fade, 0.0F, 1.0F);
        return fade * fade * (3.0F - 2.0F * fade);
    }

    private static Vec3 faceCenter(BlockPos pos, Direction face) {
        double x = pos.getX() + 0.5D + face.getStepX() * 0.5D;
        double y = pos.getY() + 0.5D + face.getStepY() * 0.5D;
        double z = pos.getZ() + 0.5D + face.getStepZ() * 0.5D;
        return new Vec3(x, y, z);
    }

    private static FaceBasis basis(Direction face) {
        return switch (face) {
            case UP, DOWN -> new FaceBasis(new Vec3(1.0D, 0.0D, 0.0D), new Vec3(0.0D, 0.0D, 1.0D));
            case NORTH, SOUTH -> new FaceBasis(new Vec3(1.0D, 0.0D, 0.0D), new Vec3(0.0D, 1.0D, 0.0D));
            case EAST, WEST -> new FaceBasis(new Vec3(0.0D, 0.0D, 1.0D), new Vec3(0.0D, 1.0D, 0.0D));
        };
    }

    private static Vec3 normal(Direction face) {
        return new Vec3(face.getStepX(), face.getStepY(), face.getStepZ());
    }

    private static double edgeBiasedOffset(double value) {
        if (value < 0.38D) {
            return -0.47D + value / 0.38D * 0.16D;
        }

        if (value > 0.62D) {
            return 0.31D + (value - 0.62D) / 0.38D * 0.16D;
        }

        return (value - 0.5D) * 0.58D;
    }

    private static int color(float renderTime, int seed, int alpha) {
        float phase = renderTime * 0.115F + (seed & 1023) * 0.019F;
        int red = channel(phase);
        int green = channel(phase + 2.0943952F);
        int blue = channel(phase + 4.1887903F);
        return (Mth.clamp(alpha, 0, 255) << 24) | (red << 16) | (green << 8) | blue;
    }

    private static int channel(float phase) {
        return 88 + (int) ((0.5F + 0.5F * Mth.sin(phase)) * 167.0F);
    }

    private static int hash(int x, int y, int z, int salt) {
        int h = x * 73428767 ^ y * 91227153 ^ z * 42317861 ^ salt * 1999673;
        h ^= h >>> 16;
        h *= 0x7feb352d;
        h ^= h >>> 15;
        h *= 0x846ca68b;
        h ^= h >>> 16;
        return h;
    }

    private static double random01(int seed) {
        return (seed & 0x00FFFFFF) / 16777215.0D;
    }

    private static void putVertex(BufferBuilder buffer, Matrix4f matrix, Vec3 pos, int color, float u, float v) {
        buffer.addVertex(matrix, (float) pos.x, (float) pos.y, (float) pos.z)
                .setColor((color >> 16) & 255, (color >> 8) & 255, color & 255, (color >> 24) & 255)
                .setUv(u, v)
        ;
    }

    private record FaceBasis(Vec3 u, Vec3 v) {
    }
}

