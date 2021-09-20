package co.arago.util.validation;

import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

/**
 * This interface provides value checking to the classes that implement it.
 */
public interface RequiredFieldChecks {
    /**
     * Check for null values. Used with required parameters of builder fields.
     *
     * @param item The item to check for null
     * @param name Name of the item for the exception message
     * @param <N>  Type of any item.
     * @return The item itself
     * @throws NullPointerException when item == null
     */
    default <N> N notNull(N item, String name) {
        return Objects.requireNonNull(item, "Field '" + name + "' is required and cannot be null.");
    }

    /**
     * Check for null values. Used with required parameters of builder fields.
     *
     * @param item The item to check for null
     * @param name Name of the item for the exception message
     * @param <N>  Type of any item.
     * @return The item itself
     * @throws NullPointerException when item == null
     */
    default <N> N[] notNull(String name, N[] item) {
        if (item == null)
            throw new IllegalArgumentException("Field '" + name + "' is required and cannot be null.");
        return item;
    }

    /**
     * Check for null values. Used with required parameters of builder fields.
     *
     * @param item The item to check for null
     * @param name Name of the item for the exception message
     * @param <N>  Type of any item.
     * @return The item itself
     * @throws NullPointerException when item == null
     */
    default <N> N[] notEmpty(String name, N[] item) {
        if (item == null || item.length == 0)
            throw new IllegalArgumentException("Field '" + name + "' is required and cannot be null or empty.");
        return item;
    }

    /**
     * Check for empty string values. Used with required parameters of builder fields.
     *
     * @param item The item to check for
     * @param name Name of the item for the exception message
     * @return The item itself
     * @throws IllegalArgumentException when item is empty
     */
    default String notEmpty(String item, String name) {
        if (StringUtils.isEmpty(item))
            throw new IllegalArgumentException("Field '" + name + "' cannot be empty.");
        return item;
    }

    /**
     * Check for empty collection values. Used with required parameters of builder fields.
     *
     * @param item The item to check for
     * @param name Name of the item for the exception message
     * @return The item itself
     * @throws IllegalArgumentException when item is empty
     */
    default Collection<?> notEmpty(Collection<?> item, String name) {
        if (item == null || item.isEmpty())
            throw new IllegalArgumentException("Collection '" + name + "' cannot be empty.");
        return item;
    }

    /**
     * Check for empty map values. Used with required parameters of builder fields.
     *
     * @param item The item to check for
     * @param name Name of the item for the exception message
     * @return The item itself
     * @throws IllegalArgumentException when item is empty
     */
    default Map<?, ?> notEmpty(Map<?, ?> item, String name) {
        if (item == null || item.isEmpty())
            throw new IllegalArgumentException("Map '" + name + "' cannot be empty.");
        return item;
    }

    /**
     * Check for blank string values. Used with required parameters of builder fields.
     *
     * @param item The item to check for
     * @param name Name of the item for the exception message
     * @return The item itself
     * @throws IllegalArgumentException when item is blank
     */
    default String notBlank(String item, String name) {
        if (StringUtils.isBlank(item))
            throw new IllegalArgumentException("Field '" + name + "' cannot be blank.");
        return item;
    }

    /**
     * Create an exception for any custom error
     *
     * @param message The message to display.
     * @throws IllegalArgumentException with the message.
     */
    default void anyError(String message) {
        throw new IllegalArgumentException(message);
    }
}
