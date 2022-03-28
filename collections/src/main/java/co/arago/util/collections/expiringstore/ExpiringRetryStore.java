package co.arago.util.collections.expiringstore;

import co.arago.util.collections.expiringstore.exceptions.StoreItemExistsException;
import co.arago.util.collections.expiringstore.exceptions.StoreItemExpiredException;
import co.arago.util.collections.expiringstore.messages.ExpiringRetryMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

/**
 * This store contains a counter for retriesLeft. If retriesLeft reaches zero, the message is automatically
 * discarded.
 *
 * @param <T> Type of items to store
 */
public class ExpiringRetryStore<T> extends AbstractExpiringStore<T, ExpiringRetryMessage<T>> {

    static final int DEFAULT_RETRIES = 4;
    private final static Logger log = LoggerFactory.getLogger(ExpiringRetryStore.class);

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
        this(maxRetries, "RetryStore-" + counter.incrementAndGet());
    }

    /**
     * Constructor
     *
     * @param maxRetries Maximum amount of retries.
     * @param name       Name of this store.
     */
    public ExpiringRetryStore(int maxRetries, String name) {
        super(name);
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
    public synchronized void add(
            Instant expiresAt,
            String id,
            T message) throws StoreItemExpiredException, StoreItemExistsException {
        addInternal(new ExpiringRetryMessage<>(this, expiresAt, id, message, retriesLeft));
    }

    /**
     * Add a message to the store if it does not exist. This message will also be removed when its "retriesLeft" is
     * exhausted via {@link #retryGet(String)}.
     *
     * @param expiresAt   Timestamp after which the message expires
     * @param id          The unique id of the message
     * @param message     The message itself to store
     * @param retriesLeft Explicitly set the retries for this message.
     * @throws StoreItemExpiredException When the expiresAt is already expired.
     * @throws StoreItemExistsException  When the message already exists.
     */
    public synchronized void add(
            Instant expiresAt,
            String id,
            T message,
            int retriesLeft) throws StoreItemExpiredException, StoreItemExistsException {
        addInternal(new ExpiringRetryMessage<>(this, expiresAt, id, message, retriesLeft));
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
    public synchronized void put(
            Instant expiresAt,
            String id,
            T message) throws StoreItemExpiredException {
        putInternal(new ExpiringRetryMessage<>(this, expiresAt, id, message, retriesLeft));
    }

    /**
     * Put a message to the store, possibly overwriting existing messages with the same id.
     * This message will also be removed when its "retriesLeft" is
     * exhausted via {@link #retryGet(String)}.
     *
     * @param expiresAt   Timestamp after which the message expires
     * @param id          The unique id of the message
     * @param message     The message itself to store
     * @param retriesLeft Explicitly set the retries for this message.
     * @throws StoreItemExpiredException When the expiresAt is already expired.
     */
    public synchronized void put(
            Instant expiresAt,
            String id,
            T message,
            int retriesLeft) throws StoreItemExpiredException {
        putInternal(new ExpiringRetryMessage<>(this, expiresAt, id, message, retriesLeft));
    }

    /**
     * Each call uses {@link ExpiringRetryMessage#getAndDecRetries()} for the message with this id.
     * When it reaches 0, the message is discarded from the {@link ExpiringRetryStore}.
     *
     * @param id Id of the message.
     * @return The message or null when message got discarded or no message with this id exists.
     */
    public synchronized T retryGet(String id) {
        ExpiringRetryMessage<T> existingRetryMessage = storeMap.get(id);
        if (existingRetryMessage == null)
            return null;

        if (existingRetryMessage.getAndDecRetries() <= 0) {
            log.debug("Discard message {} because no retries left.", id);
            remove(id);
            return null;
        }

        return existingRetryMessage.getMessage();
    }
}
