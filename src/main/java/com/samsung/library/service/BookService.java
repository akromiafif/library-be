package com.samsung.library.service;

import com.samsung.library.dto.BookDTO;
import com.samsung.library.dto.SearchRequestDTO;
import com.samsung.library.model.Author;
import com.samsung.library.model.Book;
import com.samsung.library.repository.AuthorRepository;
import com.samsung.library.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class BookService {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private AuthorRepository authorRepository;

    // Create a new book
    public BookDTO createBook(BookDTO bookDTO) {
        Optional<Author> author = authorRepository.findById(bookDTO.getAuthorId());
        if (author.isPresent()) {
            Book book = convertToEntity(bookDTO);
            book.setAuthor(author.get());
            Book savedBook = bookRepository.save(book);
            return convertToDTO(savedBook);
        }
        throw new RuntimeException("Author not found with ID: " + bookDTO.getAuthorId());
    }

    // Get book by ID
    @Transactional(readOnly = true)
    public Optional<BookDTO> getBookById(Long id) {
        return bookRepository.findByIdWithAuthor(id)
                .map(this::convertToDTO);
    }

    // Get all books
    @Transactional(readOnly = true)
    public List<BookDTO> getAllBooks() {
        return bookRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Update book
    public BookDTO updateBook(Long id, BookDTO bookDTO) {
        Optional<Book> existingBook = bookRepository.findById(id);
        if (existingBook.isPresent()) {
            Book book = existingBook.get();

            // Update author if changed
            if (!book.getAuthor().getId().equals(bookDTO.getAuthorId())) {
                Optional<Author> newAuthor = authorRepository.findById(bookDTO.getAuthorId());
                if (newAuthor.isPresent()) {
                    book.setAuthor(newAuthor.get());
                } else {
                    throw new RuntimeException("Author not found with ID: " + bookDTO.getAuthorId());
                }
            }

            book.setTitle(bookDTO.getTitle());
            book.setCategory(bookDTO.getCategory());
            book.setPublishingYear(bookDTO.getPublishingYear());
            book.setIsbn(bookDTO.getIsbn());
            book.setDescription(bookDTO.getDescription());
            book.setTotalCopies(bookDTO.getTotalCopies());
            book.setAvailableCopies(bookDTO.getAvailableCopies());

            Book updatedBook = bookRepository.save(book);
            return convertToDTO(updatedBook);
        }
        return null;
    }

    // Delete book
    public boolean deleteBook(Long id) {
        if (bookRepository.existsById(id)) {
            bookRepository.deleteById(id);
            return true;
        }
        return false;
    }

    // Search books
    @Transactional(readOnly = true)
    public List<BookDTO> searchBooks(SearchRequestDTO searchRequest) {
        return bookRepository.searchBooks(
                        searchRequest.getBookTitle(),
                        searchRequest.getCategory(),
                        searchRequest.getAuthorName(),
                        searchRequest.getPublishingYear()
                ).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Get available books
    @Transactional(readOnly = true)
    public List<BookDTO> getAvailableBooks() {
        return bookRepository.findAvailableBooks().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Get books by category
    @Transactional(readOnly = true)
    public List<BookDTO> getBooksByCategory(String category) {
        return bookRepository.findByCategoryIgnoreCase(category).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Get all categories
    @Transactional(readOnly = true)
    public List<String> getAllCategories() {
        return bookRepository.findAllCategories();
    }

    // Update book availability (used when borrowing/returning)
    public boolean updateBookAvailability(Long bookId, int change) {
        Optional<Book> book = bookRepository.findById(bookId);
        if (book.isPresent()) {
            Book b = book.get();
            int newAvailableCopies = b.getAvailableCopies() + change;
            if (newAvailableCopies >= 0 && newAvailableCopies <= b.getTotalCopies()) {
                b.setAvailableCopies(newAvailableCopies);
                bookRepository.save(b);
                return true;
            }
        }
        return false;
    }

    // Convert entity to DTO
    private BookDTO convertToDTO(Book book) {
        BookDTO dto = new BookDTO();
        dto.setId(book.getId());
        dto.setTitle(book.getTitle());
        dto.setCategory(book.getCategory());
        dto.setPublishingYear(book.getPublishingYear());
        dto.setIsbn(book.getIsbn());
        dto.setDescription(book.getDescription());
        dto.setTotalCopies(book.getTotalCopies());
        dto.setAvailableCopies(book.getAvailableCopies());
        dto.setAuthorId(book.getAuthor().getId());
        dto.setAuthorName(book.getAuthor().getName());
        dto.setCreatedAt(book.getCreatedAt());
        dto.setUpdatedAt(book.getUpdatedAt());
        return dto;
    }

    // Convert DTO to entity
    private Book convertToEntity(BookDTO dto) {
        Book book = new Book();
        book.setTitle(dto.getTitle());
        book.setCategory(dto.getCategory());
        book.setPublishingYear(dto.getPublishingYear());
        book.setIsbn(dto.getIsbn());
        book.setDescription(dto.getDescription());
        book.setTotalCopies(dto.getTotalCopies());
        book.setAvailableCopies(dto.getAvailableCopies());
        return book;
    }
}