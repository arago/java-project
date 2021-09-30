package co.arago.util.collections.expiringstore.messages;

import co.arago.util.collections.expiringstore.AbstractExpiringStore;
import co.arago.util.collections.expiringstore.exceptions.StoreItemExpiredException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Date;
import java.util.TimerTask;

/**
 * A message with an expire. The item is removed from its container when timeout has run out.
 */
public class ExpiringMessage<T> extends TimerTask {

    private final static Logger log = LoggerFactory.getLogger(ExpiringMessage.class);

    protected final AbstractExpiringStore<?, ?> parent;
    protected final T message;
    protected final String id;
    protected final Instant expiresAt;

    /**
     * Constructor
     *
     * @param parent    Reference to the ExpiringStore.
     * @param expiresAt Timestamp after which the message expires and will be removed from the
     *                  ExpiringStore#storeMap.
     * @param id        The unique id of the message
     * @param message   The original message
     * @throws StoreItemExpiredException When the expiresAt is already expired.
     */
    public ExpiringMessage(AbstractExpiringStore<?, ?> parent, Instant expiresAt, String id, T message)
            throws StoreItemExpiredException {
        this.parent = parent;
        if (expiresAt.isBefore(Instant.now())) {
            throw new StoreItemExpiredException("Not adding " +
                    message.getClass().getSimpleName() + " " + id +
                    " because it has expired at " + expiresAt + ".");
        }

        this.id = id;
        this.message = message;
        this.expiresAt = expiresAt;
        this.parent.schedule(this, Date.from(expiresAt));
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
        parent.remove(id);
    }

    /**
     * Getter
     *
     * @return The message
     */
    public T getMessage() {
        return message;
    }

    public String getId() {
        return id;
    }
}
