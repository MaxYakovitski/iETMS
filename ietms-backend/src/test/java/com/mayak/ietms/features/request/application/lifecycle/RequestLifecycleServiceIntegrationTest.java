package com.mayak.ietms.features.request.application.lifecycle;

import com.mayak.ietms.AbstractIntegrationTest;
import com.mayak.ietms.features.bid.application.BidCommandService;
import com.mayak.ietms.features.request.domain.enums.RequestStatus;
import com.mayak.ietms.features.request.domain.enums.ShipmentType;
import com.mayak.ietms.features.request.domain.enums.TransportType;
import com.mayak.ietms.features.request.domain.model.Request;
import com.mayak.ietms.features.request.domain.model.SpotRequest;
import com.mayak.ietms.features.request.infra.persistence.RequestRepository;
import com.mayak.ietms.features.shipment.infra.persistence.ShipmentRepository;
import com.mayak.ietms.features.user.domain.enums.Role;
import com.mayak.ietms.features.user.domain.enums.UserType;
import com.mayak.ietms.features.user.domain.model.Profile;
import com.mayak.ietms.features.user.domain.model.User;
import com.mayak.ietms.features.user.infra.persistence.UserRepository;
import com.mayak.ietms.request.dto.bid.BidCreateDto;
import com.mayak.ietms.shared.exception.business.RequestDeletionNotAllowedException;
import com.mayak.ietms.shared.exception.business.RequestStateException;
import com.mayak.ietms.shared.exception.business.UnauthorizedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@Transactional
@DisplayName("RequestLifecycleService + BidCommandService — інтэграцыйныя тэсты")
class RequestLifecycleServiceIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private RequestLifecycleService lifecycleService;

    @Autowired
    private BidCommandService bidCommandService;

    @Autowired
    private RequestRepository requestRepository;

    @Autowired
    private ShipmentRepository shipmentRepository;

    @Autowired
    private UserRepository userRepository;

    // ─────────────────────────────────────────────────────────────
    // Test data
    // ─────────────────────────────────────────────────────────────

    private User authorUser;
    private User transportUser;
    private User secondTransportUser;

    @BeforeEach
    void setUp() {
        authorUser = savedUser("Аўтар", "author@test.com", UserType.EMPLOYEE, Role.CLIENT_SPECIALIST);
        transportUser = savedUser("Дыспетчар", "dispatcher@test.com", UserType.EMPLOYEE, Role.TRANSPORT_SPECIALIST);
        secondTransportUser = savedUser("Дыспетчар2", "dispatcher2@test.com", UserType.EMPLOYEE, Role.TRANSPORT_SPECIALIST);
    }

    // ─────────────────────────────────────────────────────────────
    // 1. Cannot accept request without bids
    // ─────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("accept() — прыняцце заявак")
    class AcceptRequest {

        @Test
        @DisplayName("Немагчыма прыняць заяўку без стаўкі — RequestStateException")
        void cannotAcceptWithoutBids() {
            Request request = savedRequest(RequestStatus.OFFERED, authorUser);
            assertThatThrownBy(
                    () -> lifecycleService.accept(request.getId(), new BigDecimal("1000.00"), authorUser.getId()))
                    .isInstanceOf(RequestStateException.class);
        }

        @Test
        @DisplayName("accept() з існуючай стаўкай усталёўвае статус ACCEPTED")
        void acceptWithBidSetsAccepted() {
            Request request = savedRequest(RequestStatus.NEW, authorUser);
            lifecycleService.join(request.getId(), transportUser.getId());
            bidCommandService.create(new BidCreateDto(request.getId(),  new BigDecimal("800.00"), null),transportUser.getId());
            lifecycleService.offer(request.getId(), authorUser.getId());
            lifecycleService.accept(request.getId(), new BigDecimal("1000.00"), authorUser.getId());
            Request updated = requestRepository.findById(request.getId()).orElseThrow();
            assertThat(updated.getStatus()).isEqualTo(RequestStatus.ACCEPTED);
        }

        @Test
        @DisplayName("accept() стварае Shipment калі яго яшчэ няма")
        void acceptCreatesShipment() {
            Request request = savedRequest(RequestStatus.NEW, authorUser);
            lifecycleService.join(request.getId(), transportUser.getId());
            bidCommandService.create(new BidCreateDto(request.getId(), new BigDecimal("500.00"), null),transportUser.getId());
            lifecycleService.offer(request.getId(), authorUser.getId());
            lifecycleService.accept(request.getId(), new BigDecimal("700.00"), authorUser.getId());
            assertThat(shipmentRepository.existsByRequestId(request.getId())).isTrue();
        }

        @Test
        @DisplayName("accept() выбірае мінімальную стаўку як найлепшую")
        void acceptSelectsLowestBid() {
            Request request = savedRequest(RequestStatus.NEW, authorUser);
            lifecycleService.join(request.getId(), transportUser.getId());
            lifecycleService.join(request.getId(), secondTransportUser.getId());
            bidCommandService.create(new BidCreateDto(request.getId(), new BigDecimal("900.00"), null),transportUser.getId());
            bidCommandService.create(new BidCreateDto(request.getId(), new BigDecimal("600.00"), null), secondTransportUser.getId());
            lifecycleService.offer(request.getId(), authorUser.getId());
            lifecycleService.accept(request.getId(), new BigDecimal("1000.00"), authorUser.getId());
            Request updated = requestRepository.findById(request.getId()).orElseThrow();
            // Best bid is 600, dispatcher is second TransportUser
            assertThat(updated.getDispatcherId()).isEqualTo(secondTransportUser.getId());
            assertThat(updated.getBidPrice()).isEqualByComparingTo(new BigDecimal("600.00"));
            assertThat(updated.getProfitMargin()).isEqualByComparingTo(new BigDecimal("400.00"));
        }
    }

    // ─────────────────────────────────────────────────────────────
    // 2. Cannot delete request with shipment
    // ─────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("delete() — выдаленне заявак")
    class DeleteRequest {

        @Test
        @DisplayName("Немагчыма выдаліць заяўку з існуючай адпраўкай")
        void cannotDeleteRequestWithShipment() {
            Request request = savedRequest(RequestStatus.NEW, authorUser);
            lifecycleService.join(request.getId(), transportUser.getId());
            bidCommandService.create(new BidCreateDto(request.getId(), new BigDecimal("500.00"), null), transportUser.getId());
            lifecycleService.offer(request.getId(), authorUser.getId());
            lifecycleService.accept(request.getId(), new BigDecimal("700.00"), authorUser.getId());
            // Shipment now exists
            assertThat(shipmentRepository.existsByRequestId(request.getId())).isTrue();
            assertThatThrownBy(() -> lifecycleService.delete(request.getId(), authorUser.getId()))
                    .isInstanceOf(RequestDeletionNotAllowedException.class);
        }

        @Test
        @DisplayName("delete() без адпраўкі — паспяхова выдаляе")
        void deleteWithoutShipmentSucceeds() {
            Request request = savedRequest(RequestStatus.NEW, authorUser);
            lifecycleService.delete(request.getId(), authorUser.getId());
            assertThat(requestRepository.findById(request.getId())).isEmpty();
        }

        @Test
        @DisplayName("Не-аўтар не можа выдаліць заяўку — UnauthorizedException")
        void nonAuthorCannotDelete() {
            Request request = savedRequest(RequestStatus.NEW, authorUser);
            assertThatThrownBy(() -> lifecycleService.delete(request.getId(), transportUser.getId()))
                    .isInstanceOf(UnauthorizedException.class);
        }
    }

    // ─────────────────────────────────────────────────────────────
    // 3. join() and leave() update status correctly
    // ─────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("join() / leave() — кіраванне ўдзельнікамі")
    class JoinLeave {

        @Test
        @DisplayName("join() мяняе статус NEW → IN_PROGRESS")
        void joinChangesStatusToInProgress() {
            Request request = savedRequest(RequestStatus.NEW, authorUser);
            lifecycleService.join(request.getId(), transportUser.getId());
            Request updated = requestRepository.findById(request.getId()).orElseThrow();
            assertThat(updated.getStatus()).isEqualTo(RequestStatus.IN_PROGRESS);
            assertThat(updated.getCompetitorsId()).contains(transportUser.getId());
        }

        @Test
        @DisplayName("leave() апошняга ўдзельніка мяняе статус IN_PROGRESS → NEW")
        void leaveLastCompetitorChangesStatusToNew() {
            Request request = savedRequest(RequestStatus.NEW, authorUser);
            lifecycleService.join(request.getId(), transportUser.getId());
            lifecycleService.leave(request.getId(), transportUser.getId());
            Request updated = requestRepository.findById(request.getId()).orElseThrow();
            assertThat(updated.getStatus()).isEqualTo(RequestStatus.NEW);
            assertThat(updated.getCompetitorsId()).doesNotContain(transportUser.getId());
        }

        @Test
        @DisplayName("leave() з двума ўдзельнікамі — статус застаецца IN_PROGRESS")
        void leaveOneOfTwoKeepsInProgress() {
            Request request = savedRequest(RequestStatus.NEW, authorUser);
            lifecycleService.join(request.getId(), transportUser.getId());
            lifecycleService.join(request.getId(), secondTransportUser.getId());
            lifecycleService.leave(request.getId(), transportUser.getId());
            Request updated = requestRepository.findById(request.getId()).orElseThrow();
            assertThat(updated.getStatus()).isEqualTo(RequestStatus.IN_PROGRESS);
        }

        @Test
        @DisplayName("Аўтар не можа далучыцца да ўласнай заяўкі — UnauthorizedException")
        void authorCannotJoinOwnRequest() {
            Request request = savedRequest(RequestStatus.NEW, authorUser);
            assertThatThrownBy(() -> lifecycleService.join(request.getId(), authorUser.getId()))
                    .isInstanceOf(UnauthorizedException.class);
        }
    }

    // ─────────────────────────────────────────────────────────────
    // 4. Duplicate join does not create two records
    // ─────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Ідэмпатэнтнасць join() і leave()")
    class JoinIdempotency {

        @Test
        @DisplayName("Паўторны join() кідае UnauthorizedException")
        void doubleJoinThrowsException() {
            Request request = savedRequest(RequestStatus.NEW, authorUser);
            lifecycleService.join(request.getId(), transportUser.getId());
            assertThatThrownBy(() -> lifecycleService.join(request.getId(), transportUser.getId())).isInstanceOf(UnauthorizedException.class)
                    .hasMessageContaining("already joined");
            // Competitor count is still one
            Request updated = requestRepository.findById(request.getId()).orElseThrow();
            assertThat(updated.getCompetitorsId()).hasSize(1);
        }

        @Test
        @DisplayName("Паўторны leave() не кідае памылку")
        void doubleLeaveIsIdempotent() {
            Request request = savedRequest(RequestStatus.NEW, authorUser);
            lifecycleService.join(request.getId(), transportUser.getId());
            lifecycleService.leave(request.getId(), transportUser.getId());
            // Repeated leave() must not throw Exception
            assertThatCode(() -> lifecycleService.leave(request.getId(), transportUser.getId()))
                    .doesNotThrowAnyException();
        }
    }

    // ─────────────────────────────────────────────────────────────
    // 5. BidCommandService — bids
    // ─────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("BidCommandService — стварэнне і выдаленне ставак")
    class BidTests {

        @Test
        @DisplayName("Стварэнне стаўкі мяняе статус → BIDDING")
        void createBidChangesStatusToBidding() {
            Request request = savedRequest(RequestStatus.IN_PROGRESS, authorUser);
            lifecycleService.join(request.getId(), transportUser.getId());
            bidCommandService.create(new BidCreateDto(request.getId(), new BigDecimal("500.00"), "тэст"), transportUser.getId());
            Request updated = requestRepository.findById(request.getId()).orElseThrow();
            assertThat(updated.getStatus()).isEqualTo(RequestStatus.BIDDING);
        }

        @Test
        @DisplayName("Выдаленне адзінай стаўкі вяртае статус → IN_PROGRESS")
        void deleteBidReturnsToInProgress() {
            Request request = savedRequest(RequestStatus.NEW, authorUser);
            lifecycleService.join(request.getId(), transportUser.getId());
            var bid = bidCommandService.create(
                    new BidCreateDto(request.getId(), new BigDecimal("500.00"), null), transportUser.getId());
            bidCommandService.delete(bid.id(), transportUser.getId());
            Request updated = requestRepository.findById(request.getId()).orElseThrow();
            assertThat(updated.getStatus()).isEqualTo(RequestStatus.IN_PROGRESS);
        }

        @Test
        @DisplayName("Не ўласнік не можа выдаліць стаўку — UnauthorizedException")
        void nonOwnerCannotDeleteBid() {
            Request request = savedRequest(RequestStatus.NEW, authorUser);
            lifecycleService.join(request.getId(), transportUser.getId());
            var bid = bidCommandService.create(
                    new BidCreateDto(request.getId(), new BigDecimal("500.00"), null), transportUser.getId());
            assertThatThrownBy(() -> bidCommandService.delete(bid.id(), secondTransportUser.getId()))
                    .isInstanceOf(UnauthorizedException.class);
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────

    private User savedUser(String name, String email, UserType type, Role role) {
        User user = new User();
        user.setName(name);
        user.setSurname("Тэставы");
        user.setEmail(email);
        user.setPassword("hash");
        user.setUserType(type);
        Profile profile = Profile.builder().role(role).build();
        user.setProfile(profile);
        return userRepository.save(user);
    }

    private Request savedRequest(RequestStatus status, User author) {
        SpotRequest request = new SpotRequest();
        request.setAuthorId(author.getId());
        request.setStatus(status);
        request.setShipmentType(ShipmentType.FTL);
        request.setTransportType(TransportType.TILT);
        request.setDangerous(false);
        request.setStartDate(LocalDateTime.now().plusDays(1));
        request.setEndDate(LocalDateTime.now().plusDays(3));
        return requestRepository.save(request);
    }
}