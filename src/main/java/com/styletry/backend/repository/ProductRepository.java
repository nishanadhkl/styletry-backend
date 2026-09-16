package com.styletry.backend.repository;

import com.styletry.backend.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByCategory(String category);
    List<Product> findByNameContainingIgnoreCase(String name);
    List<Product> findByDiscountPercentGreaterThan(Integer discount);
    List<Product> findByIsNewArrivalTrue();

    @Query("""
            select p from Product p
            where (coalesce(:category, '') = '' or lower(p.category) = lower(coalesce(:category, '')))
              and (:saleOnly = false or p.discountPercent > 0)
              and (:newOnly = false or p.isNewArrival = true)
              and (coalesce(:query, '') = ''
                   or lower(p.name) like lower(concat('%', coalesce(:query, ''), '%'))
                   or lower(coalesce(p.description, '')) like lower(concat('%', coalesce(:query, ''), '%')))
            """)
    Page<Product> findProductsPage(@Param("category") String category,
                                   @Param("saleOnly") boolean saleOnly,
                                   @Param("newOnly") boolean newOnly,
                                   @Param("query") String query,
                                   Pageable pageable);
}
