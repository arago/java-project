package co.arago.util.text.templates;

import org.apache.commons.text.StringEscapeUtils;
import org.apache.commons.text.StringSubstitutor;

/**
 * Encapsulates {@link StringSubstitutor} and adds escaping of values via StringEscapeUtils.
 * Has default mappings for JAVA, JAVASCRIPT and CSV escapes of values.
 */
public class EscapingStringFromTemplateBuilder extends AbstractStringFromTemplateBuilder<EscapingStringFromTemplateBuilder> {
    /**
     * The class for StringLookup that uses StringEscapeUtils for escaping Strings
     */
    protected static class EscapingStringLookup extends AbstractStringFromTemplateBuilder.EscapingStringLookup {

        static {
            defaultMapping.put("JAVA", (map, key) ->
                    StringEscapeUtils.escapeJava(getDefaultFromMap(map, key))
            );
            defaultMapping.put("JAVASCRIPT", (map, key) ->
                    StringEscapeUtils.escapeEcmaScript(getDefaultFromMap(map, key))
            );
            defaultMapping.put("CSV", (map, key) ->
                    StringEscapeUtils.escapeCsv(getDefaultFromMap(map, key))
            );
        }

    }

    /**
     * Constructor using defaults.
     */
    protected EscapingStringFromTemplateBuilder() {
        super(new EscapingStringLookup());
    }

    public static EscapingStringFromTemplateBuilder newInstance() {
        return new EscapingStringFromTemplateBuilder();
    }

    @Override
    protected EscapingStringFromTemplateBuilder self() {
        return this;
    }

}
