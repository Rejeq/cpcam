package com.rejeq.cpcam.core.data.model

/**
 * Represents a resolution with a non-negative width and height.
 *
 * This class is similar to `android.util.Size` but enforces non-negative
 * dimensions and provides methods for simplifying resolutions
 * (reducing them to their lowest terms).
 * It also supports comparison based on the total area of the resolution.
 *
 * @property width The width of the resolution, must be non-negative.
 * @property height The height of the resolution, must be non-negative.
 * @throws IllegalArgumentException if either the width or height is negative.
 */
class Resolution(val width: Int, val height: Int) : Comparable<Resolution> {
    init {
        require(width >= 0) { "Width cannot be negative" }
        require(height >= 0) { "Height cannot be negative" }
    }

    /**
     * The aspect ratio of the object, calculated as the width divided by the
     * height.
     *
     * This property provides the ratio between the width and height.
     * For example, a value of 1.0 indicates a square, a value greater than 1.0
     * indicates a landscape orientation, and a value less than 1.0 indicates a
     * portrait orientation.
     *
     * @return The aspect ratio as a Float.
     * @throws ArithmeticException if the height is zero, resulting in division
     *         by zero.
     */
    val aspectRatio get() = width.toFloat() / height.toFloat()

    /**
     * Returns a simplified resolution, representing the aspect ratio in its
     * simple form.
     * For example, a resolution of 1920x1080 would be simplified to 16x9.
     *
     * @return A new [Resolution] object representing the simplified resolution.
     *
     * @sample
     * ```
     * val resolution = Resolution(1920, 1080)
     * val simple = resolution.simplified() // Returns Resolution(16, 9)
     *
     * val resolution2 = Resolution(800, 600)
     * val simple2 = resolution2.simplified() // Returns Resolution(4,3)
     * ```
     */
    fun simplified(): Resolution {
        val gcd = gcd(width, height)

        return Resolution(width / gcd, height / gcd)
    }

    override fun compareTo(other: Resolution): Int {
        val area = width * height
        val otherArea = other.width * other.height
        return area.compareTo(otherArea)
    }

    override fun toString(): String = "${width}x$height"

    companion object {
        /**
         * Parses a string representation of a resolution into a `Resolution`
         * object.
         *
         * The expected format of the input string is "${width}x${height}",
         * where width and height are integers.
         * For example: "1920x1080", "1280x720", etc.
         *
         * @param input The string to parse.
         * @return A `Resolution` object representing the parsed resolution,
         *         or `null` if the input string is not in the correct format
         *         or if the width or height cannot be parsed as integers.
         */
        fun fromString(input: String): Resolution? {
            val (w, h) = input.split('x').takeIf { it.size == 2 }?.let {
                val width = it[0].toIntOrNull()
                val height = it[1].toIntOrNull()

                if (width != null && height != null) {
                    width to height
                } else {
                    null
                }
            } ?: return null

            return Resolution(w, h)
        }
    }
}

private tailrec fun gcd(p: Int, q: Int): Int = if (q == 0) p else gcd(q, p % q)
