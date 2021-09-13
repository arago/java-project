package co.arago.util.collections.expiringstore;

import co.arago.util.collections.expiringstore.exceptions.StoreItemExistsException;
import co.arago.util.collections.expiringstore.exceptions.StoreItemExpiredException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Contains the store items with an "expiresAt" after which the items are automatically removed from the store.
 *
 * @param <T> Type of items to store
 */
public class ExpiringStore<T> implements AutoCloseable {

    private final static Logger log = LoggerFactory.getLogger(ExpiringStore.class);

    private static final AtomicInteger counter = new AtomicInteger(0);

    private final Timer timer;
    private final String name;

    /**
     * A  message with an expire. The item is removed from its container when timeout has run out.
     */
    protected class ExpiringMessage extends TimerTask {
        protected T message;
        protected String id;
        protected Instant expiresAt;

        /**
         * Constructor
         *
         * @param expiresAt Timestamp after which the message expires and will be removed from the
         *                  {@link ExpiringStore#storeMap}.
         * @param id        The unique id of the message
         * @param message   The original message
         * @throws StoreItemExpiredException When the expiresAt is already expired.
         */
        public ExpiringMessage(Instant expiresAt, String id, T message) throws StoreItemExpiredException {
            if (expiresAt.isBefore(Instant.now())) {
                throw new StoreItemExpiredException("Not adding " +
                        message.getClass().getSimpleName() + " " + id +
                        " because it has expired at " + expiresAt + ".");
            }

            this.id = id;
            this.message = message;
            this.expiresAt = expiresAt;
            timer.schedule(this, Date.from(expiresAt));
        }

        /**
         * The action to be performed by this timer task. Remove message from the ExpiringStore.
         */
        @Override
        public void run() {
            log.debug("Discard {} {} because it expired at {}.",
                    message.getClass().getSimpleName(),
                    id,
                    expiresAt);
            remove(id);
        }

        /**
         * Getter
         *
         * @return The message
         */
        public T getMessage() {
            return message;
        }

    }

    protected Map<String, ExpiringMessage> storeMap = new HashMap<>();

    /**
     * Constructor
     * <p>
     * Generate an automatic name.
     */
    public ExpiringStore() {
        this("Store-" + counter.incrementAndGet());
    }

    /**
     * Constructor
     *
     * @param name Name of the ExpiringStore.
     */
    public ExpiringStore(String name) {
        this.name = name;
        this.timer = new Timer("Timer for " + name);
    }

    /**
     * Add the message
     *
     * @param expiringMessage Message to add
     * @throws StoreItemExistsException When the expiringMessage already exists.
     */
    protected void addInternal(ExpiringMessage expiringMessage) throws StoreItemExistsException {
        ExpiringMessage existingMessage = storeMap.putIfAbsent(expiringMessage.id, expiringMessage);
        if (existingMessage != null) {
            throw new StoreItemExistsException("Not adding " + expiringMessage.message.getClass().getSimpleName() +
                    " " + expiringMessage.id + " because it already exists.");
        }
    }

    /**
     * Put the message, allowing to overwrite messages with the same id. Overwritten messages will be canceled.
     *
     * @param expiringMessage Message to put
     */
    protected void putInternal(ExpiringMessage expiringMessage) {
        ExpiringMessage existingMessage = storeMap.put(expiringMessage.id, expiringMessage);
        if (existingMessage != null)
            existingMessage.cancel();
    }

    /**
     * Add a message to the store if it does not exist
     *
     * @param expiresAt Timestamp after which the message expires
     * @param id        The unique id of the message
     * @param message   The message itself to store
     * @throws StoreItemExpiredException When the expiresAt is already expired.
     * @throws StoreItemExistsException  When the message already exists.
     */
    public synchronized void add(Instant expiresAt, String id, T message) throws StoreItemExpiredException, StoreItemExistsException {
        addInternal(new ExpiringMessage(expiresAt, id, message));
    }

    /**
     * Put a message to the store, possibly overwriting existing messages.
     *
     * @param expiresAt Timestamp after which the message expires
     * @param id        The unique id of the message
     * @param message   The message itself to store
     * @throws StoreItemExpiredException When the expiresAt is already expired.
     */
    public synchronized void put(Instant expiresAt, String id, T message) throws StoreItemExpiredException {
        putInternal(new ExpiringMessage(expiresAt, id, message));
    }

    /**
     * Remove a message from the storeMap and cancel its TimerTask.
     *
     * @param id Id of the message
     */
    public synchronized void remove(String id) {
        ExpiringMessage message = storeMap.get(id);
        if (message != null)
            message.cancel();
    }

    /**
     * Getter
     *
     * @param id Id of the message
     * @return The stored message
     */
    public synchronized T get(String id) {
        ExpiringMessage message = storeMap.get(id);
        return (message != null ? message.getMessage() : null);
    }

    /**
     * Getter
     *
     * @return The name of this store.
     */
    public String getName() {
        return name;
    }

    /**
     * Cancel the {@link #timer} and clear the {@link #storeMap}. This Store cannot be used thereafter.
     */
    @Override
    public synchronized void close() {
        timer.cancel();
        storeMap.clear();
    }

}
