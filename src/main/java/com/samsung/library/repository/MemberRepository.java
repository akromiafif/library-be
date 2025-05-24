package com.samsung.library.repository;

import com.samsung.library.model.Member;
import com.samsung.library.model.MembershipStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    // Find member by email
    Optional<Member> findByEmail(String email);

    // Find members by name (case insensitive)
    List<Member> findByNameContainingIgnoreCase(String name);

    // Find members by membership status
    List<Member> findByMembershipStatus(MembershipStatus status);

    // Find members by phone
    Optional<Member> findByPhone(String phone);

    // Find members who joined between dates
    List<Member> findByMembershipDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    // Find member with their borrowed books
    @Query("SELECT m FROM Member m LEFT JOIN FETCH m.borrowedBooks WHERE m.id = :id")
    Optional<Member> findByIdWithBorrowedBooks(@Param("id") Long id);

    // Find members with overdue books
    @Query("SELECT DISTINCT m FROM Member m JOIN m.borrowedBooks bb WHERE bb.status = 'OVERDUE'")
    List<Member> findMembersWithOverdueBooks();

    // Find members with active borrowings
    @Query("SELECT DISTINCT m FROM Member m JOIN m.borrowedBooks bb WHERE bb.status = 'BORROWED'")
    List<Member> findMembersWithActiveBorrowings();

    // Count active members
    @Query("SELECT COUNT(m) FROM Member m WHERE m.membershipStatus = 'ACTIVE'")
    Long countActiveMembers();

    // Search members by name or email
    @Query("SELECT m FROM Member m WHERE " +
            "LOWER(m.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(m.email) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Member> searchMembers(@Param("searchTerm") String searchTerm);
}