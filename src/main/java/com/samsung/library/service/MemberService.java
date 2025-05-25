package com.samsung.library.service;

import com.samsung.library.dto.BorrowedBookSummaryDTO;
import com.samsung.library.dto.MemberDTO;
import com.samsung.library.model.Member;
import com.samsung.library.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class MemberService {

    @Autowired
    private MemberRepository memberRepository;

    // Create a new member
    public MemberDTO createMember(MemberDTO memberDTO) {
        // Check if email already exists
        if (memberRepository.findByEmail(memberDTO.getEmail()).isPresent()) {
            throw new RuntimeException("Member with email " + memberDTO.getEmail() + " already exists");
        }

        Member member = convertToEntity(memberDTO);
        Member savedMember = memberRepository.save(member);
        return convertToDTO(savedMember);
    }

    // Get member by ID
    @Transactional(readOnly = true)
    public Optional<MemberDTO> getMemberById(Long id) {
        return memberRepository.findById(id)
                .map(this::convertToDTO);
    }

    // Get member by ID with borrowed books
    @Transactional(readOnly = true)
    public Optional<MemberDTO> getMemberByIdWithBorrowedBooks(Long id) {
        return memberRepository.findByIdWithBorrowedBooks(id)
                .map(this::convertToDTOWithBorrowedBooks);
    }

    // Get all members
    @Transactional(readOnly = true)
    public List<MemberDTO> getAllMembers() {
        return memberRepository.findAll().stream()
                .map(this::convertToDTOWithBorrowedBooks)
                .collect(Collectors.toList());
    }

    // Update member
    public MemberDTO updateMember(Long id, MemberDTO memberDTO) {
        Optional<Member> existingMember = memberRepository.findById(id);
        if (existingMember.isPresent()) {
            Member member = existingMember.get();

            // Check if email is being changed and if it already exists
            if (!member.getEmail().equals(memberDTO.getEmail())) {
                if (memberRepository.findByEmail(memberDTO.getEmail()).isPresent()) {
                    throw new RuntimeException("Member with email " + memberDTO.getEmail() + " already exists");
                }
            }

            member.setName(memberDTO.getName());
            member.setEmail(memberDTO.getEmail());
            member.setPhone(memberDTO.getPhone());
            member.setAddress(memberDTO.getAddress());
            member.setMembershipStatus(memberDTO.getMembershipStatus());

            Member updatedMember = memberRepository.save(member);
            return convertToDTO(updatedMember);
        }
        return null;
    }

    // Delete member
    public boolean deleteMember(Long id) {
        if (memberRepository.existsById(id)) {
            memberRepository.deleteById(id);
            return true;
        }
        return false;
    }

    // Search members
    @Transactional(readOnly = true)
    public List<MemberDTO> searchMembers(String searchTerm) {
        return memberRepository.searchMembers(searchTerm).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Get member by email
    @Transactional(readOnly = true)
    public Optional<MemberDTO> getMemberByEmail(String email) {
        return memberRepository.findByEmail(email)
                .map(this::convertToDTO);
    }

    // Convert entity to DTO
    private MemberDTO convertToDTO(Member member) {
        MemberDTO dto = new MemberDTO();
        dto.setId(member.getId());
        dto.setName(member.getName());
        dto.setEmail(member.getEmail());
        dto.setPhone(member.getPhone());
        dto.setAddress(member.getAddress());
        dto.setMembershipDate(member.getMembershipDate());
        dto.setMembershipStatus(member.getMembershipStatus());
        dto.setCreatedAt(member.getCreatedAt());
        dto.setUpdatedAt(member.getUpdatedAt());
        return dto;
    }

    // Convert entity to DTO with borrowed books
    private MemberDTO convertToDTOWithBorrowedBooks(Member member) {
        MemberDTO dto = convertToDTO(member);
        if (member.getBorrowedBooks() != null) {
            List<BorrowedBookSummaryDTO> borrowedBookSummaries = member.getBorrowedBooks().stream()
                    .map(bb -> new BorrowedBookSummaryDTO(
                            bb.getId(),
                            bb.getBook().getTitle(),
                            bb.getBook().getAuthor().getName(),
                            bb.getBorrowDate(),
                            bb.getDueDate(),
                            bb.getReturnDate(),
                            bb.getStatus(),
                            bb.getFineAmount()
                    ))
                    .collect(Collectors.toList());
            dto.setBorrowedBooks(borrowedBookSummaries);
        }
        return dto;
    }

    // Convert DTO to entity
    private Member convertToEntity(MemberDTO dto) {
        Member member = new Member();
        member.setName(dto.getName());
        member.setEmail(dto.getEmail());
        member.setPhone(dto.getPhone());
        member.setAddress(dto.getAddress());
        member.setMembershipStatus(dto.getMembershipStatus());
        return member;
    }
}