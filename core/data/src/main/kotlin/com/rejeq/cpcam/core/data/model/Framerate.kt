package com.rejeq.cpcam.core.data.model

/**
 * Represents a camera framerate range.
 *
 * @property min The minimum framerate in frames per second
 * @property max The maximum framerate in frames per second
 */
data class Framerate(val min: Int, val max: Int) {
    init {
        require(min <= max) {
            "Minimum framerate ($min) must be less than or equal to maximum " +
                "framerate ($max)"
        }
        require(min >= 0) { "Minimum framerate ($min) must be non-negative" }
    }

    /**
     * Indicates whether this framerate represents a fixed rate
     * (min equals max).
     */
    val isFixed: Boolean get() = min == max

    /**
     * Returns the fixed framerate value if this is a fixed rate,
     * otherwise throws IllegalStateException.
     */
    val fixed: Int get() {
        check(isFixed) { "Framerate is not fixed (min=$min, max=$max)" }
        return min
    }

    override fun toString(): String = "$min-$max"

    companion object {
        /**
         * Creates a fixed framerate with the same min and max value.
         *
         * @param fps The fixed framerate value in frames per second
         */
        fun fixed(fps: Int) = Framerate(fps, fps)

        fun fromString(input: String): Framerate? {
            val (min, max) = input.split('-').takeIf { it.size == 2 }?.let {
                val min = it[0].toIntOrNull()
                val max = it[1].toIntOrNull()

                if (min != null && max != null) {
                    min to max
                } else {
                    null
                }
            } ?: return null

            return Framerate(min, max)
        }
    }
}
