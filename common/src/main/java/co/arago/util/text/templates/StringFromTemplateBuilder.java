package co.arago.util.text.templates;

import org.apache.commons.text.StringSubstitutor;

/**
 * Encapsulates {@link StringSubstitutor} and adds escaping of values via StringEscapeUtils.
 * Has no default value escaping.
 */
public class StringFromTemplateBuilder extends AbstractStringFromTemplateBuilder<StringFromTemplateBuilder> {

    protected StringFromTemplateBuilder() {
    }

    public static StringFromTemplateBuilder newInstance() {
        return new StringFromTemplateBuilder();
    }

    @Override
    protected StringFromTemplateBuilder self() {
        return this;
    }
}
