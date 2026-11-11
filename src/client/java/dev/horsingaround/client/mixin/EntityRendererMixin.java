package dev.horsingaround.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.horsingaround.client.render.RidePoser;

import net.minecraft.client.renderer.entity.EntityRenderer;


import net.minecraft.world.entity.Entity;





import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Leads tie to a managed horse and to its rider where they are drawn, not where vanilla's fixed points on the entity are
 * (see {@link RidePoser#leashOffset} and {@link RidePoser#ropeHold}). Vanilla uses those points only to draw leads. Before
 * 1.21 leads are drawn by the mob renderer.
 */

@Mixin(EntityRenderer.class)


public abstract class EntityRendererMixin {











	@WrapOperation(method = "renderLeash", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getLeashOffset(F)Lnet/minecraft/world/phys/Vec3;"))
	private Vec3 horsingaround$leashOffset(final Entity leashed, final float partialTicks, final Operation<Vec3> original) {
		return RidePoser.leashOffset(leashed, original.call(leashed, partialTicks), partialTicks);
	}







	@WrapOperation(



		method = "renderLeash",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getRopeHoldPosition(F)Lnet/minecraft/world/phys/Vec3;")
	)
	private Vec3 horsingaround$ropeHold(final Entity holder, final float partialTicks, final Operation<Vec3> original) {
		return RidePoser.ropeHold(holder, original.call(holder, partialTicks), partialTicks);
	}
}
