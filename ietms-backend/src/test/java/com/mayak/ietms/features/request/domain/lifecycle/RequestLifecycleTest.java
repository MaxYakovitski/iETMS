package com.mayak.ietms.features.request.domain.lifecycle;

import com.mayak.ietms.features.bid.domain.model.Bid;
import com.mayak.ietms.features.request.domain.enums.RequestStatus;
import com.mayak.ietms.features.request.domain.model.RefuseReason;
import com.mayak.ietms.features.request.domain.model.Request;
import com.mayak.ietms.features.request.domain.model.SpotRequest;
import com.mayak.ietms.features.user.domain.model.User;
import com.mayak.ietms.shared.exception.business.RequestStateException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

@DisplayName("RequestLifecycle — unit-тэсты")
class RequestLifecycleTest {

    private RequestLifecycle lifecycle;

    @BeforeEach
    void setUp() {
        lifecycle = new RequestLifecycle();
    }

    // ─────────────────────────────────────────────────────────────
    // Дапаможнікі
    // ─────────────────────────────────────────────────────────────

    /** Мінімальны SpotRequest без JPA-кантэксту */
    private SpotRequest spotRequest(RequestStatus status) {
        SpotRequest r = new SpotRequest();
        r.setStatus(status);
        return r;
    }

    private User userWithId(Long id) {
        User u = new User();
        u.setId(id);
        return u;
    }

    private Bid bidWithAmount(BigDecimal amount, User user) {
        Bid bid = new Bid();
        bid.setAmount(amount);
        bid.setUser(user);
        return bid;
    }

    private RefuseReason testReason(String code) {
        return () -> code;
    }

    // ─────────────────────────────────────────────────────────────
    // 1. Пераходы статусаў: NEW → BIDDING → OFFERED → ACCEPTED
    // ─────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Пераходы статусаў")
    class StatusTransition {

        @Test
        @DisplayName("NEW → BIDDING праз markBidding()")
        void newToBidding() {
            Request request = spotRequest(RequestStatus.NEW);
            lifecycle.markBidding(request);
            assertThat(request.getStatus()).isEqualTo(RequestStatus.BIDDING);
        }

        @Test
        @DisplayName("IN_PROGRESS → BIDDING праз markBidding()")
        void inProgressToBidding() {
            Request request = spotRequest(RequestStatus.IN_PROGRESS);
            lifecycle.markBidding(request);
            assertThat(request.getStatus()).isEqualTo(RequestStatus.BIDDING);
        }

        @Test
        @DisplayName("BIDDING → OFFERED праз offer()")
        void biddingToOffered() {
            Request request = spotRequest(RequestStatus.BIDDING);
            lifecycle.offer(request);
            assertThat(request.getStatus()).isEqualTo(RequestStatus.OFFERED);
        }

        @Test
        @DisplayName("OFFERED → ACCEPTED праз accept()")
        void offeredToAccepted() {
            SpotRequest request = spotRequest(RequestStatus.OFFERED);
            request.setClientPrice(new BigDecimal("1000.00"));
            User dispatcher = userWithId(42L);
            Bid bid = bidWithAmount(new BigDecimal("800.00"), dispatcher);

            lifecycle.accept(request, bid, new BigDecimal("1000.00"));

            assertThat(request.getStatus()).isEqualTo(RequestStatus.ACCEPTED);
            assertThat(request.getDispatcherId()).isEqualTo(42L);
        }

        @Test
        @DisplayName("accept() патрабуе статус OFFERED — інакш RequestStateException")
        void acceptRequiresOfferedStatus() {
            Request request = spotRequest(RequestStatus.BIDDING);
            User dispatcher = userWithId(1L);
            Bid bid = bidWithAmount(new BigDecimal("500.00"), dispatcher);

            assertThatThrownBy(() -> lifecycle.accept(request, bid, BigDecimal.TEN))
                    .isInstanceOf(RequestStateException.class);
        }

        @Test
        @DisplayName("Любы нефінальны статус → REFUSED праз refuse()")
        void anyStatusToRefused() {
            for (RequestStatus status : new RequestStatus[]{
                    RequestStatus.NEW, RequestStatus.IN_PROGRESS,
                    RequestStatus.BIDDING, RequestStatus.OFFERED}) {

                Request r = spotRequest(status);
                lifecycle.refuse(r, testReason("REASON_X"));
                assertThat(r.getStatus())
                        .as("Павінен быць REFUSED для ўваходнага " + status)
                        .isEqualTo(RequestStatus.REFUSED);
            }
        }
    }

    // ─────────────────────────────────────────────────────────────
    // 2. Забарона пераходаў з фінальных статусаў
    // ─────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Забарона аперацый на фінальных запытах")
    class FinalStatusGuards {

        @Test
        @DisplayName("markBidding() на ACCEPTED — павінен ігнараваць (isFinal guard)")
        void markBiddingIgnoresFinal() {
            Request request = spotRequest(RequestStatus.ACCEPTED);
            lifecycle.markBidding(request);
            // статус не змяніўся
            assertThat(request.getStatus()).isEqualTo(RequestStatus.ACCEPTED);
        }

        @Test
        @DisplayName("offer() на ACCEPTED — кідае IllegalStateException")
        void offerOnAcceptedThrows() {
            Request request = spotRequest(RequestStatus.ACCEPTED);
            assertThatThrownBy(() -> lifecycle.offer(request))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("offer() на REFUSED — кідае IllegalStateException")
        void offerOnRefusedThrows() {
            Request request = spotRequest(RequestStatus.REFUSED);
            assertThatThrownBy(() -> lifecycle.offer(request))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("refuse() на ACCEPTED — кідае IllegalStateException")
        void refuseOnAcceptedThrows() {
            Request request = spotRequest(RequestStatus.ACCEPTED);
            assertThatThrownBy(() -> lifecycle.refuse(request, testReason("X")))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("archive() на нефінальным статусе — кідае IllegalStateException")
        void archiveNonFinalThrows() {
            Request request = spotRequest(RequestStatus.BIDDING);
            assertThatThrownBy(() -> lifecycle.archive(request))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("archive() на ўжо заархіваваным — ігнаруе паўторны выклік")
        void archiveAlreadyArchivedIsIdempotent() {
            Request request = spotRequest(RequestStatus.ACCEPTED);
            request.setArchived(true);
            // не кідае выключэнне
            assertThatCode(() -> lifecycle.archive(request))
                    .doesNotThrowAnyException();
        }
    }

    // ─────────────────────────────────────────────────────────────
    // 3. Разлік profitMargin пры accept()
    // ─────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Разлік profitMargin пры accept()")
    class ProfitMarginCalculation {

        @Test
        @DisplayName("profitMargin = clientPrice − bidAmount (станоўчая маржа)")
        void positiveMargin() {
            SpotRequest request = spotRequest(RequestStatus.OFFERED);
            User dispatcher = userWithId(1L);
            Bid bid = bidWithAmount(new BigDecimal("700.00"), dispatcher);

            lifecycle.accept(request, bid, new BigDecimal("1000.00"));

            assertThat(request.getProfitMargin())
                    .isEqualByComparingTo(new BigDecimal("300.00"));
        }

        @Test
        @DisplayName("profitMargin = 0 калі clientPrice роўны bidAmount")
        void zeroMargin() {
            SpotRequest request = spotRequest(RequestStatus.OFFERED);
            User dispatcher = userWithId(1L);
            Bid bid = bidWithAmount(new BigDecimal("500.00"), dispatcher);

            lifecycle.accept(request, bid, new BigDecimal("500.00"));

            assertThat(request.getProfitMargin())
                    .isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("profitMargin = 0 калі clientPrice не перададзены (null)")
        void nullClientPriceGivesZeroMargin() {
            SpotRequest request = spotRequest(RequestStatus.OFFERED);
            User dispatcher = userWithId(1L);
            Bid bid = bidWithAmount(new BigDecimal("500.00"), dispatcher);

            lifecycle.accept(request, bid, null);

            assertThat(request.getProfitMargin())
                    .isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("bidPrice захоўвае суму стаўкі пасля accept()")
        void bidPriceStoredCorrectly() {
            SpotRequest request = spotRequest(RequestStatus.OFFERED);
            User dispatcher = userWithId(1L);
            BigDecimal bidAmount = new BigDecimal("350.50");
            Bid bid = bidWithAmount(bidAmount, dispatcher);

            lifecycle.accept(request, bid, new BigDecimal("600.00"));

            assertThat(request.getBidPrice()).isEqualByComparingTo(bidAmount);
        }

        @Test
        @DisplayName("dispatcherId усталёўваецца з bid.user.id пасля accept()")
        void dispatcherIdSetFromBidUser() {
            SpotRequest request = spotRequest(RequestStatus.OFFERED);
            User dispatcher = userWithId(99L);
            Bid bid = bidWithAmount(new BigDecimal("100.00"), dispatcher);

            lifecycle.accept(request, bid, new BigDecimal("200.00"));

            assertThat(request.getDispatcherId()).isEqualTo(99L);
        }
    }

    // ─────────────────────────────────────────────────────────────
    // 4. recalculateStatus()
    // ─────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("recalculateStatus() — усе камбінацыі")
    class RecalculateStatus {

        @Test
        @DisplayName("hasBids=true → статус BIDDING незалежна ад hasCompetitors")
        void hasBidsAlwaysBidding() {
            Request r1 = spotRequest(RequestStatus.NEW);
            lifecycle.recalculateStatus(r1, true, true);
            assertThat(r1.getStatus()).isEqualTo(RequestStatus.BIDDING);

            Request r2 = spotRequest(RequestStatus.IN_PROGRESS);
            lifecycle.recalculateStatus(r2, true, false);
            assertThat(r2.getStatus()).isEqualTo(RequestStatus.BIDDING);
        }

        @Test
        @DisplayName("hasBids=false, hasCompetitors=true → статус IN_PROGRESS")
        void noBidsWithCompetitorsIsInProgress() {
            Request request = spotRequest(RequestStatus.NEW);
            lifecycle.recalculateStatus(request, false, true);
            assertThat(request.getStatus()).isEqualTo(RequestStatus.IN_PROGRESS);
        }

        @Test
        @DisplayName("hasBids=false, hasCompetitors=false → статус NEW")
        void noBidsNoCompetitorsIsNew() {
            Request request = spotRequest(RequestStatus.IN_PROGRESS);
            lifecycle.recalculateStatus(request, false, false);
            assertThat(request.getStatus()).isEqualTo(RequestStatus.NEW);
        }

        @Test
        @DisplayName("recalculateStatus() ігнаруе фінальны запыт")
        void finalRequestNotRecalculated() {
            Request request = spotRequest(RequestStatus.ACCEPTED);
            lifecycle.recalculateStatus(request, false, false);
            // ACCEPTED не змяніўся
            assertThat(request.getStatus()).isEqualTo(RequestStatus.ACCEPTED);
        }

        @Test
        @DisplayName("recalculateStatus() ігнаруе REFUSED")
        void refusedNotRecalculated() {
            Request request = spotRequest(RequestStatus.REFUSED);
            lifecycle.recalculateStatus(request, true, true);
            assertThat(request.getStatus()).isEqualTo(RequestStatus.REFUSED);
        }
    }

}