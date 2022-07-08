package co.arago.util.validation;

import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

/**
 * This interface provides value checking to the classes that implement it.
 */
public interface ValueChecks {
    /**
     * Check for null values.
     *
     * @param value The value to check for null
     * @param name  Name of the value for the exception message
     * @param <N>   Type of any value.
     * @return The value itself
     * @throws NullPointerException when value == null
     */
    static <N> N notNull(N value, String name) {
        return Objects.requireNonNull(value, "Value '" + name + "' is required and cannot be null.");
    }

    /**
     * Check for null value arrays.
     *
     * @param name  Name of the value for the exception message
     * @param value The value array to check for null
     * @param <N>   Type of any value.
     * @return The value itself
     * @throws NullPointerException when value == null
     */
    static <N> N[] notNull(String name, N[] value) {
        if (value == null)
            throw new IllegalArgumentException("Value '" + name + "' is required and cannot be null.");
        return value;
    }

    /**
     * Check for null or empty value array.
     *
     * @param name  Name of the value for the exception message
     * @param value The value array to check for null
     * @param <N>   Type of any value.
     * @return The value itself
     * @throws NullPointerException when value == null
     */
    static <N> N[] notEmpty(String name, N[] value) {
        if (value == null || value.length == 0)
            throw new IllegalArgumentException("Value '" + name + "' is required and cannot be null or empty.");
        return value;
    }

    /**
     * Check for empty string values.
     *
     * @param value The value to check for
     * @param name  Name of the value for the exception message
     * @return The value itself
     * @throws IllegalArgumentException when value is empty
     */
    static String notEmpty(String value, String name) {
        if (StringUtils.isEmpty(value))
            throw new IllegalArgumentException("Value '" + name + "' cannot be empty.");
        return value;
    }

    /**
     * Check for empty collection values.
     *
     * @param value The value to check for
     * @param name  Name of the value for the exception message
     * @return The value itself
     * @throws IllegalArgumentException when value is empty
     */
    static Collection<?> notEmpty(Collection<?> value, String name) {
        if (value == null || value.isEmpty())
            throw new IllegalArgumentException("Collection '" + name + "' cannot be empty.");
        return value;
    }

    /**
     * Check for empty map values.
     *
     * @param value The value to check for
     * @param name  Name of the value for the exception message
     * @return The value itself
     * @throws IllegalArgumentException when value is empty
     */
    static Map<?, ?> notEmpty(Map<?, ?> value, String name) {
        if (value == null || value.isEmpty())
            throw new IllegalArgumentException("Map '" + name + "' cannot be empty.");
        return value;
    }

    /**
     * Check for blank string values.
     *
     * @param value The value to check for
     * @param name  Name of the value for the exception message
     * @return The value itself
     * @throws IllegalArgumentException when value is blank
     */
    static String notBlank(String value, String name) {
        if (StringUtils.isBlank(value))
            throw new IllegalArgumentException("Value '" + name + "' cannot be blank.");
        return value;
    }

    /**
     * Create an exception for any custom error
     *
     * @param message The message to display.
     * @throws IllegalArgumentException with the message.
     */
    static void anyError(String message) {
        throw new IllegalArgumentException(message);
    }
}
