package com.luckyducky.luckyducky.repositories;

import com.luckyducky.luckyducky.model.Budget;
import com.luckyducky.luckyducky.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BudgetRepository extends JpaRepository<Budget, Long> {

    public Budget findBudgetByUserAndName(User user, String name);
}
