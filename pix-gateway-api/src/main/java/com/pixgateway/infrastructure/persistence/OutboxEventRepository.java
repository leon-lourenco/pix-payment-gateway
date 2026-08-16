package com.pixgateway.infrastructure.persistence;

import com.pixgateway.domain.OutboxEvent;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {

    List<OutboxEvent> findTop50ByPublishedAtIsNullOrderByCreatedAtAsc();

    Optional<OutboxEvent> findByAggregateId(UUID aggregateId);
}
