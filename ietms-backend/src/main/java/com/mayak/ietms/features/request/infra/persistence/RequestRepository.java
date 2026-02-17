package com.mayak.ietms.features.request.infra.persistence;

import com.mayak.ietms.features.request.domain.enums.RequestStatus;
import com.mayak.ietms.features.request.domain.model.ContractRequest;
import com.mayak.ietms.features.request.domain.model.Request;
import com.mayak.ietms.features.request.domain.model.SpotRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface RequestRepository extends JpaRepository<Request, Long>, JpaSpecificationExecutor<Request>, RequestRepositoryCustom {

    @Query("""         
SELECT r FROM Request r
WHERE r.archived = false
ORDER BY
    CASE r.status
            WHEN NEW THEN 1
            WHEN IN_PROGRESS THEN 2
            WHEN BIDDING THEN 3
            WHEN OFFERED THEN 4
            WHEN ACCEPTED THEN 5
            WHEN REFUSED THEN 6
            ELSE 7
            END ,
            r.issueDate DESC
""")
    Page<Request> findAllActiveSorted(Pageable pageable);

    @Query("""
SELECT r FROM Request r
WHERE r.archived = false
  AND TYPE(r) = :clazz
ORDER BY
    CASE r.status
        WHEN NEW THEN 1
        WHEN IN_PROGRESS THEN 2
        WHEN BIDDING THEN 3
        WHEN OFFERED THEN 4
        WHEN ACCEPTED THEN 5
        WHEN REFUSED THEN 6
        ELSE 7
    END,
    r.issueDate DESC
""")
    Page<Request> findAllByType(@Param("clazz") Class<? extends Request> clazz, Pageable pageable);

    @Query("""
        SELECT COUNT(r)
        FROM Request r
        WHERE r.authorId = :userId
          AND r.issueDate BETWEEN :start AND :end
    """)
    int countByAuthorId(@Param("userId") Long userId,
                        @Param("start") Instant start,
                        @Param("end") Instant end);

    @Query("""
        SELECT COUNT(DISTINCT r)
        FROM Request r
        WHERE :userId MEMBER OF r.competitorsId
          AND r.issueDate BETWEEN :start AND :end
    """)
    int countByCompetitorsContains(@Param("userId") Long userId,
                                   @Param("start") Instant start,
                                   @Param("end") Instant end);

    @Query("""
        SELECT COUNT(DISTINCT b)
        FROM Bid b
        WHERE b.user.id = :userId
          AND b.time BETWEEN :start AND :end
    """)
    int countDistinctByBidsUser(@Param("userId") Long userId,
                                @Param("start") Instant start,
                                @Param("end") Instant end);

    @Query("""
        SELECT COUNT(r)
        FROM Request r
        WHERE r.dispatcherId = :userId
          AND r.issueDate BETWEEN :start AND :end
    """)
    int countByDispatchedUser(@Param("userId") Long userId,
                            @Param("start") Instant start,
                            @Param("end") Instant end);


    @Query("""
    SELECT COUNT(r)
    FROM Request r
    JOIN User u ON u.id = r.authorId
    JOIN Profile p ON p.id = u.id
    WHERE TYPE(r) = :type
      AND p.department.id = :departmentId
      AND r.issueDate BETWEEN :start AND :end
""")
    int countByTypeAndDepartment(@Param("type") Class<? extends Request> type,
                                 @Param("departmentId") Long departmentId,
                                 @Param("start") LocalDateTime start,
                                 @Param("end") LocalDateTime end);

    @Query("""
    SELECT COUNT(r)
    FROM Request r
    JOIN User u ON u.id = r.authorId
    JOIN Profile p ON p.id = u.id
    WHERE TYPE(r) = :type
      AND r.status = :status
      AND p.department.id = :departmentId
      AND r.issueDate BETWEEN :start AND :end
""")
    int countByTypeAndStatusAndDepartment(@Param("type") Class<? extends Request> type,
                                          @Param("status") RequestStatus status,
                                          @Param("departmentId") Long departmentId,
                                          @Param("start") LocalDateTime start,
                                          @Param("end") LocalDateTime end);

    @Query("""
    SELECT r.refuseReason, COUNT(r)
    FROM Request r
    JOIN User u ON u.id = r.authorId
    JOIN Profile p ON p.id = u.id
    WHERE r.status = :status
      AND p.department.id = :departmentId
      AND r.issueDate BETWEEN :startDate AND :endDate
      AND r.refuseReason IS NOT NULL
    GROUP BY r.refuseReason
""")
    List<Object[]> countRefusedByReason(@Param("type") Class<? extends Request> type,
                                        @Param("departmentId") Long departmentId,
                                        @Param("status") RequestStatus status,
                                        @Param("startDate") LocalDateTime startDate,
                                        @Param("endDate") LocalDateTime endDate);

    @Query("""
    SELECT r FROM SpotRequest r
    LEFT JOIN FETCH r.customer
    LEFT JOIN FETCH r.bids
    WHERE r.id = :id
""")
    Optional<SpotRequest> findFullSpotById(@Param("id") Long id);

    @Query("""
    SELECT r FROM ContractRequest r
    LEFT JOIN FETCH r.customer
    LEFT JOIN FETCH r.bids
    LEFT JOIN FETCH r.lane
    WHERE r.id = :id
""")
    Optional<ContractRequest> findFullContractById(@Param("id") Long id);

    @Query("""
    SELECT DISTINCT r FROM Request r
    LEFT JOIN FETCH r.customer
    LEFT JOIN FETCH r.bids
    WHERE r.issueDate BETWEEN :from AND :to
      AND r.status IN :statuses
""")
    List<Request> findFullByIssueDateBetweenAndStatusIn(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            @Param("statuses") List<RequestStatus> statuses
    );

    @Query("""
    SELECT DISTINCT r FROM Request r
    JOIN Profile p ON p.user.id = r.authorId
    LEFT JOIN FETCH r.customer с
    LEFT JOIN FETCH r.bids
    WHERE p.department.id = :departmentId
      AND r.issueDate BETWEEN :from AND :to
      AND r.status IN :statuses
""")
    List<Request> findFullByDepartmentAndIssueDateBetweenAndStatusIn(
            @Param("departmentId") Long departmentId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            @Param("statuses") List<RequestStatus> statuses
    );

    boolean existsByAuthorId(Long userId);

    boolean existsByDispatcherId(Long userId);

    @Query("""
        SELECT COUNT(r) > 0
        FROM Request r
        WHERE :userId MEMBER OF r.competitorsId
    """)
    boolean existsByCompetitor(@Param("userId") Long userId);

    @Query(
            value = """
            select exists (
                select 1
                from requests r
                where r.from_location_ids_order @> cast(:locationId as jsonb)
                   or r.to_location_ids_order   @> cast(:locationId as jsonb)
            )
        """,
            nativeQuery = true
    )
    boolean existsByLocationUsed(@Param("locationId") String locationIdJson);

    boolean existsByCustomer_Id(Long companyId);


    @Query("""
        SELECT r
        FROM Request r
        WHERE r.status IN :statuses
          AND r.issueDate < :threshold
    """)
    List<Request> findExpiredRequests(@Param("statuses") Set<RequestStatus> statuses, @Param("threshold") LocalDateTime threshold);

    @Query("""
        SELECT r
        FROM Request r
        WHERE r.status IN :statuses
          AND r.archived = false
          AND r.issueDate < :threshold
    """)
    List<Request> findRequestsForArchiving(@Param("statuses")Set<RequestStatus> statuses, @Param("threshold") LocalDateTime threshold);

}