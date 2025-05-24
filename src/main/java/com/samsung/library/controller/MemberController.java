package com.samsung.library.controller;

import com.samsung.library.dto.ApiResponseDTO;
import com.samsung.library.dto.MemberDTO;
import com.samsung.library.service.MemberService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/members")
@CrossOrigin(origins = "*")
public class MemberController {

    @Autowired
    private MemberService memberService;

    // Create a new member
    @PostMapping
    public ResponseEntity<ApiResponseDTO<MemberDTO>> createMember(@Valid @RequestBody MemberDTO memberDTO) {
        try {
            MemberDTO createdMember = memberService.createMember(memberDTO);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponseDTO.success("Member created successfully", createdMember));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponseDTO.error("Failed to create member: " + e.getMessage()));
        }
    }

    // Get all members
    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<MemberDTO>>> getAllMembers() {
        try {
            List<MemberDTO> members = memberService.getAllMembers();
            return ResponseEntity.ok(ApiResponseDTO.success("Members retrieved successfully", members));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDTO.error("Failed to retrieve members: " + e.getMessage()));
        }
    }

    // Get member by ID
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<MemberDTO>> getMemberById(@PathVariable Long id) {
        try {
            Optional<MemberDTO> member = memberService.getMemberById(id);
            if (member.isPresent()) {
                return ResponseEntity.ok(ApiResponseDTO.success("Member found", member.get()));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponseDTO.error("Member not found with ID: " + id));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDTO.error("Failed to retrieve member: " + e.getMessage()));
        }
    }

    // Get member by ID with borrowed books
    @GetMapping("/{id}/borrowed-books")
    public ResponseEntity<ApiResponseDTO<MemberDTO>> getMemberByIdWithBorrowedBooks(@PathVariable Long id) {
        try {
            Optional<MemberDTO> member = memberService.getMemberByIdWithBorrowedBooks(id);
            if (member.isPresent()) {
                return ResponseEntity.ok(ApiResponseDTO.success("Member with borrowed books found", member.get()));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponseDTO.error("Member not found with ID: " + id));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDTO.error("Failed to retrieve member with borrowed books: " + e.getMessage()));
        }
    }

    // Update member
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<MemberDTO>> updateMember(
            @PathVariable Long id, @Valid @RequestBody MemberDTO memberDTO) {
        try {
            MemberDTO updatedMember = memberService.updateMember(id, memberDTO);
            if (updatedMember != null) {
                return ResponseEntity.ok(ApiResponseDTO.success("Member updated successfully", updatedMember));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponseDTO.error("Member not found with ID: " + id));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponseDTO.error("Failed to update member: " + e.getMessage()));
        }
    }

    // Delete member
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<Void>> deleteMember(@PathVariable Long id) {
        try {
            boolean deleted = memberService.deleteMember(id);
            if (deleted) {
                return ResponseEntity.ok(ApiResponseDTO.success("Member deleted successfully", null));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponseDTO.error("Member not found with ID: " + id));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDTO.error("Failed to delete member: " + e.getMessage()));
        }
    }

    // Search members
    @GetMapping("/search")
    public ResponseEntity<ApiResponseDTO<List<MemberDTO>>> searchMembers(@RequestParam String searchTerm) {
        try {
            List<MemberDTO> members = memberService.searchMembers(searchTerm);
            return ResponseEntity.ok(ApiResponseDTO.success("Members found", members));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDTO.error("Failed to search members: " + e.getMessage()));
        }
    }

    // Get member by email
    @GetMapping("/email/{email}")
    public ResponseEntity<ApiResponseDTO<MemberDTO>> getMemberByEmail(@PathVariable String email) {
        try {
            Optional<MemberDTO> member = memberService.getMemberByEmail(email);
            if (member.isPresent()) {
                return ResponseEntity.ok(ApiResponseDTO.success("Member found by email", member.get()));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponseDTO.error("Member not found with email: " + email));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDTO.error("Failed to retrieve member by email: " + e.getMessage()));
        }
    }
}