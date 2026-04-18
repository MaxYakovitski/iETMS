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
        WITH
        created AS (
                    SELECT author_id AS user_id, COUNT(*) AS cnt
                    FROM requests
                    WHERE issue_date >= :from AND issue_date < :toExclusive
                    AND author_id = ANY(:userIds)
                    GROUP BY author_id
                   ),
        joined AS (
                    SELECT rc.user_id, COUNT(*) AS cnt
                    FROM request_competitors rc
                    JOIN requests r ON r.id = rc.request_id
                    AND r.issue_date >= :from AND r.issue_date < :toExclusive
                    WHERE rc.user_id = ANY(:userIds)
                    GROUP BY rc.user_id
                    ),
        first_bids AS (
                    SELECT b.user_id, b.request_id, MIN(b.time) AS first_time
                    FROM bids b
                    JOIN requests r ON r.id = b.request_id
                    AND r.request_type = 'SPOT'
                    AND r.issue_date >= :from AND r.issue_date < :toExclusive
                    WHERE b.deleted = false AND b.user_id = ANY(:userIds)
                    GROUP BY b.user_id, b.request_id
                    ),
        bids_agg AS (
                    SELECT fb.user_id,
                    COUNT(*) AS bided,
                    ROUND(AVG(GREATEST(EXTRACT(EPOCH FROM (fb.first_time - r.issue_date)) / 60.0, 0))::numeric, 2) AS avg_response
                    FROM first_bids fb
                    JOIN requests r ON r.id = fb.request_id
                    GROUP BY fb.user_id
                    ),
        accepted_spot AS (
                    SELECT author_id AS user_id, COUNT(*) AS cnt
                    FROM requests
                    WHERE status = 'ACCEPTED' AND request_type = 'SPOT'
                    AND issue_date >= :from AND issue_date < :toExclusive
                    AND author_id = ANY(:userIds)
                    GROUP BY author_id
                    ),
        accepted_contract AS (
                    SELECT author_id AS user_id, COUNT(*) AS cnt
                    FROM requests
                    WHERE status = 'ACCEPTED' AND request_type = 'CONTRACT'
                    AND issue_date >= :from AND issue_date < :toExclusive
                    AND author_id = ANY(:userIds)
                    GROUP BY author_id
                    ),
        dispatched AS (
                    SELECT dispatcher_id AS user_id, COUNT(*) AS cnt
                    FROM requests
                    WHERE issue_date >= :from AND issue_date < :toExclusive
                    AND dispatcher_id = ANY(:userIds)
                    GROUP BY dispatcher_id
                    )
            SELECT
            u.id AS userId,
            u.name AS firstName,
            u.surname AS lastName,
            COALESCE(c.cnt, 0)   AS created,
            COALESCE(j.cnt, 0)   AS joined,
            COALESCE(ba.bided, 0) AS bided,
            COALESCE(asp.cnt, 0) AS acceptedSpot,
            COALESCE(ac.cnt, 0)  AS acceptedContract,
            COALESCE(d.cnt, 0)   AS dispatched,
            COALESCE(ba.avg_response, 0) AS avgResponseMinutes
            FROM users u
            LEFT JOIN created c          ON c.user_id = u.id
            LEFT JOIN joined j           ON j.user_id = u.id
            LEFT JOIN bids_agg ba        ON ba.user_id = u.id
            LEFT JOIN accepted_spot asp  ON asp.user_id = u.id
            LEFT JOIN accepted_contract ac ON ac.user_id = u.id
            LEFT JOIN dispatched d       ON d.user_id = u.id
        WHERE u.id = ANY(:userIds)
        """, nativeQuery = true)
    List<UserStatsRow> userStats(
            @Param("from") Instant from,
            @Param("toExclusive") Instant toExclusive,
            @Param("userIds") Long[] userIds
    );
}