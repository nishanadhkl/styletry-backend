package com.styletry.backend.repository;

import com.styletry.backend.model.Variant;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface VariantRepository extends JpaRepository<Variant, Long> {
    List<Variant> findByProductId(Long productId);
}