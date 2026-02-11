package com.example.naebuilding.repository;

import com.example.naebuilding.domain.RequestImageEntity;
import com.example.naebuilding.dto.RequestImageDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RequestImageRepository extends JpaRepository<RequestImageEntity, Long> {

    @Query("""
    SELECT new com.example.naebuilding.dto.RequestImageDto(
        ri.imageId,
        ri.imageUrl,
        ri.sortOrder
    )
    FROM RequestImageEntity ri
    WHERE ri.request.requestId = :requestId
    ORDER BY ri.sortOrder ASC
""")
    List<RequestImageDto> findImageDtosByRequestId(@Param("requestId") Long requestId);

}
