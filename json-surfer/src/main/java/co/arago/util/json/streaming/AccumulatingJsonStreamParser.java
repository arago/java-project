package co.arago.util.json.streaming;

import org.apache.commons.lang3.StringUtils;
import org.jsfr.json.NonBlockingParser;
import org.jsfr.json.exception.JsonSurfingException;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * A json reader that receives chunks of json data and accumulates them in a non-blocking-parser until a
 * chunk of json data defined by jsonPath is ready to be processed. Calls the callback with the chunk of JSON data.
 */
public class AccumulatingJsonStreamParser extends AbstractJsonStreamParser implements AutoCloseable {

    public abstract static class Conf<T extends Conf<T>> extends AbstractJsonStreamParser.Conf<T> {

        /**
         * The default charset UTF_8. Change this to the intended charset of the incoming chunks in
         * {@link #feed(java.lang.String)}
         */
        protected Charset charset = StandardCharsets.UTF_8;

        /**
         * Set the charset. Default is UTF-8.
         *
         * @param charset The Charset to use.
         * @return {@link #self()}
         */
        public T setCharset(Charset charset) {
            this.charset = charset;
            return self();
        }

        public Conf(JsonStreamCallback callback) {
            super(callback);
        }

        @Override
        public abstract AccumulatingJsonStreamParser build();
    }

    public final static class Builder extends Conf<Builder> {

        public Builder(JsonStreamCallback callback) {
            super(callback);
        }

        @Override
        protected Builder self() {
            return this;
        }

        @Override
        public AccumulatingJsonStreamParser build() {
            return new AccumulatingJsonStreamParser(this);
        }
    }

    /**
     * Reference to the parser that is used here
     */
    protected final NonBlockingParser parser;

    protected final Charset charset;

    /**
     * Protected constructor
     *
     * @param builder The builder for this class.
     */
    protected AccumulatingJsonStreamParser(Conf<?> builder) {
        super(builder);

        this.charset = builder.charset;

        parser = jsonSurferTool.getJsonSurfer().createNonBlockingParser(createSurfingConfiguration());
    }

    /**
     * Get Builder
     *
     * @param callback Callback for the matching keys (JsonPath) of {@link #jsonCuts}.
     * @return New instance of {@link AccumulatingJsonStreamParser.Builder}
     */
    public static Conf<?> newBuilder(JsonStreamCallback callback) {
        return new AccumulatingJsonStreamParser.Builder(callback);
    }

    /**
     * Add data to the parser.
     *
     * @param data Incoming data for the parser.
     */
    public void feed(String data) {
        if (StringUtils.isNotEmpty(data)) {
            feed(data.getBytes(charset));
        }
    }

    /**
     * Add data to the parser.
     *
     * @param data Incoming data for the parser as byte[]. Ignores the charset.
     */
    public void feed(byte[] data) {
        try {
            if (!parser.feed(data, 0, data.length))
                throw new JsonSurfingException("Parser could not parse all of incoming data.", new IllegalArgumentException());
        } catch (Exception e) {
            callback.dataException(e);
        }
    }

    /**
     * Tell the parser that all data for it has ended.
     */
    @Override
    public void close() throws Exception {
        parser.endOfInput();
        callback.dataFinished();
    }

}
