package co.arago.util.json.streaming;

import co.arago.util.json.AbstractJsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.jsfr.json.JsonPathListener;
import org.jsfr.json.ParsingContext;
import org.jsfr.json.SurfingConfiguration;

import java.util.Map;

/**
 * Basic class for a parser that can stream and execute callbacks on matching JsonPaths.
 */
public abstract class AbstractJsonStreamParser extends AbstractJsonParser {

    public static abstract class Conf<T extends Conf<T>> extends AbstractJsonParser.Conf<T> {

        protected final JsonStreamCallback callback;

        /**
         * Constructor
         *
         * @param callback The callback when a key (JsonPath) of {@link #jsonCuts} matches.
         */
        public Conf(JsonStreamCallback callback) {
            this.callback = callback;
        }

        protected abstract T self();

        public abstract AbstractJsonStreamParser build();
    }

    protected final JsonStreamCallback callback;

    /**
     * Listener class for JsonPath matches.
     */
    protected class CallbackJsonPathListener implements JsonPathListener {

        private final String field;

        /**
         * Constructor
         *
         * @param field Name of the data that is returned via {@link JsonStreamCallback#dataCallback(String, Object)}
         *              in {@link #onValue(Object, ParsingContext)}. If this is null, the name of the current
         *              {@link ParsingContext} or just 'data' will be used.
         */
        protected CallbackJsonPathListener(String field) {
            this.field = field;
        }

        /**
         * Return chunks of data to the callback interface.
         *
         * @param value   the value of json node that matches bound JsonPath
         * @param context parsing context
         */
        @Override
        public void onValue(Object value, ParsingContext context) {
            try {
                String field = (this.field == null
                        ? (context.getCurrentFieldName() == null ? "data" : context.getCurrentFieldName())
                        : this.field);
                Object parsedValue = (value instanceof JsonNode)
                        ? jsonSurferTool.getJsonTools().transformObject(value, Object.class)
                        : value;

                callback.dataCallback(field, parsedValue);
            } catch (Exception x) {
                callback.dataException(x);
            }
        }
    }

    /**
     * Protected constructor.
     *
     * @param builder The builder for this class
     */
    protected AbstractJsonStreamParser(Conf<?> builder) {
        super(builder);
        if (builder.callback == null)
            throw new IllegalArgumentException("callback is missing");

        this.callback = builder.callback;
    }

    /**
     * @return A SurfingConfiguration built from {@link #jsonCuts}.
     * @see CallbackJsonPathListener
     */
    protected SurfingConfiguration createSurfingConfiguration() {
        SurfingConfiguration.Builder configBuilder = jsonSurferTool.getJsonSurfer().configBuilder();

        for (Map.Entry<String, String> jsonPathEntry : jsonCuts.entrySet()) {
            try {
                configBuilder.bind(jsonPathEntry.getKey(), new CallbackJsonPathListener(jsonPathEntry.getValue()));
            } catch (ParseCancellationException e) {
                throw new IllegalArgumentException(
                        "Invalid jsonPath '" + jsonPathEntry.getKey() + "' in definition of 'jsonCuts'.", e);
            }
        }

        return configBuilder.build();
    }
}
