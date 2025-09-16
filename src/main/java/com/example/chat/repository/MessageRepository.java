package com.example.chat.repository;

import com.example.chat.model.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    
    Page<Message> findByConversationIdAndDeletedFalseOrderByCreatedAtDesc(Long conversationId, Pageable pageable);
    
    @Query("SELECT m FROM Message m WHERE m.conversation.id = :conversationId AND m.deleted = false AND m.id > :afterMessageId ORDER BY m.createdAt ASC")
    List<Message> findNewMessages(@Param("conversationId") Long conversationId, @Param("afterMessageId") Long afterMessageId);
    
    @Query("SELECT COUNT(m) FROM Message m WHERE m.conversation.id = :conversationId AND m.id > :lastReadMessageId AND m.sender.id != :userId AND m.deleted = false")
    long countUnreadMessages(@Param("conversationId") Long conversationId, @Param("lastReadMessageId") Long lastReadMessageId, @Param("userId") Long userId);
    
    void deleteByConversationId(Long conversationId);
    
    @Query("SELECT m FROM Message m WHERE m.conversation.id = :conversationId AND m.sender.id != :userId AND m.status != 'READ' AND m.deleted = false")
    List<Message> findUnreadMessagesByConversationAndNotSender(@Param("conversationId") Long conversationId, @Param("userId") Long userId);
}