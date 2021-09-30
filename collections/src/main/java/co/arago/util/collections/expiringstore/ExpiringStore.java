package co.arago.util.collections.expiringstore;

import co.arago.util.collections.expiringstore.exceptions.StoreItemExistsException;
import co.arago.util.collections.expiringstore.exceptions.StoreItemExpiredException;
import co.arago.util.collections.expiringstore.messages.ExpiringMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

/**
 * Contains the store items with an "expiresAt" after which the items are automatically removed from the store.
 *
 * @param <T> Type of items to store
 */
public class ExpiringStore<T> extends AbstractExpiringStore<T, ExpiringMessage<T>> {

    private final static Logger log = LoggerFactory.getLogger(ExpiringStore.class);

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
        super(name);
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
    public synchronized void add(
            Instant expiresAt,
            String id,
            T message
    ) throws StoreItemExpiredException, StoreItemExistsException {
        addInternal(new ExpiringMessage<T>(this, expiresAt, id, message));
    }

    /**
     * Put a message to the store, possibly overwriting existing messages.
     *
     * @param expiresAt Timestamp after which the message expires
     * @param id        The unique id of the message
     * @param message   The message itself to store
     * @throws StoreItemExpiredException When the expiresAt is already expired.
     */
    public synchronized void put(
            Instant expiresAt,
            String id,
            T message
    ) throws StoreItemExpiredException {
        putInternal(new ExpiringMessage<T>(this, expiresAt, id, message));
    }

}
