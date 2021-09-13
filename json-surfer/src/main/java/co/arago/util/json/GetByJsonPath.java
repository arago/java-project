package co.arago.util.json;

import org.jsfr.json.exception.JsonSurfingException;

import java.util.Collection;

/**
 * Get an arbitrary field from any Object by using a JsonPath.
 * The Object must be convertable to a String via {@link JsonTools#toString(Object)}.
 * <p>
 * This class is a simplification of {@link JsonPathParser}.
 */
public class GetByJsonPath {

    protected final String jsonPath;

    private final JsonPathParser jsonPathParser;

    /**
     * Protected constructor.
     * <p>
     * Use {@link #newWith(String)} or {@link #newWith(String, JsonTools)}.
     *
     * @param jsonPath  The jsonPath to use
     * @param jsonTools The specific jsonTools to use
     */
    protected GetByJsonPath(String jsonPath, JsonTools jsonTools) {
        this.jsonPath = jsonPath;
        this.jsonPathParser = JsonPathParser.newBuilder()
                .setJsonSurferTool(JsonSurferTool.newInstance(jsonTools))
                .addJsonPath(jsonPath).
                build();
    }

    /**
     * Static constructor. Uses JsonTools.DEFAULT.
     *
     * @param jsonPath The jsonPath to use
     * @return New instance of {@link GetByJsonPath}
     */
    public static GetByJsonPath newWith(String jsonPath) {
        return newWith(jsonPath, JsonTools.DEFAULT);
    }

    /**
     * Static constructor.
     *
     * @param jsonPath  The jsonPath to use
     * @param jsonTools The specific jsonTools to use
     * @return New instance of {@link GetByJsonPath}
     */
    public static GetByJsonPath newWith(String jsonPath, JsonTools jsonTools) {
        return new GetByJsonPath(jsonPath, jsonTools);
    }

    /**
     * Get as simple Object
     *
     * @param jsonObject Object the jsonPath is applied to.
     * @return The jsonObject found or null.
     * @throws JsonSurfingException When the object cannot be transformed into a Json string.
     */
    public Object get(Object jsonObject) {
        return jsonPathParser.getOneFromOnePath(jsonObject);
    }

    /**
     * Get as Object of Class T
     *
     * @param jsonObject Object the jsonPath is applied to.
     * @param clazz      The class of the desired result.
     * @param <T>        Type of the result.
     * @return The jsonObject found or null.
     * @throws JsonSurfingException When the object cannot be transformed into a Json string.
     */
    public <T> T get(Object jsonObject, Class<T> clazz) {
        return jsonPathParser.getOneFromOnePath(jsonObject, clazz);
    }

    /**
     * Get all as Collection of Objects
     *
     * @param jsonObject Object the jsonPath is applied to.
     * @return The list of Objects found.
     * @throws JsonSurfingException When the object cannot be transformed into a Json string.
     */
    public Collection<Object> getAll(Object jsonObject) {
        return jsonPathParser.getAllFromOnePath(jsonObject);
    }

    /**
     * Get all as Collection of Objects of Class T
     *
     * @param jsonObject Object the jsonPath is applied to.
     * @param clazz      The class of the desired result.
     * @param <T>        Type of the result.
     * @return The list of Objects found.
     * @throws JsonSurfingException When the object cannot be transformed into a Json string.
     */
    public <T> Collection<T> getAll(Object jsonObject, Class<T> clazz) {
        return jsonPathParser.getAllFromOnePath(jsonObject, clazz);
    }

}
