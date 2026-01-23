package com.mayak.iet.features.request.domain.lifecycle;

import com.mayak.iet.features.bid.domain.model.Bid;
import com.mayak.iet.features.lane.domain.model.Lane;
import com.mayak.iet.features.request.domain.enums.RequestStatus;
import com.mayak.iet.features.request.domain.model.ContractRequest;
import com.mayak.iet.features.request.domain.model.RefuseReason;
import com.mayak.iet.features.request.domain.model.Request;
import com.mayak.iet.features.request.domain.model.SpotRequest;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class RequestLifecycle {

    /* ================= CREATE / INIT ================= */

    public void assignLane(ContractRequest request, Lane lane) {
        if (request.getStatus().isFinal()) {
            throw new IllegalStateException("Cannot assign lane to final request");
        }
        request.assignLane(lane);
    }

    /* ================= STATUS TRANSITIONS ================= */

    public void markBidding(Request request) {
        if (request.getStatus().isFinal()) return;
        request.setStatus(RequestStatus.BIDDING);
    }

    public void offer(Request request) {
        if (request.getStatus().isFinal()) {
            throw new IllegalStateException("Cannot offer final request");
        }
        request.setStatus(RequestStatus.OFFERED);
    }

    public void accept(Request request, Bid bestBid, BigDecimal clientPrice) {
        if (request.getStatus() != RequestStatus.OFFERED) {
            throw new IllegalStateException("Request must be OFFERED before acceptance");
        }

        request.setAssignedUserId(bestBid.getUser().getId());
        request.setStatus(RequestStatus.ACCEPTED);

        if (request instanceof SpotRequest spot) {
            spot.setClientPrice(clientPrice);
        }

        applyMargin(request, bestBid);
    }

    public void refuse(Request request, RefuseReason reason) {
        if (request.getStatus().isFinal()) {
            throw new IllegalStateException("Cannot refuse final request");
        }
        request.setStatus(RequestStatus.REFUSED);
        request.setReason(reason);
    }

    /* ================= STATUS RECALCULATION ================= */

    public void recalculateStatus(Request request, boolean hasBids, boolean hasCompetitors) {
        if (request.getStatus().isFinal()) return;

        RequestStatus newStatus;
        if (hasBids) {
            newStatus = RequestStatus.BIDDING;
        } else if (hasCompetitors) {
            newStatus = RequestStatus.IN_PROGRESS;
        } else {
            newStatus = RequestStatus.NEW;
        }

        request.setStatus(newStatus);
    }

    /* ================= ARCHIVATION ================= */
    public void archive(Request request) {
        if (request.isArchived()) {
            return;
        }

        if (!request.getStatus().isFinal()) {
            throw new IllegalStateException(
                    "Only final requests can be archived, status=" + request.getStatus()
            );
        }

        request.setArchived(true);
    }

    /* ================= INTERNAL ================= */

    private void applyMargin(Request request, Bid bestBid) {
        request.setBidPrice(bestBid.getAmount());

        if (request.getClientPrice() != null) {
            request.setProfitMargin(
                    request.getClientPrice().subtract(bestBid.getAmount())
            );
        } else {
            request.setProfitMargin(BigDecimal.ZERO);
        }
    }
}
