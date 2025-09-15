package com.example.chat.repository;

import com.example.chat.model.ConversationMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationMemberRepository extends JpaRepository<ConversationMember, Long> {
    
    Optional<ConversationMember> findByConversationIdAndUserId(Long conversationId, Long userId);
    
    List<ConversationMember> findByConversationId(Long conversationId);
    
    @Query("SELECT cm FROM ConversationMember cm WHERE cm.conversation.id = :conversationId AND cm.user.id != :excludeUserId")
    List<ConversationMember> findOtherMembers(@Param("conversationId") Long conversationId, @Param("excludeUserId") Long excludeUserId);
    
    boolean existsByConversationIdAndUserId(Long conversationId, Long userId);
}