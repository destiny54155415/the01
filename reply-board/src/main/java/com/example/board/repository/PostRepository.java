package com.example.board.repository;

import com.example.board.domain.Post;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostRepository extends JpaRepository<Post, Long> {

    List<Post> findAllByOrderByGroupNoDescGroupOrderAsc();

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update Post p
            set p.groupOrder = p.groupOrder + 1
            where p.groupNo = :groupNo and p.groupOrder > :groupOrder
            """)
    void shiftOrders(@Param("groupNo") long groupNo, @Param("groupOrder") int groupOrder);
}
