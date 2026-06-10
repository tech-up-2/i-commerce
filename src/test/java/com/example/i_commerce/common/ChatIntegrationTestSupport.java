package com.example.i_commerce.common;

import com.example.i_commerce.domain.chat.entity.ChatParticipant;
import com.example.i_commerce.domain.chat.entity.ChatRoom;
import com.example.i_commerce.domain.chat.repository.ChatMessageRepository;
import com.example.i_commerce.domain.chat.repository.ChatParticipantRepository;
import com.example.i_commerce.domain.chat.repository.ChatRoomRepository;
import com.example.i_commerce.domain.member.entity.Member;
import com.example.i_commerce.domain.member.entity.enums.Gender;
import com.example.i_commerce.domain.member.entity.enums.MemberType;
import com.example.i_commerce.global.security.jwt.JwtTokenUtil;
import com.example.i_commerce.global.security.jwt.TokenPayload;
import com.example.i_commerce.global.security.principal.CustomUserPrincipal.PrincipalType;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class ChatIntegrationTestSupport extends IntegrationTestSupport {
    @Autowired protected ChatRoomRepository chatRoomRepository;
    @Autowired protected ChatParticipantRepository chatParticipantRepository;
    @Autowired protected ChatMessageRepository chatMessageRepository;
    @Autowired protected JwtTokenUtil jwtTokenUtil;
    @AfterEach
    void tearDown() {
        chatMessageRepository.deleteAllInBatch();
        chatParticipantRepository.deleteAllInBatch();
        chatRoomRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
    }
    protected ChatTestSet saveDefaultChatTestSet() {

        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
        // 구매자 생성
        Member member = memberRepository.save(
            Member.builder()
                .name(dataEncryptor.encrypt("테스트회원"))
                .phoneNumber(dataEncryptor.encrypt("01011111111"))
                .emailHash("customer" + uniqueId)
                .emailEncrypted(dataEncryptor.encrypt("customer@test.com"))
                .password("password")
                .sex(Gender.MALE)
                .birthday(dataEncryptor.encrypt("20000101"))
                .role(MemberType.CUSTOMER)
                .build()
        );

        // 판매자 Member 생성
        Member sellerMember = memberRepository.save(
            Member.builder()
                .name(dataEncryptor.encrypt("테스트판매자"))
                .phoneNumber(dataEncryptor.encrypt("01022222222"))
                .emailHash("seller" + uniqueId)
                .emailEncrypted(dataEncryptor.encrypt("seller@test.com"))
                .password("password")
                .sex(Gender.MALE)
                .birthday(dataEncryptor.encrypt("20010101"))
                .role(MemberType.SELLER)
                .isSeller(true)
                .build()
        );

        ChatRoom chatRoom = chatRoomRepository.save(
            ChatRoom.builder()
                .name("테스트 채팅방")
                .roomKey(
                ChatRoom.generateRoomKey(
                    member.getId(),
                    sellerMember.getId()
                )
            )
                .isGroupChat(false)
                .build()
        );
        chatParticipantRepository.save(
            ChatParticipant.builder()
                .chatRoom(chatRoom)
                .memberId(member.getId())
                .isBan(false)
                .build()
        );
        chatParticipantRepository.save(
            ChatParticipant.builder()
                .chatRoom(chatRoom)
                .memberId(sellerMember.getId())
                .isBan(false)
                .build()
        );


        return new ChatTestSet(
            member,
            sellerMember,
            chatRoom
        );

    }
    protected  String createMemberToken(Member member)
    {
        return jwtTokenUtil.createToken(
            new TokenPayload(
                PrincipalType.MEMBER,
                member.getId(),
                member.getRole(),
                member.getStatus(),
                null
            )
        );
    }
    protected  String createSellerToken(Member sellerMember)
    {
        return jwtTokenUtil.createToken(
            new TokenPayload(
                PrincipalType.MEMBER,
                sellerMember.getId(),
                sellerMember.getRole(),
                sellerMember.getStatus(),
                null
            )
        );
    }


    protected record ChatTestSet(
        Member customer,
        Member sellerMember,
        ChatRoom room
    ) {}

}
