package co.arago.util.json;

import java.util.HashMap;
import java.util.Map;

/**
 * Basic class of a JsonSurfer containing a map of JsonPaths and field names.
 */
public abstract class AbstractJsonParser {

    public static abstract class Conf<T extends Conf<T>> {

        /**
         * Tuple of JsonPath and 'field' for completed chunk detection. If value it is null, use the current field name
         * of the JSON read or just 'data' as name.
         */
        protected final Map<String, String> jsonCuts = new HashMap<>();

        protected JsonSurferTool jsonSurferTool;


        /**
         * Set a specific JsonSurferTool.
         *
         * @param jsonSurferTool The JsonSurferTool.
         * @return {@link #self()}
         */
        public T setJsonSurferTool(JsonSurferTool jsonSurferTool) {
            this.jsonSurferTool = jsonSurferTool;
            return self();
        }

        /**
         * Add a single jsonPath to parse for.
         *
         * @param jsonPath The jsonPath
         * @return {@link #self()}
         */
        public T addJsonPath(String jsonPath) {
            return addJsonPath(jsonPath, null);
        }

        /**
         * Add a single jsonPath and field name to parse for.
         *
         * @param jsonPath  The jsonPath
         * @param fieldName Name of the field
         * @return {@link #self()}
         */
        public T addJsonPath(String jsonPath, String fieldName) {
            jsonCuts.put(jsonPath, fieldName);
            return self();
        }

        /**
         * Add jsonPaths and field names to parse for.
         *
         * @param jsonCuts A map of jsonPaths and fieldNames.
         * @return {@link #self()}
         */
        public T setJsonCuts(Map<String, String> jsonCuts) {
            this.jsonCuts.putAll(jsonCuts);
            return self();
        }

        protected abstract T self();

        protected abstract AbstractJsonParser build();

    }

    /**
     * Tuple of JsonPath and 'field' for completed chunk detection. If value it is null, use the current field name
     * of the JSON read or just 'data' as name.
     */
    protected final Map<String, String> jsonCuts;

    protected final JsonSurferTool jsonSurferTool;

    protected AbstractJsonParser(Conf<?> builder) {
        this.jsonCuts = builder.jsonCuts;
        this.jsonSurferTool = (builder.jsonSurferTool != null ? builder.jsonSurferTool : JsonSurferTool.newInstance());
    }
}
