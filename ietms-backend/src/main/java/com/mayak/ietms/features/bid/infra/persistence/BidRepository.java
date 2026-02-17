package com.mayak.ietms.features.bid.infra.persistence;

import com.mayak.ietms.features.bid.domain.model.Bid;
import com.mayak.ietms.features.request.domain.model.Request;
import com.mayak.ietms.features.user.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public interface BidRepository extends JpaRepository<Bid, Long> {

    Set<Bid> findByRequest(Request request);
    Set<Bid> findByRequestAndDeletedFalse(Request request);
    List<Bid> findByRequestOrderByTimeDesc(Request request);
    List<Bid> findByUserAndTimeBetween(User user, LocalDateTime from, LocalDateTime to);
}