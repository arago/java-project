package co.arago.util.collections.expiringstore.messages;

import co.arago.util.collections.expiringstore.AbstractExpiringStore;
import co.arago.util.collections.expiringstore.exceptions.StoreItemExpiredException;

import java.time.Instant;

/**
 * A message with an expire and retry. The item is removed from its container when timeout has run out.
 */
public class ExpiringRetryMessage<T> extends ExpiringMessage<T> {
    protected int retriesLeft;

    /**
     * Constructor
     *
     * @param parent     Reference to the ExpiringRetryStore.
     * @param expiresAt  Timestamp after which the message expires and will be removed from the
     *                   ExpiringStore#storeMap.
     * @param id         The unique id of the message
     * @param message    The original message
     * @param maxRetries Max retries (default is 4)
     * @throws StoreItemExpiredException When the expiresAt is already expired.
     */
    public ExpiringRetryMessage(AbstractExpiringStore<?, ?> parent, Instant expiresAt, String id, T message, int maxRetries)
            throws StoreItemExpiredException {
        super(parent, expiresAt, id, message);
        this.retriesLeft = maxRetries;
    }

    public synchronized int getAndDecRetries() {
        return this.retriesLeft--;
    }
}
