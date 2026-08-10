package us.fihgu.toolbox.compat;

import java.lang.reflect.Field;

/**
 * Resolves Bukkit/Paper enum constants (and Enchantment, which is a class
 * with static instance fields rather than a real Java enum) by name at
 * runtime, trying a list of candidate names in order.
 * <p>
 * A handful of vanilla-item/enchantment names were renamed across the
 * Minecraft versions this project targets (e.g. {@code Material.GRASS} to
 * {@code Material.SHORT_GRASS}, {@code Enchantment.DAMAGE_ALL} to
 * {@code Enchantment.SHARPNESS}). Referencing either name directly as a
 * compile-time constant would make the single, unmodified AdditionsAPI
 * source tree fail to compile against whichever Paper API version does not
 * have that name. Resolving the name at runtime - the same technique this
 * project already needs for Particle renames - lets one source tree compile
 * and behave correctly against every targeted Paper API version.
 * <p>
 * See CLAUDE.md ("Known Bukkit/Paper API drift") for the full rename list.
 */
public final class EnumCompat {

	private EnumCompat() {
	}

	/**
	 * Returns the first enum constant of {@code enumType} whose name matches
	 * one of {@code candidateNames}, trying them in order. Returns
	 * {@code null} if none of the candidate names exist on this enum in the
	 * currently running Bukkit/Paper API.
	 */
	public static <E extends Enum<E>> E resolve(Class<E> enumType, String... candidateNames) {
		for (String name : candidateNames) {
			try {
				return Enum.valueOf(enumType, name);
			} catch (IllegalArgumentException ignored) {
				// Not this name on this API version - try the next candidate.
			}
		}
		return null;
	}

	/**
	 * Returns the first {@code public static final} field of {@code type}
	 * whose name matches one of {@code candidateFieldNames}, trying them in
	 * order. Used for {@code org.bukkit.enchantments.Enchantment}, which
	 * publishes its vanilla enchantments as static instance fields on an
	 * abstract class rather than as real Java enum constants, so
	 * {@link #resolve} does not apply to it.
	 */
	@SuppressWarnings("unchecked")
	public static <T> T resolveStaticField(Class<T> type, String... candidateFieldNames) {
		for (String name : candidateFieldNames) {
			try {
				Field field = type.getField(name);
				Object value = field.get(null);
				if (value != null) {
					return (T) value;
				}
			} catch (ReflectiveOperationException | ClassCastException ignored) {
				// Not this name on this API version - try the next candidate.
			}
		}
		return null;
	}
}
