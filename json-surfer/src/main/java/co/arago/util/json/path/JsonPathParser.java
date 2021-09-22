package co.arago.util.json.path;

import co.arago.util.json.AbstractJsonParser;
import co.arago.util.json.JsonSurferTool;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.jsfr.json.Collector;
import org.jsfr.json.ValueBox;
import org.jsfr.json.exception.JsonSurfingException;

import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Wrapper around the mechanics of the {@link JsonSurferTool#getJsonSurfer()} for parsing data via JsonPaths.
 */
public class JsonPathParser extends AbstractJsonParser {

    public abstract static class Conf<T extends Conf<T>> extends AbstractJsonParser.Conf<T> {
        @Override
        protected abstract JsonPathParser build();
    }

    public final static class Builder extends Conf<Builder> {

        @Override
        protected Builder self() {
            return this;
        }

        @Override
        public JsonPathParser build() {
            return new JsonPathParser(this);
        }
    }

    /**
     * Protected constructor.
     *
     * @param builder Builder for this class.
     * @see #newBuilder()
     */
    protected JsonPathParser(Conf<?> builder) {
        super(builder);
    }

    /**
     * Get a new Builder
     *
     * @return New Builder instance.
     */
    public static Conf<?> newBuilder() {
        return new JsonPathParser.Builder();
    }

    /**
     * Get a matching {@link Collector} from the type of the input.
     *
     * @param input Any input type.
     * @return The matching collector for the input.
     * @throws JsonSurfingException When the input cannot be parsed into a Json String.
     */
    protected Collector getCollector(Object input) {
        if (input instanceof String)
            return jsonSurferTool.getJsonSurfer().collector((String) input);
        if (input instanceof InputStream)
            return jsonSurferTool.getJsonSurfer().collector((InputStream) input);

        try {
            return jsonSurferTool.getJsonSurfer().collector(jsonSurferTool.getJsonTools().toString(input));
        } catch (JsonProcessingException e) {
            throw new JsonSurfingException("Cannot parse json object.", e);
        }
    }

    /**
     * Get one value for each entry in {@link #jsonCuts}.
     *
     * @param collector The collector to use.
     * @param clazz     Type of the result values in the returned map.
     * @param <C>       Type of value
     * @return A map with Tuple [name, value] where value is one found by the matching key (json path) of
     * {@link #jsonCuts}.
     */
    protected <C> Map<String, C> getOneValues(Collector collector, Class<C> clazz) {
        Map<String, ValueBox<C>> valueBoxMap = new HashMap<>();
        for (Map.Entry<String, String> jsonCut : jsonCuts.entrySet()) {
            valueBoxMap.put(jsonCut.getValue() != null ? jsonCut.getValue() : "data", collector.collectOne(jsonCut.getKey(), clazz));
        }
        collector.exec();

        Map<String, C> result = new HashMap<>();

        for (Map.Entry<String, ValueBox<C>> resultBox : valueBoxMap.entrySet()) {
            result.put(resultBox.getKey(), resultBox.getValue().get());
        }

        return result;
    }

    /**
     * Get all values for each entry in {@link #jsonCuts}.
     *
     * @param collector The collector to use.
     * @param clazz     Type of the result values in the returned map.
     * @param <C>       Type of value
     * @return A map with Tuple [name, collection of values] where values are a collection of all found by the matching
     * key (json path) of {@link #jsonCuts}.
     */
    protected <C> Map<String, Collection<C>> getAllValues(Collector collector, Class<C> clazz) {
        Map<String, ValueBox<Collection<C>>> valueBoxMap = new HashMap<>();
        for (Map.Entry<String, String> jsonCut : jsonCuts.entrySet()) {
            valueBoxMap.put(jsonCut.getValue() != null ? jsonCut.getValue() : "data", collector.collectAll(jsonCut.getKey(), clazz));
        }
        collector.exec();

        Map<String, Collection<C>> result = new HashMap<>();

        for (Map.Entry<String, ValueBox<Collection<C>>> resultBox : valueBoxMap.entrySet()) {
            result.put(resultBox.getKey(), resultBox.getValue().get());
        }

        return result;
    }

    /**
     * @throws IllegalArgumentException When {@link #jsonCuts} is empty but at least one entry is needed.
     */
    protected void checkDataField() {
        if (jsonCuts.isEmpty())
            throw new IllegalArgumentException("The JsonPath is missing");
    }

    /**
     * Get one value from one path in {@link #jsonCuts} [jsonPath, name].
     *
     * @param jsonObject The object to parse.
     * @return The value found or null if nothing can be found.
     */
    public Object getOneFromOnePath(Object jsonObject) {
        return getOneFromOnePath(jsonObject, Object.class);
    }

    /**
     * Get one value from one path in {@link #jsonCuts} [jsonPath, name].
     *
     * @param jsonObject The object to parse.
     * @param clazz Class of result values.
     * @param <C> Type of result values.
     * @return The value found or null if nothing can be found.
     */
    public <C> C getOneFromOnePath(Object jsonObject, Class<C> clazz) {
        checkDataField();
        return getOneValues(getCollector(jsonObject), clazz).get("data");
    }

    /**
     * Get one value from all paths in {@link #jsonCuts} [jsonPath, name].
     *
     * @param jsonObject The object to parse.
     * @return Map of [name, the value found].
     */
    public Map<String, Object> getOneFromAllPaths(Object jsonObject) {
        return getOneFromAllPaths(jsonObject, Object.class);
    }

    /**
     * Get one value from all paths in {@link #jsonCuts} [jsonPath, name].
     *
     * @param jsonObject The object to parse.
     * @param clazz Class of result values.
     * @param <C> Type of result values.
     * @return Map of [name, the value found].
     */
    public <C> Map<String, C> getOneFromAllPaths(Object jsonObject, Class<C> clazz) {
        return getOneValues(getCollector(jsonObject), clazz);
    }

    /**
     * Get all matching values from one path in {@link #jsonCuts} [jsonPath, name].
     *
     * @param jsonObject The object to parse.
     * @return Collection of found values.
     */
    public Collection<Object> getAllFromOnePath(Object jsonObject) {
        return getAllFromOnePath(jsonObject, Object.class);
    }

    /**
     * Get all matching values from one path in {@link #jsonCuts} [jsonPath, name].
     *
     * @param jsonObject The object to parse.
     * @param clazz Class of result values.
     * @param <C> Type of result values.
     * @return Collection of found values.
     */
    public <C> Collection<C> getAllFromOnePath(Object jsonObject, Class<C> clazz) {
        checkDataField();
        return getAllValues(getCollector(jsonObject), clazz).get("data");
    }

    /**
     * Get all matching values from all paths in {@link #jsonCuts} [jsonPath, name].
     *
     * @param jsonObject The object to parse.
     * @return Map of [name, Collection of found values].
     */
    public Map<String, Collection<Object>> getAllFromAllPaths(Object jsonObject) {
        return getAllFromAllPaths(jsonObject, Object.class);
    }

    /**
     * Get all matching values from all paths in {@link #jsonCuts} [jsonPath, name].
     *
     * @param jsonObject The object to parse.
     * @param clazz Class of result values.
     * @param <C> Type of result values.
     * @return Map of [name, Collection of found values].
     */
    public <C> Map<String, Collection<C>> getAllFromAllPaths(Object jsonObject, Class<C> clazz) {
        return getAllValues(getCollector(jsonObject), clazz);
    }

}
