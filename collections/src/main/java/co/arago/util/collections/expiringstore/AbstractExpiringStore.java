package co.arago.util.collections.expiringstore;

import co.arago.util.collections.expiringstore.exceptions.StoreItemExistsException;
import co.arago.util.collections.expiringstore.exceptions.StoreItemExpiredException;
import co.arago.util.collections.expiringstore.messages.ExpiringMessage;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Abstract root class for expiring stores.
 * <p>
 * Contains the store items with an "expiresAt" after which the items are automatically removed from the store.
 *
 * @param <T> Type of items to store
 * @param <M> Type of ExpiringMessages to use.
 */
public abstract class AbstractExpiringStore<T, M extends ExpiringMessage<T>> implements AutoCloseable {

    protected static final AtomicInteger counter = new AtomicInteger(0);

    private final Timer timer;
    private final String name;

    protected final Map<String, M> storeMap = new HashMap<>();

    /**
     * Constructor
     *
     * @param name Name of the ExpiringStore.
     */
    public AbstractExpiringStore(String name) {
        this.name = name;
        this.timer = new Timer("Timer for " + name);
    }

    /**
     * Add the message
     *
     * @param expiringMessage Message to add
     * @throws StoreItemExistsException When the expiringMessage already exists.
     */
    protected void addInternal(M expiringMessage) throws StoreItemExistsException {
        M existingMessage = storeMap.putIfAbsent(expiringMessage.getId(), expiringMessage);
        if (existingMessage != null) {
            throw new StoreItemExistsException("Not adding " + expiringMessage.getMessage().getClass().getSimpleName() +
                    " " + expiringMessage.getId() + " because it already exists.");
        }
    }

    /**
     * Put the message, allowing to overwrite messages with the same id. Overwritten messages will be canceled.
     *
     * @param expiringMessage Message to put
     */
    protected void putInternal(M expiringMessage) {
        M existingMessage = storeMap.put(expiringMessage.getId(), expiringMessage);
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
    public abstract void add(Instant expiresAt, String id, T message) throws StoreItemExpiredException, StoreItemExistsException;

    /**
     * Put a message to the store, possibly overwriting existing messages.
     *
     * @param expiresAt Timestamp after which the message expires
     * @param id        The unique id of the message
     * @param message   The message itself to store
     * @throws StoreItemExpiredException When the expiresAt is already expired.
     */
    public abstract void put(Instant expiresAt, String id, T message) throws StoreItemExpiredException;

    /**
     * Remove a message from the storeMap and cancel its TimerTask.
     *
     * @param id Id of the message
     */
    public synchronized void remove(String id) {
        M message = storeMap.remove(id);
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
        M message = storeMap.get(id);
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
     * Schedule an expiry with the {@link #timer},
     *
     * @param timerTask The timerTask to schedule.
     * @param date      The timestamp when the timerTask will be called.
     */
    public void schedule(TimerTask timerTask, Date date) {
        timer.schedule(timerTask, date);
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
