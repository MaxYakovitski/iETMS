package com.mayak.ietms.features.statistics.infra.persistence;

import com.mayak.ietms.features.user.domain.model.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public interface UserStatisticsRepository extends Repository <User, Long> {

    interface UserStatsRow {
        Long getUserId();
        String getFirstName();
        String getLastName();

        Integer getCreated();
        Integer getJoined();
        Integer getBided();
        Integer getAcceptedSpot();
        Integer getAcceptedContract();
        Integer getDispatched();

        BigDecimal getAvgResponseMinutes();
    }

    @Query(value = """
        WITH first_bids AS (
            SELECT
                b.user_id,
                b.request_id,
                MIN(b.time) AS first_time
            FROM bids b
            JOIN requests r ON r.id = b.request_id
            WHERE b.deleted = false
              AND r.request_type = 'SPOT'
              AND r.issue_date >= :from
              AND r.issue_date < :toExclusive
              AND b.user_id = ANY(:userIds)
            GROUP BY b.user_id, b.request_id
        )
        SELECT
            u.id AS userId,
            u.name AS firstName,
            u.surname AS lastName,

            COUNT(DISTINCT r_created.id) AS created,
            COUNT(DISTINCT r_joined.id) AS joined,
            COUNT(DISTINCT fb.request_id) AS bided,
            COUNT(DISTINCT r_accepted_spot.id) AS acceptedSpot,
            COUNT(DISTINCT r_accepted_contract.id) AS acceptedContract,
            COUNT(DISTINCT r_dispatched.id) AS dispatched,

            COALESCE(
                ROUND(
                    AVG(
                        GREATEST(
                            EXTRACT(EPOCH FROM (fb.first_time - r.issue_date)) / 60.0,
                            0
                        )
                    )::numeric,
                    2
                ),
                0
            ) AS avgResponseMinutes

        FROM users u
        LEFT JOIN requests r_created
            ON r_created.author_id = u.id
               AND r_created.issue_date >= :from
               AND r_created.issue_date < :toExclusive

        LEFT JOIN request_competitors rc
            ON rc.user_id = u.id
        LEFT JOIN requests r_joined
            ON r_joined.id = rc.request_id
               AND r_joined.issue_date >= :from
               AND r_joined.issue_date < :toExclusive

        LEFT JOIN first_bids fb
            ON fb.user_id = u.id

        LEFT JOIN requests r
            ON r.id = fb.request_id
        
        LEFT JOIN requests r_accepted_spot
            ON r_accepted_spot.author_id = u.id
               AND r_accepted_spot.status = 'ACCEPTED'
               AND r_accepted_spot.request_type = 'SPOT'
               AND r_accepted_spot.issue_date >= :from
               AND r_accepted_spot.issue_date < :toExclusive
        
        LEFT JOIN requests r_accepted_contract
            ON r_accepted_contract.author_id = u.id
               AND r_accepted_contract.status = 'ACCEPTED'
               AND r_accepted_contract.request_type = 'CONTRACT'
               AND r_accepted_contract.issue_date >= :from
               AND r_accepted_contract.issue_date < :toExclusive
        
        LEFT JOIN requests r_dispatched
            ON r_dispatched.dispatcher_id = u.id
               AND r_dispatched.issue_date >= :from
               AND r_dispatched.issue_date < :toExclusive

        WHERE u.id = ANY(:userIds)
        GROUP BY u.id, u.name, u.surname
        """, nativeQuery = true)
    List<UserStatsRow> userStats(
            @Param("from") Instant from,
            @Param("toExclusive") Instant toExclusive,
            @Param("userIds") Long[] userIds
    );
}