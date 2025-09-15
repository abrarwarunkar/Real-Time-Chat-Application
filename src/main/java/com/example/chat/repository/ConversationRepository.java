package com.example.chat.repository;

import com.example.chat.model.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    
    @Query("SELECT c FROM Conversation c JOIN c.members m WHERE m.user.id = :userId ORDER BY c.updatedAt DESC")
    List<Conversation> findByUserId(@Param("userId") Long userId);
    
    @Query("SELECT c FROM Conversation c WHERE c.type = 'DIRECT' AND " +
           "EXISTS (SELECT 1 FROM ConversationMember m1 WHERE m1.conversation = c AND m1.user.id = :user1Id) AND " +
           "EXISTS (SELECT 1 FROM ConversationMember m2 WHERE m2.conversation = c AND m2.user.id = :user2Id)")
    Optional<Conversation> findDirectConversation(@Param("user1Id") Long user1Id, @Param("user2Id") Long user2Id);
}