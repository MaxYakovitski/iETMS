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

public interface RequestRepository
        extends JpaRepository<Request, Long>, JpaSpecificationExecutor<Request>, RequestRepositoryCustom {

    /**
     * Returns all non-archived requests sorted by status priority
     * (NEW → IN_PROGRESS → BIDDING → OFFERED → ACCEPTED → REFUSED),
     * then by updatedAt date descending within each status group.
     */
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
            r.updatedAt DESC
""")
    Page<Request> findAllActiveSorted(Pageable pageable);

    /**
     * Same ordering as {@link #findAllActiveSorted} but filtered by request type
     * ({@link SpotRequest} or {@link ContractRequest}).
     */
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
    r.updatedAt DESC
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
    SELECT COUNT(r)
    FROM Request r
    WHERE r.authorId = :userId
      AND r.status = :status
      AND TYPE(r) = :type
      AND r.issueDate BETWEEN :from AND :to
""")
    int countByAuthorIdAndStatusAndType(@Param("userId") Long userId,
                                        @Param("status") RequestStatus status,
                                        @Param("type") Class<? extends Request> type,
                                        @Param("from") Instant from,
                                        @Param("to") Instant to);

    @Query("""
        SELECT COUNT(DISTINCT r)
        FROM Request r
        WHERE :userId MEMBER OF r.competitorsId
          AND r.issueDate BETWEEN :start AND :end
    """)
    int countByCompetitorsContains(@Param("userId") Long userId,
                                   @Param("start") Instant start,
                                   @Param("end") Instant end);

    /**
     * Counts distinct requests that have at least one bid placed by the given user
     * within the time range. Counts requests, not individual bids.
     */
    @Query("""
        SELECT COUNT(DISTINCT b.request.id)
        FROM Bid b
        WHERE b.user.id = :userId
          AND b.time BETWEEN :start AND :end
    """)
    int countRequestsWithBidByUser(@Param("userId") Long userId,
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
      AND r.issueDate >= :from
      AND r.issueDate < :toExclusive
""")
    int countByTypeAndDepartment(@Param("type") Class<? extends Request> type,
                                 @Param("departmentId") Long departmentId,
                                 @Param("from") Instant from,
                                 @Param("toExclusive") Instant toExclusive);

    @Query("""
    SELECT COUNT(r)
    FROM Request r
    JOIN User u ON u.id = r.authorId
    JOIN Profile p ON p.id = u.id
    WHERE TYPE(r) = :type
      AND r.status = :status
      AND p.department.id = :departmentId
      AND r.issueDate >= :from
      AND r.issueDate < :toExclusive
""")
    int countByTypeAndStatusAndDepartment(@Param("type") Class<? extends Request> type,
                                          @Param("status") RequestStatus status,
                                          @Param("departmentId") Long departmentId,
                                          @Param("from") Instant from,
                                          @Param("toExclusive") Instant toExclusive);

    @Query("""
    SELECT COUNT(r)
    FROM Request r
    JOIN User u ON u.id = r.authorId
    JOIN Profile p ON p.id = u.id
    WHERE TYPE(r) = :type
      AND r.status = :status
      AND r.refuseReason = :reasonCode
      AND p.department.id = :departmentId
      AND r.issueDate >= :from
      AND r.issueDate < :toExclusive
""")
    int countRefusedByReasonAndType(
            @Param("type") Class<? extends Request> type,
            @Param("departmentId") Long departmentId,
            @Param("status") RequestStatus status,
            @Param("reasonCode") String reasonCode,
            @Param("from") Instant from,
            @Param("toExclusive") Instant toExclusive
    );

    /**
     * Returns pairs of [refuseReason, count] for refused requests
     * of the given type in the given department and date range.
     * Result rows: {@code Object[0]} — reason code (String),
     * {@code Object[1]} — count (Long).
     */
    @Query("""
    SELECT r.refuseReason, COUNT(r)
    FROM Request r
    JOIN User u ON u.id = r.authorId
    JOIN Profile p ON p.id = u.id
    WHERE TYPE(r) = :type
      AND r.status = :status
      AND p.department.id = :departmentId
      AND r.issueDate >= :from
      AND r.issueDate < :toExclusive
      AND r.refuseReason IS NOT NULL
    GROUP BY r.refuseReason
""")
    List<Object[]> countRefusedByReason(@Param("type") Class<? extends Request> type,
                                        @Param("departmentId") Long departmentId,
                                        @Param("status") RequestStatus status,
                                        @Param("from") Instant from,
                                        @Param("toExclusive") Instant toExclusive);

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
            @Param("from") Instant from,
            @Param("to") Instant to,
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
            @Param("from") Instant from,
            @Param("to") Instant to,
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

    /**
     * Checks whether the given location is referenced in any request's
     * from/to location lists using a native PostgreSQL JSONB containment query.
     */
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

    /**
     * Finds requests eligible for automatic expiry by the scheduler.
     * Filters by status and {@code startDate} — requests whose start date
     * is before the given threshold.
     */
    @Query("""
        SELECT r
        FROM Request r
        WHERE r.status IN :statuses
          AND r.startDate < :threshold
    """)
    List<Request> findExpiredRequests(@Param("statuses") Set<RequestStatus> statuses, @Param("threshold") LocalDateTime threshold);

    /**
     * Finds non-archived requests eligible for archiving by the scheduler.
     * Filters by status and {@code issueDate} — requests whose issue date
     * is before the given threshold.
     */
    @Query("""
        SELECT r
        FROM Request r
        WHERE r.status IN :statuses
          AND r.archived = false
          AND r.issueDate < :threshold
    """)
    List<Request> findRequestsForArchiving(@Param("statuses")Set<RequestStatus> statuses, @Param("threshold") Instant threshold);

}