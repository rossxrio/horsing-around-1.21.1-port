package dev.horsingaround.ride;

import net.minecraft.world.entity.LivingEntity;

import net.minecraft.world.entity.ai.attributes.Attributes;












/**
 * Numbers vanilla keeps as attributes from 1.20.5 on (gravity, how far a fall is safe, how much of it hurts) and as
 * fixed values before; one place, so the ride's physics reads the same on every version.
 */
final class Vanilla {
	private Vanilla() {
	}

	/** Downward pull per tick (the extra gravity of the ride is the ride's own). */
	static double gravity(final LivingEntity entity) {

		return entity.getGravity();


	}

	/** The fall (blocks) that does no damage. */
	static double safeFall(final LivingEntity entity) {

		return entity.getAttributeValue(Attributes.SAFE_FALL_DISTANCE);







	}

	/** What is left of a fall past the safe distance, as damage per block. */
	static double fallDamageMultiplier(final LivingEntity entity) {

		return entity.getAttributeValue(Attributes.FALL_DAMAGE_MULTIPLIER);


	}

	/**
	 * The longest fall (blocks) that costs the entity at most {@code hurt} health points, less a margin for error. From
	 * 1.21.5 vanilla rounds fall damage down; before, it rounded it up, so any fall past the safe distance hurts.
	 */
	static double longestFall(final LivingEntity entity, final int hurt) {
		final double multiplier = Math.max(fallDamageMultiplier(entity), 1.0E-3);



		return safeFall(entity) + hurt / multiplier - 0.05;
	}

	/**
	 * Health points a fall of {@code fall} blocks costs the horse's rider. From 1.21.5 the rider takes the fall themselves
	 * (from their own safe distance, rounded down); before, the horse handed its own damage on to its riders.
	 */
	static double riderFallHurt(final LivingEntity horse, final LivingEntity rider, final double fall) {



		return Math.ceil(Math.max(fall - safeFall(horse), 0.0) * fallDamageMultiplier(horse));
	}

	/** The longest fall (blocks) that costs the horse's rider at most {@code hurt} health points (see {@link #riderFallHurt}). */
	static double longestRiderFall(final LivingEntity horse, final LivingEntity rider, final int hurt) {



		return longestFall(horse, hurt);
	}












}
