package com.mayak.ietms.features.request.infra.persistance;

import com.mayak.ietms.AbstractRepositoryTest;
import com.mayak.ietms.features.company.domain.model.Company;
import com.mayak.ietms.features.company.infra.persistence.CompanyRepository;
import com.mayak.ietms.features.request.domain.enums.RequestStatus;
import com.mayak.ietms.features.request.domain.enums.ShipmentType;
import com.mayak.ietms.features.request.domain.enums.TransportType;
import com.mayak.ietms.features.request.domain.model.Request;
import com.mayak.ietms.features.request.domain.model.SpotRequest;
import com.mayak.ietms.features.request.infra.persistence.RequestRepository;
import com.mayak.ietms.request.dto.enums.RequestStatusDto;
import com.mayak.ietms.request.dto.filter.RequestFilterDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Consumer;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@DisplayName("RequestRepositoryImpl")
public class RequestRepositoryImplTest extends AbstractRepositoryTest {

    @Autowired
    private RequestRepository requestRepository;

    @Autowired
    private CompanyRepository companyRepository;

    // ─────────────────────────────────────────────────────────────
    // filterByQuery
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("filterByQuery — пусты фільтр — вяртае толькі неархіваваныя заяўкі")
    void filterByQuery_emptyFilter_returnsNonArchivedOnly() {
        saveSpot(r -> r.setCustomerReference("A1"));
        saveSpot(r -> r.setCustomerReference("A2"));
        saveSpot(r -> { r.setCustomerReference("A3"); r.setArchived(true); });

        Page<Request> result = requestRepository
                .filterByQuery(new RequestFilterDto(), null, PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).noneMatch(Request::isArchived);
    }

    @Test
    @DisplayName("filterByQuery — фільтр па статусу — вяртае толькі адпаведныя")
    void filterByQuery_withStatusFilter_returnsMatchingOnly() {
        saveSpot(r -> r.setStatus(RequestStatus.NEW));
        saveSpot(r -> r.setStatus(RequestStatus.IN_PROGRESS));
        saveSpot(r -> r.setStatus(RequestStatus.BIDDING));

        RequestFilterDto filter = new RequestFilterDto();
        filter.setStatuses(List.of(RequestStatusDto.NEW, RequestStatusDto.IN_PROGRESS));

        Page<Request> result = requestRepository.filterByQuery(
                filter, null, PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent())
                .extracting(r -> r.getStatus().name())
                .containsExactlyInAnyOrder("NEW", "IN_PROGRESS");
    }

    @Test
    @DisplayName("filterByQuery — фільтр па вазе — вяртае запісы ў дыяпазоне")
    void filterByQuery_withWeightRange_returnsMatchingOnly() {
        saveSpot(r -> r.setWeight(500.0));   // у дыяпазоне
        saveSpot(r -> r.setWeight(1000.0));  // у дыяпазоне
        saveSpot(r -> r.setWeight(100.0));   // ніжэй мінімуму
        saveSpot(r -> r.setWeight(2000.0));  // вышэй максімуму

        RequestFilterDto filter = new RequestFilterDto();
        filter.setMinWeight(400.0);
        filter.setMaxWeight(1200.0);

        Page<Request> result = requestRepository.filterByQuery(
                filter, null, PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent())
                .extracting(Request::getWeight)
                .allMatch(w -> w >= 400.0 && w <= 1200.0);
    }

    @Test
    @DisplayName("filterByQuery — фільтр па назве кампаніі — LOWER LIKE, без уліку рэгістра")
    void filterByQuery_withCompanyName_returnsCaseInsensitiveMatch() {
        Company logistics = companyRepository.save(new Company("Logistics Partner"));
        Company other = companyRepository.save(new Company("Fast Freight"));

        saveSpot(r -> r.setCustomer(logistics));
        saveSpot(r -> r.setCustomer(other));
        saveSpot(r -> {});  // без кампаніі

        RequestFilterDto filter = new RequestFilterDto();
        filter.setCompanyName("logist");  // Company.normalize() → "LOGISTICS PARTNER"

        Page<Request> result = requestRepository.filterByQuery(
                filter, null, PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().getFirst().getCustomer().getName())
                .isEqualTo("LOGISTICS PARTNER");
    }

    @Test
    @DisplayName("filterByQuery — пагінацыя — totalElements і памер старонкі карэктныя")
    void filterByQuery_pagination_returnsCorrectPageData() {
        for (int i = 0; i < 5; i++) saveSpot(r -> {});

        Page<Request> page0 = requestRepository
                .filterByQuery(new RequestFilterDto(), null, PageRequest.of(0, 2));
        Page<Request> page2 = requestRepository
                .filterByQuery(new RequestFilterDto(), null, PageRequest.of(2, 2));

        assertThat(page0.getTotalElements()).isEqualTo(5);
        assertThat(page0.getContent()).hasSize(2);
        assertThat(page2.getContent()).hasSize(1);
    }

    // ─────────────────────────────────────────────────────────────
    // searchByQuery
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("searchByQuery — пусты ці бланкавы запыт — вяртае пусты вынік")
    void searchByQuery_blankQuery_returnsEmpty() {
        saveSpot(r -> r.setCustomerReference("SEARCH-REF-001"));

        Page<Request> result = requestRepository.searchByQuery(
                "   ", null, null, PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isZero();
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    @DisplayName("searchByQuery — па customerReference — знаходзіць адпаведную заяўку")
    void searchByQuery_byCustomerReference_returnsMatch() {
        saveSpot(r -> r.setCustomerReference("CARGO-2026-001"));
        saveSpot(r -> r.setCustomerReference("OTHER-CARGO"));

        Page<Request> result = requestRepository.searchByQuery(
                "cargo-2026", null, null, PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().getFirst().getCustomerReference())
                .isEqualTo("CARGO-2026-001");
    }

    @Test
    @DisplayName("searchByQuery — па TID — знаходзіць адпаведную заяўку")
    void searchByQuery_byTid_returnsMatch() {
        saveSpot(r -> r.setTid("TID-XYZ-99"));
        saveSpot(r -> r.setTid("TID-ABC-01"));

        Page<Request> result = requestRepository.searchByQuery(
                "xyz", null, null, PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().getFirst().getTid()).isEqualTo("TID-XYZ-99");
    }

    @Test
    @DisplayName("searchByQuery — архіваваная заяўка — не патрапляе ў вынік")
    void searchByQuery_archivedRequest_notIncluded() {
        saveSpot(r -> { r.setArchived(true); r.setCustomerReference("ARCHIVED-REF"); });

        Page<Request> result = requestRepository.searchByQuery(
                "archived", null, null, PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isZero();
    }

    // ─────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────

    private void saveSpot(Consumer<SpotRequest> customizer) {
        SpotRequest r = new SpotRequest();
        r.setAuthorId(999L);          // author_id не мае FK — можна любы Long
        r.setShipmentType(ShipmentType.FTL);
        r.setTransportType(TransportType.TILT);
        r.setDangerous(false);
        r.setStartDate(LocalDateTime.now().plusDays(1));
        r.setEndDate(LocalDateTime.now().plusDays(3));
        r.setStatus(RequestStatus.NEW);
        r.setArchived(false);
        customizer.accept(r);
        requestRepository.save(r);
    }

}