package co.arago.util.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.kjetland.jackson.jsonSchema.JsonSchemaConfig;
import com.kjetland.jackson.jsonSchema.JsonSchemaGenerator;

/**
 * Generate JsonSchema from provided {@link JsonTools}.
 */
public class JsonSchemaTool {
    private final JsonSchemaGenerator jsonSchemaGenerator;

    /**
     * Constructor
     *
     * @param jsonTools {@link JsonTools} with mapper for the {@link #jsonSchemaGenerator}.
     */
    public JsonSchemaTool(JsonTools jsonTools) {
        jsonSchemaGenerator = new JsonSchemaGenerator(
                jsonTools.getMapper(),
                false,
                JsonSchemaConfig.vanillaJsonSchemaDraft4()
        );
    }

    /**
     * Return a Json Schema
     *
     * @param clazz Class type to get the schema of.
     * @return The JsonSchema for the class.
     */
    JsonNode getSchema(Class<?> clazz) {
        return jsonSchemaGenerator.generateJsonSchema(clazz);
    }

    /**
     * Return a Json Schema
     *
     * @param obj Object to get the schema of.
     * @return The JsonSchema for the object.
     */
    JsonNode getSchema(Object obj) {
        return getSchema(obj.getClass());
    }

    /**
     * Return a Json Schema
     *
     * @param className Class name to get the schema of.
     * @return The JsonSchema for the class named className.
     */
    JsonNode getSchema(String className) throws ClassNotFoundException {
        return getSchema(Class.forName(className));
    }

}
