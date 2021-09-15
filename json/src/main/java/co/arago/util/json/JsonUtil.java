package co.arago.util.json;

/**
 * This class contains several common instances of JsonTool definitions.
 */
public class JsonUtil {

    /**
     * The default instance of the JsonTools.
     */
    public static final JsonTools DEFAULT = JsonTools.newBuilder().build();

    /**
     * These JsonTools fail on unknown properties.
     */
    public static final JsonTools STRICT = JsonTools.newBuilder().setFailOnUnknownProperties(true).build();

    /**
     * These JsonTools skip values that are set to null.
     */
    public static final JsonTools SKIP_NULL = JsonTools.newBuilder().setSkipNullMapValues(true).build();
}
