package com.samsung.library.repository;

import com.samsung.library.model.BorrowedBook;
import com.samsung.library.model.BorrowStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BorrowedBookRepository extends JpaRepository<BorrowedBook, Long> {

    // Find borrowed books by member
    List<BorrowedBook> findByMemberId(Long memberId);

    // Find borrowed books by book
    List<BorrowedBook> findByBookId(Long bookId);

    // Find borrowed books by status
    List<BorrowedBook> findByStatus(BorrowStatus status);

    // Find borrowed books by member and status
    List<BorrowedBook> findByMemberIdAndStatus(Long memberId, BorrowStatus status);

    // Find borrowed books by book and status
    List<BorrowedBook> findByBookIdAndStatus(Long bookId, BorrowStatus status);

    // Find overdue books
    @Query("SELECT bb FROM BorrowedBook bb WHERE bb.dueDate < :currentDate AND bb.status = 'BORROWED'")
    List<BorrowedBook> findOverdueBooks(@Param("currentDate") LocalDate currentDate);

    // Find books borrowed on a specific date
    List<BorrowedBook> findByBorrowDate(LocalDate borrowDate);

    // Find books borrowed between dates
    List<BorrowedBook> findByBorrowDateBetween(LocalDate startDate, LocalDate endDate);

    // Find books returned between dates
    List<BorrowedBook> findByReturnDateBetween(LocalDate startDate, LocalDate endDate);

    // Search functionality for borrowed books
    @Query("SELECT bb FROM BorrowedBook bb JOIN bb.book b JOIN bb.member m JOIN b.author a WHERE " +
            "(:bookTitle IS NULL OR LOWER(b.title) LIKE LOWER(CONCAT('%', :bookTitle, '%'))) AND " +
            "(:memberName IS NULL OR LOWER(m.name) LIKE LOWER(CONCAT('%', :memberName, '%'))) AND " +
            "(:borrowDate IS NULL OR bb.borrowDate = :borrowDate) AND " +
            "(:status IS NULL OR bb.status = :status)")
    List<BorrowedBook> searchBorrowedBooks(@Param("bookTitle") String bookTitle,
                                           @Param("memberName") String memberName,
                                           @Param("borrowDate") LocalDate borrowDate,
                                           @Param("status") BorrowStatus status);

    // Find borrowed book with full details
    @Query("SELECT bb FROM BorrowedBook bb " +
            "JOIN FETCH bb.book b " +
            "JOIN FETCH bb.member m " +
            "JOIN FETCH b.author a " +
            "WHERE bb.id = :id")
    Optional<BorrowedBook> findByIdWithDetails(@Param("id") Long id);

    // Get borrowing statistics
    @Query("SELECT bb.status, COUNT(bb) FROM BorrowedBook bb GROUP BY bb.status")
    List<Object[]> getBorrowingStatistics();

    // Find most popular books (most borrowed)
    @Query("SELECT b.title, a.name, COUNT(bb) as borrowCount FROM BorrowedBook bb " +
            "JOIN bb.book b JOIN b.author a " +
            "GROUP BY b.id, b.title, a.name " +
            "ORDER BY borrowCount DESC")
    List<Object[]> findMostPopularBooks();

    // Find most active members (most borrowings)
    @Query("SELECT m.name, m.email, COUNT(bb) as borrowCount FROM BorrowedBook bb " +
            "JOIN bb.member m " +
            "GROUP BY m.id, m.name, m.email " +
            "ORDER BY borrowCount DESC")
    List<Object[]> findMostActiveMembers();

    // Check if a book is currently borrowed by a member
    @Query("SELECT COUNT(bb) > 0 FROM BorrowedBook bb WHERE bb.book.id = :bookId AND bb.member.id = :memberId AND bb.status = 'BORROWED'")
    boolean isBookCurrentlyBorrowedByMember(@Param("bookId") Long bookId, @Param("memberId") Long memberId);

    // Count current borrowings by member
    @Query("SELECT COUNT(bb) FROM BorrowedBook bb WHERE bb.member.id = :memberId AND bb.status = 'BORROWED'")
    Long countCurrentBorrowingsByMember(@Param("memberId") Long memberId);

    // Find books due today
    @Query("SELECT bb FROM BorrowedBook bb WHERE bb.dueDate = :today AND bb.status = 'BORROWED'")
    List<BorrowedBook> findBooksDueToday(@Param("today") LocalDate today);

    // Calculate total fines for a member
    @Query("SELECT COALESCE(SUM(bb.fineAmount), 0) FROM BorrowedBook bb WHERE bb.member.id = :memberId")
    Double calculateTotalFinesByMember(@Param("memberId") Long memberId);
}