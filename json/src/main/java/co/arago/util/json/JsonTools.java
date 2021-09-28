package co.arago.util.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.core.json.JsonWriteFeature;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.DeserializationProblemHandler;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * Wrapper for several Jackson methods around the {@link JsonTools#mapper}.
 */
public class JsonTools {

    private final static Logger log = LoggerFactory.getLogger(JsonTools.class);

    public static abstract class Conf<T extends Conf<T>> {
        protected boolean skipNullMapValues = false;
        protected boolean failOnUnknownProperties = false;
        protected boolean failOnEmptyBeans = false;
        protected boolean lenientParsing = false;
        protected boolean escapeNonAscii = false;
        protected boolean warnOnUnknownProperties = false;

        /**
         * Does not serialize values that are set to 'null' in a json-object to the output. Default is false.
         *
         * @param skipNullMapValues the flag.
         * @return {@link #self()}
         */
        public T setSkipNullMapValues(boolean skipNullMapValues) {
            this.skipNullMapValues = skipNullMapValues;
            return self();
        }

        /**
         * Ignore properties on deserialization that are not used to construct an object. Default is false.
         *
         * @param failOnUnknownProperties the flag.
         * @return {@link #self()}
         */
        public T setFailOnUnknownProperties(boolean failOnUnknownProperties) {
            this.failOnUnknownProperties = failOnUnknownProperties;
            return self();
        }

        /**
         * Allow created beans to be empty
         *
         * @param failOnEmptyBeans the flag. Default is false.
         * @return {@link #self()}
         */
        public T setFailOnEmptyBeans(boolean failOnEmptyBeans) {
            this.failOnEmptyBeans = failOnEmptyBeans;
            return self();
        }

        /**
         * Try your best to parse even illegal JSON. Default is false.
         *
         * @param lenientParsing the flag.
         * @return {@link #self()}
         */
        public T setLenientParsing(boolean lenientParsing) {
            this.lenientParsing = lenientParsing;
            return self();
        }

        /**
         * Escape all non-ascii characters, even UTF8. Default is false.
         *
         * @param escapeNonAscii the flag.
         * @return {@link #self()}
         */
        public T setEscapeNonAscii(boolean escapeNonAscii) {
            this.escapeNonAscii = escapeNonAscii;
            return self();
        }

        /**
         * An instance of the JsonTools that warns in log when unknown properties are encountered. Default is false.
         *
         * @param warnOnUnknownProperties the flag.
         * @return {@link #self()}
         */
        public T setWarnOnUnknownProperties(boolean warnOnUnknownProperties) {
            this.warnOnUnknownProperties = warnOnUnknownProperties;
            return self();
        }

        protected abstract T self();

        public abstract JsonTools build();
    }

    protected static final class Builder extends Conf<Builder> {

        @Override
        protected Builder self() {
            return this;
        }

        /**
         * Build the {@link JsonTools} with the parameters of this Builder.
         *
         * @return New instance of {@link JsonTools}.
         */
        @Override
        public JsonTools build() {
            return new JsonTools(this);
        }
    }

    /**
     * Json mapper who does the mapping
     */
    private final JsonMapper mapper;

    protected JsonTools(Conf<?> builder) {
        JsonMapper.Builder mapperBuilder = JsonMapper.builder();

        mapperBuilder.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, builder.failOnUnknownProperties);
        mapperBuilder.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, builder.failOnEmptyBeans);
        mapperBuilder.configure(JsonWriteFeature.ESCAPE_NON_ASCII, builder.escapeNonAscii);

        if (builder.skipNullMapValues)
            mapperBuilder.serializationInclusion(JsonInclude.Include.NON_NULL);

        if (builder.lenientParsing) {
            mapperBuilder.configure(JsonReadFeature.ALLOW_UNQUOTED_FIELD_NAMES, true);
            mapperBuilder.configure(JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS, true);
            mapperBuilder.configure(JsonReadFeature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER, true);
            mapperBuilder.configure(JsonReadFeature.ALLOW_LEADING_ZEROS_FOR_NUMBERS, true);
            mapperBuilder.configure(JsonReadFeature.ALLOW_SINGLE_QUOTES, true);
        }

        mapperBuilder.defaultPrettyPrinter(PrettyPrinterFactory.createDefault());

        mapperBuilder.addModule(new JavaTimeModule());
        mapperBuilder.addModule(new Jdk8Module());


        if (builder.warnOnUnknownProperties) {
            mapperBuilder.addHandler(new DeserializationProblemHandler() {
                @Override
                public boolean handleUnknownProperty(
                        DeserializationContext context,
                        JsonParser p,
                        JsonDeserializer<?> deserializer,
                        Object beanOrClass,
                        String propertyName
                ) throws IOException {
                    if (log.isWarnEnabled()) {
                        String pointer = p.getParsingContext().getParent().pathAsPointer().toString();
                        log.warn(
                                "Skipping deserialization of unknown property \"{}\" at {} for target class {}",
                                propertyName,
                                (pointer != null ? pointer : "/"),
                                beanOrClass.getClass().getCanonicalName()
                        );
                    }
                    p.skipChildren();
                    return true;
                }
            });
        }

        mapper = mapperBuilder.build();
    }

    /**
     * The builder for JsonTools.
     *
     * @return new Instance of the {@link Builder}.
     */
    public static Conf<?> newBuilder() {
        return new Builder();
    }

    /**
     * Get the internal mapper. Use {@link JsonMapper#copy()} if you just want this mapper as a template for your own.
     *
     * @return the {@link #mapper}
     */
    public JsonMapper getMapper() {
        return mapper;
    }

    /**
     * Parse string into JsonNode tree
     *
     * @param data String data
     * @return The JsonNode containing the data
     * @throws JsonProcessingException On error
     */
    public JsonNode readTree(String data) throws JsonProcessingException {
        return mapper.readTree(data);
    }

    /**
     * Parse stream into JsonNode tree
     *
     * @param stream A stream to read from
     * @return The JsonNode containing the data
     * @throws IOException On IO error
     */
    public JsonNode readTree(InputStream stream) throws IOException {
        return mapper.readTree(stream);
    }

    /**
     * Parse File into JsonNode tree
     *
     * @param file File to read
     * @return The JsonNode containing the data
     * @throws IOException On IO error
     */
    public JsonNode readTree(File file) throws IOException {
        return mapper.readTree(file);
    }

    /**
     * Transforms a JSON structure read from an inputStream into an Object using injectMap
     * as additional parameters.
     *
     * @param inputStream Input stream with data
     * @param targetClass The target type of object to create
     * @param injectMap   Map with additional inject values
     * @param <T>         Type of the object
     * @return The created Object
     * @throws IOException If something else goes wrong
     */
    public <T> T toObject(InputStream inputStream, Class<T> targetClass, Map<String, Object> injectMap) throws
            IOException {
        return mapper.reader(new InjectableValues.Std(injectMap)).forType(targetClass).readValue(inputStream);
    }

    /**
     * Transforms a JSON structure read from an inputStream into an Object.
     *
     * @param inputStream Input stream with data
     * @param targetClass The target type of object to create
     * @param <T>         Type of the object
     * @return The created Object
     * @throws IOException If something else goes wrong
     */
    public <T> T toObject(InputStream inputStream, Class<T> targetClass) throws IOException {
        return mapper.readValue(inputStream, targetClass);
    }

    /**
     * Transforms a JSON structure read from a file into an Object using injectMap
     * as additional parameters.
     *
     * @param file        The file with the JSON structure to convert
     * @param targetClass The target type of object to create
     * @param injectMap   Map with additional inject values
     * @param <T>         Type of the object
     * @return The created Object
     * @throws IOException If something else goes wrong
     */
    public <T> T toObject(File file, Class<T> targetClass, Map<String, Object> injectMap) throws IOException {
        return mapper.reader(new InjectableValues.Std(injectMap)).forType(targetClass).readValue(file);
    }

    /**
     * Transforms a JSON structure read from a file into an Object.
     *
     * @param file        The file with the JSON structure to convert
     * @param targetClass The target type of object to create
     * @param <T>         Type of the object
     * @return The created Object
     * @throws IOException If something else goes wrong
     */
    public <T> T toObject(File file, Class<T> targetClass) throws IOException {
        return mapper.readValue(file, targetClass);
    }

    /**
     * Transforms a JSON structure given as String into an Object.
     *
     * @param json        The JSON structure to convert (String)
     * @param targetClass The target type of object to create
     * @param <T>         Type of the object
     * @return The created Object
     * @throws JsonProcessingException On Json Error
     */
    public <T> T toObject(String json, Class<T> targetClass) throws JsonProcessingException {
        return mapper.readValue(json, targetClass);
    }

    /**
     * Transforms a JSON structure given as String into an Object.
     *
     * @param json      The JSON structure to convert (String)
     * @param classname The target type of object to create given as string
     * @return The created Object
     * @throws ClassNotFoundException  When the classname is no name of a valid class
     * @throws JsonProcessingException On Json Error
     */
    public Object toObject(String json, String classname) throws ClassNotFoundException, JsonProcessingException {
        return toObject(json, (Class<?>) Class.forName(classname));
    }

    /**
     * Transforms a JSON structure given as String into an Object.
     *
     * @param json        The JSON structure to convert (String)
     * @param targetClass The target type of object to create
     * @param injectMap   Map with additional inject values
     * @param <T>         Type of the object
     * @return The created Object
     * @throws JsonProcessingException When the json is invalid
     */
    public <T> T toObject(String json, Class<T> targetClass, Map<String, Object> injectMap) throws
            JsonProcessingException {
        return mapper.reader(new InjectableValues.Std(injectMap)).forType(targetClass).readValue(json);
    }

    /**
     * Transforms a JSON structure given as Object into an Object.
     *
     * @param json        The JSON structure to convert (Object)
     * @param targetClass The target type of object to create
     * @param injectMap   Map with additional inject values
     * @param <T>         Type of the object
     * @return The created Object
     */
    public <T> T transformObject(Object json, Class<T> targetClass, Map<String, Object> injectMap) {
        JsonMapper copy = mapper.copy();
        copy.setInjectableValues(new InjectableValues.Std(injectMap));
        return copy.convertValue(json, targetClass);
    }

    /**
     * Transforms a JSON structure given as Object into an Object.
     *
     * @param json      The JSON structure to convert (Object)
     * @param className The target type of object to create given as string
     * @return The created Object
     * @throws ClassNotFoundException When the className is no name of a valid class
     */
    public Object transformObject(Object json, String className) throws ClassNotFoundException {
        return transformObject(json, (Class<?>) Class.forName(className));
    }

    /**
     * Transforms a JSON structure given as Object into an Object.
     *
     * @param json        The JSON structure to convert (Object)
     * @param targetClass The target type of object to create
     * @param <T>         Type of the object
     * @return The created Object
     */
    public <T> T transformObject(Object json, Class<T> targetClass) {
        return mapper.convertValue(json, targetClass);
    }

    /**
     * Transforms a JSON structure given as Object into an Object cast by
     * TypeReference.
     *
     * @param json          The JSON structure to convert (Object)
     * @param typeReference The target type of object to create
     * @param <T>           Type of the object
     * @return The created Object
     */
    public <T> T transformObject(Object json, TypeReference<T> typeReference) {
        return mapper.convertValue(json, typeReference);
    }

    /**
     * Transforms a JSON structure given as Object into an Object cast by
     * TypeReference.
     *
     * @param inputStream   Input stream with data
     * @param typeReference The target type of object to create
     * @param <T>           Type of the object
     * @return The created Object
     */
    public <T> T toObject(InputStream inputStream, TypeReference<T> typeReference) {
        return mapper.convertValue(inputStream, typeReference);
    }

    /**
     * Transforms a JSON structure given as String into an Object cast by
     * TypeReference.
     *
     * @param json          The JSON structure to convert (String)
     * @param typeReference The target type of object to create
     * @param <T>           Type of the object
     * @return The created Object
     * @throws JsonProcessingException On Json Error
     */
    public <T> T toObject(String json, TypeReference<T> typeReference) throws JsonProcessingException {
        return mapper.readValue(json, typeReference);
    }

    /**
     * Write an object as json string
     *
     * @param json The object
     * @return Object converted to a json string
     * @throws JsonProcessingException If the object cannot be converted to a
     *                                 string.
     */
    public String toString(Object json) throws JsonProcessingException {
        return mapper.writeValueAsString(json);
    }

    /**
     * Write an object as pretty json string
     *
     * @param json The object
     * @return Object converted to a json string
     * @throws JsonProcessingException If the object cannot be converted to a
     *                                 string.
     */
    public String toPrettyString(Object json) throws JsonProcessingException {
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
    }

    /**
     * Prettify a json string
     *
     * @param json The json string
     * @return The prettified json string
     * @throws IOException If the string is no valid JSON.
     */
    public String toPrettyString(String json) throws IOException {
        return toPrettyString(toPOJO(json));
    }

    /**
     * Read an InputStream with json data and return a prettified string.
     *
     * @param stream The stream with json data
     * @return The prettified json string
     * @throws IOException If the inputStream contains no valid JSON.
     */
    public String toPrettyString(InputStream stream) throws IOException {
        return toPrettyString(toPOJO(stream));
    }

    /**
     * Read a File with json data and return a prettified string.
     *
     * @param file The file with json data
     * @return The prettified json string
     * @throws IOException If the file contains no valid JSON.
     */
    public String toPrettyString(File file) throws IOException {
        return toPrettyString(toPOJO(file));
    }

    /**
     * Transforms a JSON structure given as JsonNode into an Object.
     *
     * @param json        The JSON structure to convert (JsonNode)
     * @param targetClass The target type of object to create
     * @param <T>         Type of the object
     * @return The created Object
     * @throws JsonProcessingException On processing error
     */
    public <T> T toObject(JsonNode json, Class<T> targetClass) throws JsonProcessingException {
        return mapper.treeToValue(json, targetClass);
    }

    /**
     * Transforms a JSON structure given as JsonNode into an Object.
     *
     * @param json      The JSON structure to convert (JsonNode)
     * @param className The target type of object to create
     * @return The created Object
     * @throws ClassNotFoundException  When the className is not the name of a valid class.
     * @throws JsonProcessingException On processing error
     */
    public Object toObject(JsonNode json, String className) throws ClassNotFoundException, JsonProcessingException {
        return mapper.treeToValue(json, (Class<?>) Class.forName(className));
    }

    /**
     * This function tries to transform either a String or any Object into another object via Jackson.
     *
     * @param json      The json data. String or any Object. String uses {@link JsonTools#toObject(String, String)},
     *                  everything else uses {@link JsonTools#transformObject(Object, String)}. If the string is blank, null is returned.
     * @param className Name of the resulting class that shall be created. The default is {@link Map}.
     * @return The generated object or null if no object can be created (String is blank for instance).
     * @throws ClassNotFoundException  When the className is not the name of a valid class.
     * @throws JsonProcessingException On processing error
     */
    public Object toObjectEx(Object json, String className) throws ClassNotFoundException, JsonProcessingException {
        if (json instanceof String) {
            String str = (String) json;
            return str.isBlank() ? null : toObject(str, (Class<?>) Class.forName(className));
        } else {
            return transformObject(json, (Class<?>) Class.forName(className));
        }

    }

    /**
     * This function tries to transform either a String or any Object into a map via Jackson.
     *
     * @param json The json data. String or any Object. String uses {@link JsonTools#toObject(String, String)},
     *             everything else uses {@link JsonTools#transformObject(Object, String)}. If the string is blank, null is returned.
     * @return The generated map or null if no map can be created (String is blank for instance).
     * @throws IOException When the json is invalid
     */
    public Object toObjectEx(Object json) throws IOException {
        try {
            return toObjectEx(json, "java.util.Map");
        } catch (ClassNotFoundException e) {
            // will not happen
            return null;
        }
    }

    /**
     * Create a POJO structure from a string
     *
     * @param string String with JSON
     * @return The POJO Object
     * @throws JsonProcessingException On errors with JSON conversion
     */
    public Object toPOJO(String string) throws JsonProcessingException {
        return toObject(string, Object.class);
    }

    /**
     * Create a POJO structure from a file containing JSON
     *
     * @param file File with JSON data
     * @return The POJO Object
     * @throws IOException On errors with JSON conversion
     */
    public Object toPOJO(File file) throws IOException {
        return toObject(file, Object.class);
    }

    /**
     * Create a POJO structure from an InputStream containing JSON
     *
     * @param inputStream InputStream with JSON data
     * @return The POJO Object
     * @throws IOException On errors with JSON conversion
     */
    public Object toPOJO(InputStream inputStream) throws IOException {
        return toObject(inputStream, Object.class);
    }

    /**
     * Clone a JSON object via Jackson.
     * <p>
     * This might not be an exact copy since some configuration parameters might
     * interfere with this.
     * <p>
     * The class to be cloned needs either a default constructor and have its fields or setters and getters public, or
     * it needs a Constructor with valid @{@link com.fasterxml.jackson.annotation.JsonCreator}
     * / @{@link com.fasterxml.jackson.annotation.JsonProperty} annotations.
     *
     * @param object The object to clone.
     * @param clazz  The class to cast the result to.
     * @param <T>    Type of desired object.
     * @return The clone of object or null if object is null.
     * @throws JsonProcessingException If the object cannot be transformed to a JSON String.
     */
    public <T> T clone(T object, Class<T> clazz) throws JsonProcessingException {
        if (object == null)
            return null;

        return toObject(toString(object), clazz);
    }

    /**
     * Clone a JSON object via Jackson.
     * <p>
     * This might not be an exact copy since some configuration parameters might
     * interfere with this.
     * <p>
     * The class to be cloned needs either a default constructor and have its fields or setters and getters public, or
     * it needs a Constructor with valid @{@link com.fasterxml.jackson.annotation.JsonCreator}
     * / @{@link com.fasterxml.jackson.annotation.JsonProperty} annotations.
     *
     * @param object        The object to clone.
     * @param typeReference The type reference for the result.
     * @param <T>           Type of desired object.
     * @return The clone of object or null if object is null.
     * @throws JsonProcessingException If the object cannot be transformed to a JSON String.
     */
    public <T> T clone(T object, TypeReference<T> typeReference) throws JsonProcessingException {
        if (object == null)
            return null;

        return toObject(toString(object), typeReference);
    }

}

