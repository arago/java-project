package co.arago.util.json.streaming;

import java.io.IOException;

/**
 * Callback interface for incoming JSON data
 */
public interface JsonStreamCallback {
    /**
     * Callback function for incoming data
     *
     * @param name Identifier for the data to handle
     * @param data Data payload
     * @throws IOException When the source of the data for the callback has an error.
     */
    void dataCallback(String name, Object data) throws IOException;

    /**
     * Callback on exception
     *
     * @param t Throwable with information about what went wrong.
     */
    void dataException(Throwable t);

    /**
     * Called when all data has been handled
     */
    void dataFinished();
}
