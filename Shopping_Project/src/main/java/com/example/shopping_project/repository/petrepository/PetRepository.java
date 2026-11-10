package com.example.shopping_project.repository.petrepository;

import com.example.shopping_project.entity.petentity.PetEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PetRepository extends JpaRepository<PetEntity, Long> {

    List<PetEntity> findByMemberIdOrderByIdAsc(Long memberId);
}
