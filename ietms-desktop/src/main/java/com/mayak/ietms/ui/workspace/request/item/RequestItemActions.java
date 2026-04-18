package com.mayak.ietms.ui.workspace.request.item;

import com.mayak.ietms.integration.api.RequestClient;
import lombok.RequiredArgsConstructor;
import java.math.BigDecimal;

/**
 * Encapsulates request lifecycle actions available from a list item.
 * Each mutating action delegates to {@link RequestClient} and notifies
 * the parent via {@code onChanged} callback upon completion.
 */
@RequiredArgsConstructor
public class RequestItemActions {

    private final RequestClient requestClient;
    private final Runnable onChanged;

    /** Joins the current user as a competitor on the request. */
    public void join(Long requestId) {
        requestClient.join(requestId);
        onChanged.run();
    }

    /** Removes the current user from the request competitors. */
    public void leave(Long requestId) {
        requestClient.leave(requestId);
        onChanged.run();
    }

    /** Moves the request to {@code OFFERED} status. */
    public void offer(Long requestId) {
        requestClient.offer(requestId);
        onChanged.run();
    }

    /** Accepts the request without a final price. */
    public void accept(Long requestId) {
        requestClient.accept(requestId);
        onChanged.run();
    }

    /** Accepts the request with a final price. */
    public void acceptWithPrice(Long requestId, BigDecimal price) {
        requestClient.accept(requestId, price);
        onChanged.run();
    }

    /** Refuses the request with the given reason code. */
    public void refuse(Long requestId, String reasonCode) {
        requestClient.refuse(requestId, reasonCode);
        onChanged.run();
    }

    /** Permanently deletes the request. */
    public void delete(Long requestId) {
        requestClient.delete(requestId);
        onChanged.run();
    }

    /**
     * Manually expires the request by refusing it with reason
     * {@code BID_NOT_PROVIDED}, replicating the scheduled expiry behaviour.
     */
    public void expire(Long requestId) {
        requestClient.expire(requestId);
        onChanged.run();
    }

    /** Returns the exchange string for clipboard export. */
    public String getExchangeString(Long requestId) {
        return requestClient.getExchangeString(requestId);
    }
}