package com.styletry.backend.repository;

import com.styletry.backend.model.Order;
import com.styletry.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUser(User user);
}
