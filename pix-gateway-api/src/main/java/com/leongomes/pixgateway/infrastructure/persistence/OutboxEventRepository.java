package com.leongomes.pixgateway.infrastructure.persistence;

import com.leongomes.pixgateway.domain.OutboxEvent;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {

    List<OutboxEvent> findTop50ByPublishedAtIsNullOrderByCreatedAtAsc();
}
