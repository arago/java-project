package co.arago.util.json.streaming;

import org.jsfr.json.ResumableParser;

import java.io.InputStream;

/**
 * A json reader that receives data via an InputStream. Whenever a chunk of json data defined by jsonPath is ready to
 * be processed it calls the callback with the chunk of JSON data.
 */
public class InputStreamJsonParser extends AbstractJsonStreamParser {

    public abstract static class Conf<T extends Conf<T>> extends AbstractJsonStreamParser.Conf<T> {

        InputStream inputStream;

        public Conf(JsonStreamCallback callback) {
            super(callback);
        }

        /**
         * Set the inputStream to parse
         *
         * @param inputStream The inputStream to parse.
         * @return {@link #self()}
         */
        public T setInputStream(InputStream inputStream) {
            this.inputStream = inputStream;
            return self();
        }

        @Override
        public abstract InputStreamJsonParser build();
    }

    public final static class Builder extends Conf<Builder> {

        public Builder(JsonStreamCallback callback) {
            super(callback);
        }

        @Override
        protected InputStreamJsonParser.Builder self() {
            return this;
        }

        @Override
        public InputStreamJsonParser build() {
            return new InputStreamJsonParser(this);
        }
    }

    /**
     * Reference to the parser that is used here
     */
    protected final ResumableParser parser;

    /**
     * Protected constructor
     *
     * @param builder The builder for this class.
     */
    protected InputStreamJsonParser(Conf<?> builder) {
        super(builder);
        if (builder.inputStream == null)
            throw new IllegalArgumentException("inputStream is missing");

        parser = jsonSurferTool
                .getJsonSurfer()
                .createResumableParser(builder.inputStream, createSurfingConfiguration());
    }

    /**
     * Get Builder
     *
     * @param callback Callback for the matching keys (JsonPath) of {@link #jsonCuts}.
     * @return New instance of {@link InputStreamJsonParser.Builder}
     */
    public static Conf<?> newBuilder(JsonStreamCallback callback) {
        return new InputStreamJsonParser.Builder(callback);
    }

    /**
     * Run the parser
     */
    void execute() {
        try {
            parser.parse();
            callback.dataFinished();
        } catch (Exception x) {
            callback.dataException(x);
        }
    }
}
