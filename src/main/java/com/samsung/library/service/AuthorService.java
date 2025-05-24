package com.samsung.library.service;

import com.samsung.library.dto.AuthorDTO;
import com.samsung.library.dto.BookSummaryDTO;
import com.samsung.library.model.Author;
import com.samsung.library.repository.AuthorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class AuthorService {

    @Autowired
    private AuthorRepository authorRepository;

    // Create a new author
    public AuthorDTO createAuthor(AuthorDTO authorDTO) {
        Author author = convertToEntity(authorDTO);
        Author savedAuthor = authorRepository.save(author);
        return convertToDTO(savedAuthor);
    }

    // Get author by ID
    @Transactional(readOnly = true)
    public Optional<AuthorDTO> getAuthorById(Long id) {
        return authorRepository.findById(id)
                .map(this::convertToDTO);
    }

    // Get author by ID with books
    @Transactional(readOnly = true)
    public Optional<AuthorDTO> getAuthorByIdWithBooks(Long id) {
        return authorRepository.findByIdWithBooks(id)
                .map(this::convertToDTOWithBooks);
    }

    // Get all authors
    @Transactional(readOnly = true)
    public List<AuthorDTO> getAllAuthors() {
        return authorRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Update author
    public AuthorDTO updateAuthor(Long id, AuthorDTO authorDTO) {
        Optional<Author> existingAuthor = authorRepository.findById(id);
        if (existingAuthor.isPresent()) {
            Author author = existingAuthor.get();
            author.setName(authorDTO.getName());
            author.setBiography(authorDTO.getBiography());
            author.setBirthYear(authorDTO.getBirthYear());
            author.setNationality(authorDTO.getNationality());

            Author updatedAuthor = authorRepository.save(author);
            return convertToDTO(updatedAuthor);
        }
        return null;
    }

    // Delete author
    public boolean deleteAuthor(Long id) {
        if (authorRepository.existsById(id)) {
            authorRepository.deleteById(id);
            return true;
        }
        return false;
    }

    // Search authors by name
    @Transactional(readOnly = true)
    public List<AuthorDTO> searchAuthorsByName(String name) {
        return authorRepository.findByNameContainingIgnoreCase(name).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Find authors by nationality
    @Transactional(readOnly = true)
    public List<AuthorDTO> findAuthorsByNationality(String nationality) {
        return authorRepository.findByNationalityIgnoreCase(nationality).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Convert entity to DTO
    private AuthorDTO convertToDTO(Author author) {
        AuthorDTO dto = new AuthorDTO();
        dto.setId(author.getId());
        dto.setName(author.getName());
        dto.setBiography(author.getBiography());
        dto.setBirthYear(author.getBirthYear());
        dto.setNationality(author.getNationality());
        dto.setCreatedAt(author.getCreatedAt());
        dto.setUpdatedAt(author.getUpdatedAt());
        return dto;
    }

    // Convert entity to DTO with books
    private AuthorDTO convertToDTOWithBooks(Author author) {
        AuthorDTO dto = convertToDTO(author);
        if (author.getBooks() != null) {
            List<BookSummaryDTO> bookSummaries = author.getBooks().stream()
                    .map(book -> new BookSummaryDTO(
                            book.getId(),
                            book.getTitle(),
                            book.getCategory(),
                            book.getPublishingYear(),
                            book.getAvailableCopies(),
                            book.getTotalCopies()
                    ))
                    .collect(Collectors.toList());
            dto.setBooks(bookSummaries);
        }
        return dto;
    }

    // Convert DTO to entity
    private Author convertToEntity(AuthorDTO dto) {
        Author author = new Author();
        author.setName(dto.getName());
        author.setBiography(dto.getBiography());
        author.setBirthYear(dto.getBirthYear());
        author.setNationality(dto.getNationality());
        return author;
    }
}