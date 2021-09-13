package co.arago.util.text.templates;

import org.apache.commons.text.StringSubstitutor;
import org.apache.commons.text.lookup.StringLookup;
import org.apache.commons.text.lookup.StringLookupFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * Encapsulates {@link StringSubstitutor} and adds escaping of values via StringEscapeUtils
 */
public abstract class AbstractStringFromTemplateBuilder<T extends AbstractStringFromTemplateBuilder<T>> {

    /**
     * The class for StringLookup that uses StringEscapeUtils for escaping Strings
     */
    protected static class EscapingStringLookup implements StringLookup {

        protected static final Map<String, BiFunction<Map<String, Object>, String, String>> defaultMapping = new HashMap<>();

        private final Map<String, BiFunction<Map<String, Object>, String, String>> escapeMapping;

        /**
         * The field map contains the replacements for the template
         */
        Map<String, Object> fieldMap;

        /**
         * Constructor
         */
        protected EscapingStringLookup() {
            this.escapeMapping = new HashMap<>(defaultMapping);
        }

        /**
         * Looks up a String key to a String value.
         * <p>
         * The internal implementation may use any mechanism to return the value. The simplest implementation is to use a
         * Map. However, virtually any implementation is possible.
         * </p>
         * <p>
         * For example, it would be possible to implement a lookup that used the key as a primary key, and looked up the
         * value on demand from the database Or, a numeric based implementation could be created that treats the key as an
         * integer, increments the value and return the result as a string - converting 1 to 2, 15 to 16 etc.
         * </p>
         * <p>
         * This method always returns a String, regardless of the underlying data, by converting it as necessary. For
         * example:
         * </p>
         *
         * <pre>
         * Map&lt;String, Object&gt; map = new HashMap&lt;String, Object&gt;();
         * map.put("number", new Integer(2));
         * assertEquals("2", StringLookupFactory.mapStringLookup(map).lookup("number"));
         * </pre>
         *
         * @param key the key to look up, may be null
         * @return the matching value, null if no match
         */
        @Override
        public String lookup(String key) {

            String[] typeKey = key.split(":", 2);

            if (typeKey.length == 1) {
                return getDefaultFromMap(fieldMap, typeKey[0]);
            } else {
                if (escapeMapping.containsKey(typeKey[0])) {
                    return escapeMapping.get(typeKey[0]).apply(fieldMap, typeKey[1]);
                } else {
                    return getDefaultFromMap(fieldMap, typeKey[1]);
                }
            }
        }

        protected static String getDefaultFromMap(Map<String, Object> map, String key) {
            return StringLookupFactory.INSTANCE.mapStringLookup(map).lookup(key);
        }

        protected void putMapping(String key, BiFunction<Map<String, Object>, String, String> mappingFunc) {
            escapeMapping.put(key, mappingFunc);
        }
    }

    /**
     * The template string with the placeholders for the fields
     */
    protected String template = "";

    /**
     * The StringLookup instance that uses StringEscapeUtils for escaping Strings
     */
    protected final EscapingStringLookup lookup;

    protected AbstractStringFromTemplateBuilder() {
        this(new EscapingStringLookup());
    }

    protected AbstractStringFromTemplateBuilder(EscapingStringLookup escapingStringLookup) {
        this.lookup = escapingStringLookup;
    }

    /**
     * Add a mapping to the lookup.
     *
     * @param key         Key of the mapping (i.e. for keys like "JAVA:..." that would be "JAVA").
     * @param mappingFunc The mapping function handling this key.
     * @return Reference to self for chaining
     */
    public T addMapping(String key, BiFunction<Map<String, Object>, String, String> mappingFunc) {
        lookup.putMapping(key, mappingFunc);
        return self();
    }

    /**
     * Add a field
     *
     * @param key   Key of the field
     * @param value Value of the field
     * @return Reference to self for chaining
     */
    public T putField(String key, String value) {
        lookup.fieldMap.put(key, value);
        return self();
    }

    /**
     * Add a map of fields.
     *
     * @param fieldMap Map of fields to add to the current map.
     * @return Reference to self for chaining
     */
    public T putAllFields(Map<String, Object> fieldMap) {
        lookup.fieldMap.putAll(fieldMap);
        return self();
    }

    /**
     * Set the template to use
     *
     * @param template Template string
     * @return Reference to self for chaining
     */
    public T setTemplate(String template) {
        this.template = template;
        return self();
    }

    protected abstract T self();

    /**
     * Merge map and template and return the result.
     *
     * @return The String with its placeholders filled.
     */
    public String build() {
        return (new StringSubstitutor(lookup)).replace(this.template);
    }

    /**
     * Merge map and template and return the result.
     *
     * @param template The template string to use. This ignores the internal m_template.
     * @return The String with its placeholders filled.
     */
    public String buildWith(String template) {
        return (new StringSubstitutor(lookup)).replace(template);
    }

    /**
     * Merge fieldMap and template and return the result.
     *
     * @param fieldMap The fieldMap to use. This ignores the internal m_fieldMap.
     * @return The String with its placeholders filled.
     */
    public String buildWith(Map<String, Object> fieldMap) {
        return (new StringSubstitutor(fieldMap)).replace(this.template);
    }
}
