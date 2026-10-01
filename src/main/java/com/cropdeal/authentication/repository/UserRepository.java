package com.cropdeal.authentication.repository;
 
import com.cropdeal.authentication.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
 
import java.util.Optional;
 
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
 
    Optional<User> findByEmail(String email);

    long countByActive(boolean active);

    @Query(value = "select count(*) from users where find_in_set(upper(:role), upper(role)) > 0", nativeQuery = true)
    long countUsersWithRole(@Param("role") String role);
 
}