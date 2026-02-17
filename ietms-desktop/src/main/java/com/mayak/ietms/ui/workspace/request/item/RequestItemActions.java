package com.mayak.ietms.ui.workspace.request.item;

import com.mayak.ietms.integration.api.RequestClient;
import lombok.RequiredArgsConstructor;
import java.math.BigDecimal;

@RequiredArgsConstructor
public class RequestItemActions {

    private final RequestClient requestClient;
    private final Runnable onChanged;

    public void join(Long requestId) {
        requestClient.join(requestId);
        onChanged.run();
    }

    public void leave(Long requestId) {
        requestClient.leave(requestId);
        onChanged.run();
    }

    public void offer(Long requestId) {
        requestClient.offer(requestId);
        onChanged.run();
    }

    public void accept(Long requestId) {
        requestClient.accept(requestId);
        onChanged.run();
    }

    public void acceptWithPrice(Long requestId, BigDecimal price) {
        requestClient.accept(requestId, price);
        onChanged.run();
    }

    public void refuse(Long requestId, String reasonCode) {
        requestClient.refuse(requestId, reasonCode);
        onChanged.run();
    }

    public void delete(Long requestId) {
        requestClient.delete(requestId);
        onChanged.run();
    }

    public String getExchangeString(Long requestId) {
        return requestClient.getExchangeString(requestId);
    }
}