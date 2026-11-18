package com.topwatch.back_topwatch.repository;

import com.topwatch.back_topwatch.domain.UserBlock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserBlockRepository extends JpaRepository<UserBlock, Long> {

    List<UserBlock> findByBlocker_IdOrderByCreationDateDesc(Long blockerId);

    boolean existsByBlocker_IdAndBlocked_Id(Long blockerId, Long blockedId);

    @Modifying
    @Query("delete from UserBlock b where b.blocker.id = :blockerId and b.blocked.id = :blockedId")
    void deleteByBlockerAndBlocked(@Param("blockerId") Long blockerId, @Param("blockedId") Long blockedId);

    /** Every block the user made or received. */
    @Modifying
    @Query("delete from UserBlock b where b.blocker.id = :userId or b.blocked.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);

}
