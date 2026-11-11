package dev.horsingaround.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.horsingaround.client.compat.ShaderShadows;
import dev.horsingaround.client.render.RidePoseState;
import dev.horsingaround.client.render.RidePoser;
import dev.horsingaround.client.render.LegacyRenderState;
import dev.horsingaround.ride.RideState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.phys.Vec3;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import dev.horsingaround.client.render.state.LivingEntityRenderState;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Poses horses and their riders as they are drawn (worked out in {@link RidePoser}). */
@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin {
	// Before 1.21.2 there are no render states: the renderer draws the entity itself, so a stand-in state is filled from
	// it as drawing starts (only for a ridden horse and its rider, the rest are left alone), and the model and layer
	// hooks find it in LegacyRenderState while this entity is drawn.
	@Unique
	private LivingEntityRenderState horsingaround$state;

	@Inject(method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At("HEAD"))
	private void horsingaround$extractPose(
		final LivingEntity entity, final float yaw, final float partialTicks, final PoseStack poseStack, final MultiBufferSource buffers, final int lightCoords,
		final CallbackInfo ci
	) {
		if (RidePoser.holder(entity) == null) {
			LegacyRenderState.clear();
			return;
		}
		LivingEntityRenderState state = this.horsingaround$state;
		if (state == null) {
			state = this.horsingaround$state = LegacyRenderState.create(((LivingEntityRenderer<?, ?>) (Object) this).getModel());
		}
		state.fill(entity, partialTicks);
		RidePoser.extract(entity, state, partialTicks);
		LegacyRenderState.set(entity, state);
	}

	@Inject(method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At("RETURN"))
	private void horsingaround$doneDrawing(
		final LivingEntity entity, final float yaw, final float partialTicks, final PoseStack poseStack, final MultiBufferSource buffers, final int lightCoords,
		final CallbackInfo ci
	) {
		// (The first-person hands pose the player model outside of any drawing: nothing of a ride may be left over for them.)
		LegacyRenderState.clear();
	}

	// A rider's body and head turn come from the state rather than the renderer's own working: where the rider looks within
	// the horse's turn, with the hips square to the horse.
	@ModifyArg(
		method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/LivingEntityRenderer;setupRotations(Lnet/minecraft/world/entity/LivingEntity;Lcom/mojang/blaze3d/vertex/PoseStack;FFFF)V"),
		index = 3
	)
	private float horsingaround$bodyRot(final float bodyRot) {
		final LivingEntityRenderState rider = LegacyRenderState.riderState();
		return rider != null ? rider.bodyRot : bodyRot;
	}

	@ModifyArg(
		method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/EntityModel;setupAnim(Lnet/minecraft/world/entity/Entity;FFFFF)V"),
		index = 4
	)
	private float horsingaround$headYaw(final float headYaw) {
		final LivingEntityRenderState rider = LegacyRenderState.riderState();
		return rider != null ? rider.yRot : headYaw;
	}

	@ModifyArg(
		method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/layers/RenderLayer;render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/Entity;FFFFFF)V"),
		index = 8
	)
	private float horsingaround$layersHeadYaw(final float headYaw) {
		final LivingEntityRenderState rider = LegacyRenderState.riderState();
		return rider != null ? rider.yRot : headYaw;
	}

	@Inject(
		method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
		at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;pushPose()V", shift = At.Shift.AFTER, ordinal = 0)
	)
	private void horsingaround$applyPose(
		final LivingEntity entity, final float yaw, final float partialTicks, final PoseStack poseStack,
		final MultiBufferSource buffers, final int lightCoords,
		final CallbackInfo ci
	) {
		final LivingEntityRenderState state = LegacyRenderState.state();
		if (state != null) {
			((RidePoseState) state).horsingaround$applyPose(poseStack);
		}
	}

	/**
	 * Where a ridden horse's model is drawn this frame, so its legs can find the ground under each hoof (see GroundLegs):
	 * taken once the pose is set up and before the model is animated (before 1.21.9 the model is animated before vanilla
	 * asks whether the body shows, so the legs would read the frame before's, or a horse inventory's preview).
	 */
	@Inject(
		method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/model/EntityModel;setupAnim(Lnet/minecraft/world/entity/Entity;FFFFF)V"
		)
	)
	private void horsingaround$drawnPose(
		final LivingEntity entity, final float yaw, final float partialTicks, final PoseStack poseStack,
		final MultiBufferSource buffers, final int lightCoords,
		final CallbackInfo ci
	) {
		final LivingEntityRenderState state = LegacyRenderState.state();
		final RideState ride = state == null ? null : ((RidePoseState) state).horsingaround$rideState();
		// (Not from a shader pack's shadow pass: its pose stack has the sun's view in it.)
		if (ride != null && !ShaderShadows.drawing()) {
			ride.drawnPose.set(poseStack.last().pose());
			final Vec3 camera = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
			ride.drawnCameraX = camera.x;
			ride.drawnCameraY = camera.y;
			ride.drawnCameraZ = camera.z;
			ride.drawnPoseSet = true;
		}
	}
}
