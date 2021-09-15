package co.arago.util.json;

import org.jsfr.json.JsonSurfer;
import org.jsfr.json.JsonSurferJackson;

/**
 * Creates a {@link JsonSurfer} from the mapper of the supplied {@link JsonTools}.
 */
public class JsonSurferTool {

    private final JsonSurfer jsonSurfer;
    private final JsonTools jsonTools;

    /**
     * Protected constructor. See {@link #newInstance()} and {@link #newInstance(JsonTools)}.
     *
     * @param jsonTools {@link JsonTools} with mapper for the {@link #jsonSurfer}.
     */
    protected JsonSurferTool(JsonTools jsonTools) {
        this.jsonTools = jsonTools;
        this.jsonSurfer = JsonSurferJackson.createSurfer(jsonTools.getMapper().getFactory());
    }

    /**
     * Static constructor
     *
     * @return New instance of {@link #JsonSurferTool(JsonTools)}.
     */
    public static JsonSurferTool newInstance() {
        return newInstance(JsonUtil.DEFAULT);
    }

    /**
     * Static constructor
     *
     * @param jsonTools {@link JsonTools} with mapper for the {@link #jsonSurfer}.
     * @return New instance of {@link #JsonSurferTool(JsonTools)}.
     */
    public static JsonSurferTool newInstance(JsonTools jsonTools) {
        return new JsonSurferTool(jsonTools);
    }


    public JsonSurfer getJsonSurfer() {
        return jsonSurfer;
    }

    public JsonTools getJsonTools() {
        return jsonTools;
    }

}
