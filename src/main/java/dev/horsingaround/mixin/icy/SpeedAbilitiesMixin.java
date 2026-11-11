package dev.horsingaround.mixin.icy;

import dev.horsingaround.ride.IcyCompat;
import dev.horsingaround.ride.RideStateHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Three of Icy's breed abilities change a ridden horse's speed as it goes: a burst from a standstill (Quarter Horse), a
 * surge late in a long gallop (Thoroughbred) and a bonus that builds over a ride (Arabian). This mod's riding sets its
 * gaits from the horse's speed, so on the horses it rides they reshaped how it speeds up; there they stay off, and the
 * horse speeds up as any horse of its speed does.
 */
@Pseudo
@Mixin(targets = {
	"icy.betterhorses.net.feature.breed.StandstillBurst",
	"icy.betterhorses.net.feature.breed.TopEnd",
	"icy.betterhorses.net.feature.breed.Endurance"
}, remap = false)
public abstract class SpeedAbilitiesMixin {
	private static final ResourceLocation[] BONUSES = {
		ResourceLocation.fromNamespaceAndPath(IcyCompat.MOD_ID, "ability/standstill_burst"),
		ResourceLocation.fromNamespaceAndPath(IcyCompat.MOD_ID, "ability/top_end"),
		ResourceLocation.fromNamespaceAndPath(IcyCompat.MOD_ID, "ability/endurance")
	};

	@Inject(method = "tick", at = @At("HEAD"), cancellable = true)
	private void horsingaround$offUnderThisRide(final AbstractHorse horse, @Coerce final Object data, @Coerce final Object state, final CallbackInfo ci) {
		// (Unridden, they don't change the horse's speed: nothing to do.)
		if (horse.getControllingPassenger() == null || RideStateHolder.managedRide(horse) == null) {
			return;
		}

		final AttributeInstance speed = horse.getAttribute(Attributes.MOVEMENT_SPEED);
		if (speed != null) {
			for (final ResourceLocation bonus : BONUSES) {
				speed.removeModifier(bonus);
			}
		}

		ci.cancel();
	}
}
