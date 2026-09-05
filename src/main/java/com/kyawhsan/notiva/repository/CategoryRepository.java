package com.kyawhsan.notiva.repository;

import com.kyawhsan.notiva.entity.Category;
import com.kyawhsan.notiva.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

        List<Category> findAllByUserOrderByNameAsc(
                        User user);

        Optional<Category> findByIdAndUser(
                        Long id,
                        User user);

        boolean existsByUserAndNameIgnoreCase(
                        User user,
                        String name);

        boolean existsByUserAndNameIgnoreCaseAndIdNot(
                        User user,
                        String name,
                        Long id);

        void deleteAllByUser(
                        User user);

}