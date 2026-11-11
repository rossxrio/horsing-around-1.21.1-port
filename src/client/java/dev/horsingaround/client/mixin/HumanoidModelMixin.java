package dev.horsingaround.client.mixin;

import dev.horsingaround.client.render.RiderPose;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import dev.horsingaround.client.render.state.HumanoidRenderState;

import dev.horsingaround.client.render.LegacyRenderState;
import net.minecraft.world.entity.LivingEntity;






import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Mob riders (zombie and skeleton horsemen) sit their horses like players do; players are posed in the player model mixin. */

@Mixin(HumanoidModel.class)
public abstract class HumanoidModelMixin {





















	@Inject(method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V", at = @At("TAIL"))
	private void horsingaround$riderPose(
		final LivingEntity entity, final float limbSwing, final float limbSwingAmount, final float ageInTicks, final float headYaw, final float headPitch,
		final CallbackInfo ci
	) {
		if (!((Object) this instanceof PlayerModel) && LegacyRenderState.of(entity) instanceof HumanoidRenderState state) {
			final HumanoidModel<?> model = (HumanoidModel<?>) (Object) this;
			RiderPose.apply(model, state);
			// (The hat is a part of its own here, a copy of the head, rather than a child of it.)
			model.hat.copyFrom(model.head);
		}
	}

}
