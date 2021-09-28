package co.arago.util.collections.expiringstore;

import co.arago.util.collections.expiringstore.exceptions.StoreItemExistsException;
import co.arago.util.collections.expiringstore.exceptions.StoreItemExpiredException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

/**
 * This store contains a counter for retriesLeft. If retriesLeft reaches zero, the message is automatically
 * discarded.
 *
 * @param <T> Type of items to store
 */
public class ExpiringRetryStore<T> extends ExpiringStore<T> {

    static final int DEFAULT_RETRIES = 4;
    private final static Logger log = LoggerFactory.getLogger(ExpiringRetryStore.class);

    /**
     * A message with an expire and retry. The item is removed from its container when timeout has run out.
     */
    protected static class ExpiringRetryMessage<T> extends ExpiringMessage<T> {
        protected int retriesLeft;

        /**
         * Constructor
         *
         * @param parent     Reference to the ExpiringRetryStore.
         * @param expiresAt  Timestamp after which the message expires and will be removed from the
         *                   {@link ExpiringStore#storeMap}.
         * @param id         The unique id of the message
         * @param message    The original message
         * @param maxRetries Max retries (default is 4)
         * @throws StoreItemExpiredException When the expiresAt is already expired.
         */
        ExpiringRetryMessage(ExpiringRetryStore<T> parent, Instant expiresAt, String id, T message, int maxRetries)
                throws StoreItemExpiredException {
            super(parent, expiresAt, id, message);
            this.retriesLeft = maxRetries;
        }
    }

    protected int retriesLeft;

    /**
     * Constructor
     * <p>
     * Default maxRetries is {@link #DEFAULT_RETRIES}.
     */
    public ExpiringRetryStore() {
        this(DEFAULT_RETRIES);
    }

    /**
     * Constructor
     *
     * @param maxRetries Maximum amount of retries.
     */
    public ExpiringRetryStore(int maxRetries) {
        super("RetryStore-" + counter.incrementAndGet());
        this.retriesLeft = maxRetries;
    }

    /**
     * Add a message to the store if it does not exist. This message will also be removed when its "retriesLeft" is
     * exhausted via {@link #retryGet(String)}.
     *
     * @param expiresAt Timestamp after which the message expires
     * @param id        The unique id of the message
     * @param message   The message itself to store
     * @throws StoreItemExpiredException When the expiresAt is already expired.
     * @throws StoreItemExistsException  When the message already exists.
     */
    public synchronized void add(Instant expiresAt, String id, T message)
            throws StoreItemExpiredException, StoreItemExistsException {
        addInternal(new ExpiringRetryMessage<T>(this, expiresAt, id, message, retriesLeft));
    }

    /**
     * Put a message to the store, possibly overwriting existing messages with the same id.
     * This message will also be removed when its "retriesLeft" is
     * exhausted via {@link #retryGet(String)}.
     *
     * @param expiresAt Timestamp after which the message expires
     * @param id        The unique id of the message
     * @param message   The message itself to store
     * @throws StoreItemExpiredException When the expiresAt is already expired.
     */
    public synchronized void put(Instant expiresAt, String id, T message) throws StoreItemExpiredException {
        putInternal(new ExpiringRetryMessage<T>(this, expiresAt, id, message, retriesLeft));
    }

    /**
     * Each call decreases {@link ExpiringRetryMessage#retriesLeft} for the message with this id.
     * When it reaches 0, the message is discarded from the {@link ExpiringRetryStore}.
     *
     * @param id Id of the message.
     * @return The message or null when message got discarded or no message with this id exists.
     */
    public synchronized T retryGet(String id) {
        ExpiringRetryMessage<T> existingRetryMessage = (ExpiringRetryMessage<T>) storeMap.get(id);
        if (existingRetryMessage == null)
            return null;

        if (existingRetryMessage.retriesLeft <= 0) {
            log.debug("Discard message {} because no retries left.", id);
            remove(id);
            return null;
        }

        existingRetryMessage.retriesLeft--;

        return existingRetryMessage.getMessage();
    }
}
