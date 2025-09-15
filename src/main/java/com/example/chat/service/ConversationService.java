package com.example.chat.service;

import com.example.chat.dto.ConversationDto;
import com.example.chat.dto.MessageDto;
import com.example.chat.dto.UserDto;
import com.example.chat.model.Conversation;
import com.example.chat.model.ConversationMember;
import com.example.chat.model.Message;
import com.example.chat.model.User;
import com.example.chat.repository.ConversationMemberRepository;
import com.example.chat.repository.ConversationRepository;
import com.example.chat.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ConversationService {

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private ConversationMemberRepository memberRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserService userService;

    @Transactional
    public ConversationDto createDirectConversation(Long user1Id, Long user2Id) {
        // Check if direct conversation already exists
        Optional<Conversation> existing = conversationRepository.findDirectConversation(user1Id, user2Id);
        if (existing.isPresent()) {
            return convertToDto(existing.get(), user1Id);
        }

        Conversation conversation = new Conversation();
        conversation.setType(Conversation.Type.DIRECT);
        conversation = conversationRepository.save(conversation);

        // Add members
        User user1 = userService.findById(user1Id).orElseThrow(() -> new RuntimeException("User not found"));
        User user2 = userService.findById(user2Id).orElseThrow(() -> new RuntimeException("User not found"));

        memberRepository.save(new ConversationMember(conversation, user1));
        memberRepository.save(new ConversationMember(conversation, user2));

        return convertToDto(conversation, user1Id);
    }

    @Transactional
    public ConversationDto createGroupConversation(String name, Long creatorId, List<Long> memberIds) {
        Conversation conversation = new Conversation();
        conversation.setType(Conversation.Type.GROUP);
        conversation.setName(name);
        conversation = conversationRepository.save(conversation);

        // Add creator as admin
        User creator = userService.findById(creatorId).orElseThrow(() -> new RuntimeException("User not found"));
        ConversationMember creatorMember = new ConversationMember(conversation, creator);
        creatorMember.setRole(ConversationMember.Role.ADMIN);
        memberRepository.save(creatorMember);

        // Add other members
        for (Long memberId : memberIds) {
            if (!memberId.equals(creatorId)) {
                User member = userService.findById(memberId).orElseThrow(() -> new RuntimeException("User not found"));
                memberRepository.save(new ConversationMember(conversation, member));
            }
        }

        return convertToDto(conversation, creatorId);
    }

    public List<ConversationDto> getUserConversations(Long userId) {
        List<Conversation> conversations = conversationRepository.findByUserId(userId);
        return conversations.stream()
                .map(conv -> convertToDto(conv, userId))
                .collect(Collectors.toList());
    }

    public Optional<ConversationDto> getConversation(Long conversationId, Long userId) {
        if (!memberRepository.existsByConversationIdAndUserId(conversationId, userId)) {
            return Optional.empty();
        }

        return conversationRepository.findById(conversationId)
                .map(conv -> convertToDto(conv, userId));
    }

    public Page<MessageDto> getConversationMessages(Long conversationId, Long userId, Pageable pageable) {
        if (!memberRepository.existsByConversationIdAndUserId(conversationId, userId)) {
            throw new RuntimeException("Access denied");
        }

        return messageRepository.findByConversationIdAndDeletedFalseOrderByCreatedAtDesc(conversationId, pageable)
                .map(MessageDto::new);
    }

    @Transactional
    public void addMemberToGroup(Long conversationId, Long userId, Long newMemberId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        if (conversation.getType() != Conversation.Type.GROUP) {
            throw new RuntimeException("Can only add members to group conversations");
        }

        ConversationMember requester = memberRepository.findByConversationIdAndUserId(conversationId, userId)
                .orElseThrow(() -> new RuntimeException("Access denied"));

        if (requester.getRole() != ConversationMember.Role.ADMIN) {
            throw new RuntimeException("Only admins can add members");
        }

        if (memberRepository.existsByConversationIdAndUserId(conversationId, newMemberId)) {
            throw new RuntimeException("User is already a member");
        }

        User newMember = userService.findById(newMemberId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        memberRepository.save(new ConversationMember(conversation, newMember));
    }

    @Transactional
    public void markAsRead(Long conversationId, Long userId, Long messageId) {
        ConversationMember member = memberRepository.findByConversationIdAndUserId(conversationId, userId)
                .orElseThrow(() -> new RuntimeException("Access denied"));

        member.setLastReadMessageId(messageId);
        memberRepository.save(member);
    }

    private ConversationDto convertToDto(Conversation conversation, Long currentUserId) {
        ConversationDto dto = new ConversationDto(conversation);

        // Set members
        List<UserDto> members = memberRepository.findByConversationId(conversation.getId()).stream()
                .map(member -> {
                    UserDto userDto = new UserDto(member.getUser());
                    userDto.setOnline(userService.isUserOnline(member.getUser().getId()));
                    return userDto;
                })
                .collect(Collectors.toList());
        dto.setMembers(members);

        // Set display name for direct conversations
        if (conversation.getType() == Conversation.Type.DIRECT && members.size() == 2) {
            String otherUserName = members.stream()
                    .filter(member -> !member.getId().equals(currentUserId))
                    .findFirst()
                    .map(UserDto::getUsername)
                    .orElse("Unknown");
            dto.setName(otherUserName);
        }

        // Set last message
        messageRepository.findByConversationIdAndDeletedFalseOrderByCreatedAtDesc(
                conversation.getId(), Pageable.ofSize(1))
                .getContent().stream()
                .findFirst()
                .ifPresent(message -> dto.setLastMessage(new MessageDto(message)));

        // Set unread count
        ConversationMember member = memberRepository.findByConversationIdAndUserId(conversation.getId(), currentUserId)
                .orElse(null);
        if (member != null) {
            Long lastReadMessageId = member.getLastReadMessageId();
            if (lastReadMessageId != null) {
                long unreadCount = messageRepository.countUnreadMessages(conversation.getId(), lastReadMessageId, currentUserId);
                dto.setUnreadCount(unreadCount);
            }
        }

        return dto;
    }
}