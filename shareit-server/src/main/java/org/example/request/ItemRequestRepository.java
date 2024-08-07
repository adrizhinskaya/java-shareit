package org.example.request;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.example.request.model.ItemRequest;

import java.util.List;

public interface ItemRequestRepository extends JpaRepository<ItemRequest, Long> {
    List<ItemRequest> findAllByRequester_IdOrderByCreatedDesc(Long requesterId);

    Page<ItemRequest> findAllByRequester_IdNot(Long requesterId, Pageable pageable);
}