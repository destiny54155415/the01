package com.example.shopping_project.repository.cartitem;

import com.example.shopping_project.entity.cartitem.CartItemEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CartItemRepository extends JpaRepository<CartItemEntity, Long> {

    @Query("select c from CartItemEntity c join fetch c.product p left join fetch p.category where c.memberId = :memberId order by c.id desc")
    List<CartItemEntity> findByMemberIdWithProduct(@Param("memberId") Long memberId);

    Optional<CartItemEntity> findByMemberIdAndProduct_ProductId(Long memberId, Long productId);
}
