@file:kotlin.jvm.JvmMultifileClass
@file:kotlin.jvm.JvmName("RangesKt")
package kotlin

/**
 * Represents a range of [Comparable] values.
 */
@Deprecated("This range implementation has unclear semantics and will be removed soon.", level = DeprecationLevel.ERROR)
public class ComparableRange<T: Comparable<T>> (
        override val start: T,
        override val end: T
): Range<T> {
    override fun contains(item: T): Boolean {
        return start <= item && item <= end
    }

    @Suppress("DEPRECATION_ERROR")
    override fun equals(other: Any?): Boolean {
        return other is ComparableRange<*> && (isEmpty() && other.isEmpty() ||
                start == other.start && end == other.end)
    }

    override fun hashCode(): Int {
        return if (isEmpty()) -1 else 31 * start.hashCode() + end.hashCode()
    }

    override fun toString(): String = "$start..$end"
}

/**
 * Creates a range from this [Comparable] value to the specified [that] value. This value
 * needs to be smaller than [that] value, otherwise the returned range will be empty.
 */
@Deprecated("This range implementation has unclear semantics and will be removed soon.", level = DeprecationLevel.ERROR)
@Suppress("DEPRECATION_ERROR")
public operator fun <T: Comparable<T>> T.rangeTo(that: T): ComparableRange<T> {
    return ComparableRange(this, that)
}


internal fun checkStepIsPositive(isPositive: Boolean, step: Number) {
    if (!isPositive) throw IllegalArgumentException("Step must be positive, was: $step")
}
