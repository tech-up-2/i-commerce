package com.example.i_commerce.domain.chat.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.example.i_commerce.domain.chat.entity.ChatMessage;
import com.example.i_commerce.domain.chat.entity.ChatParticipant;
import com.example.i_commerce.domain.chat.entity.ChatRoom;
import com.example.i_commerce.domain.chat.entity.MessageReadStatus;
import com.example.i_commerce.domain.chat.exception.ChatErrorCode;
import com.example.i_commerce.domain.chat.repository.ChatMessageRepository;
import com.example.i_commerce.domain.chat.repository.ChatParticipantRepository;
import com.example.i_commerce.domain.chat.repository.ChatRoomRepository;
import com.example.i_commerce.domain.chat.repository.ChatStatusRepository;
import com.example.i_commerce.domain.chat.service.dto.ChatMessageSendRequest;
import com.example.i_commerce.domain.chat.service.dto.ChatMessageSendResponse;
import com.example.i_commerce.domain.chat.service.dto.GroupChatListResponse;
import com.example.i_commerce.domain.chat.service.dto.MyChatListResponse;
import com.example.i_commerce.domain.chat.service.fixture.ChatMemberFixture;
import com.example.i_commerce.domain.chat.service.fixture.ChatRoomFixture;
import com.example.i_commerce.domain.chat.util.ChatHealthCheck;
import com.example.i_commerce.domain.member.entity.Member;
import com.example.i_commerce.domain.member.repository.MemberRepository;
import com.example.i_commerce.domain.member.service.member.MemberService;
import com.example.i_commerce.domain.member.service.member.dto.MemberChatInfo;
import com.example.i_commerce.global.common.response.ApiResponse;
import com.example.i_commerce.global.exception.AppException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
public class ChatServiceScenarioTest {

    @InjectMocks
    private ChatService chatService;

    @Mock
    private ChatMessageRepository chatMessageRepository;
    @Mock
    private ChatRoomRepository chatRoomRepository;
    @Mock
    private ChatParticipantRepository chatParticipantRepository;
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private ChatStatusRepository chatStatusRepository;
    @Mock
    private MemberService memberService;

    @Spy
    private ChatHealthCheck chatRoleChecker = new ChatHealthCheck();

    private Member member;
    private Member otherMember;
    private ChatRoom chatRoom;
    private ChatRoom singleChatRoom;
    private ChatRoom groupChatRoom;
    private ChatRoom groupChatRoom2;
    private MemberChatInfo memberChatInfo;
    private MemberChatInfo otherChatInfo;

    @BeforeEach
    public void setUp() {
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(ChatMemberFixture.createPrincipal(), null,
                List.of())
        );
        member = ChatMemberFixture.createMember(1L, "user1@naver.com");
        otherMember = ChatMemberFixture.createSeller(2L, "seller1@naver.com");
        memberChatInfo = new MemberChatInfo(member.getId(), "user1", member.getRole(),
            member.getStatus());
        otherChatInfo = new MemberChatInfo
            (otherMember.getId(), "seller1", otherMember.getRole(), otherMember.getStatus());
        singleChatRoom = ChatRoomFixture.createChatPrivateRoom(1L, "1:1 테스트 채팅방");
        groupChatRoom = ChatRoomFixture.createChatGroupRoom(1L, "상품 그룹 테스트 채팅방", 1L);
        groupChatRoom2 = ChatRoomFixture.createChatGroupRoom(2L, "상품 그룹2 테스트 채팅방", 2L);
    }

    @Test
    @DisplayName("시나리오 1 [성공]: 안 읽은 메시지가 있을 때 정상적으로 해당 내용들을 읽음 처리 할 수 있습니다.")
    void messageRead_Success() {
        Long myMemberId = member.getId();
        Long RoomId = groupChatRoom.getId();
        when(chatRoomRepository.findById(RoomId)).thenReturn(Optional.of(groupChatRoom));
        MessageReadStatus status = MessageReadStatus.builder()
            .isRead(false)
            .build();
        when(
            chatStatusRepository.findByChatRoomAndMemberIdAndIsReadFalse(groupChatRoom, myMemberId))
            .thenReturn(List.of(status));

        ApiResponse response = chatService.messageRead(RoomId);
        Assertions.assertEquals(response.code(), "SUCCESS");
    }

    @Test
    @DisplayName("시나리오 2 [예외]: 존재하지 않는 채팅방에 읽음 처리를 요청할 수 없습니다.")
    void messageRead_Fail_RoomNotFound() {
        Long myMemberId = member.getId();
        Long RoomId = groupChatRoom.getId();
        when(chatRoomRepository.findById(RoomId)).thenReturn(Optional.empty());
        AppException exception = Assertions.assertThrows(AppException.class,
            () -> chatService.messageRead(RoomId));
        Assertions.assertEquals(exception.getErrorCode(), ChatErrorCode.CHAT_ROOM_NOT_FOUND);
    }

    @Test
    @DisplayName("시나리오 3 [성공]: 정상적으로 개설된 그룹채팅방 목록을 조회할 수 있습니다.")
    void groupRoomList_Read_Success() {
        when(chatRoomRepository.findByIsGroupChatTrueAndDeletedAtIsNull())
            .thenReturn(List.of(groupChatRoom, groupChatRoom2));
        ApiResponse<List<GroupChatListResponse>> response = chatService.getGroupChatList();
        Assertions.assertEquals(response.code(), "SUCCESS");
        Assertions.assertNotNull(response.data());
        Assertions.assertEquals(2, response.data().size());
    }

    @Test
    @DisplayName("시나리오 4 [성공]: 정상적으로 내 채팅 목록을 조회할 수 있습니다.")
    void myChatList_Read_Success() {
        Long myMemberId = member.getId();
        when(memberService.getMemberChatInfo(myMemberId)).thenReturn(memberChatInfo);
        MyChatListResponse mockResponse = new MyChatListResponse(myMemberId, "테스트 상품 그룹채팅방1", true,
            3L);
        when(chatStatusRepository.findMyChatList(myMemberId)).thenReturn(List.of(mockResponse));
        ApiResponse<List<MyChatListResponse>> response = chatService.getMyChatList();
        Assertions.assertEquals(response.code(), "SUCCESS");
        Assertions.assertNotNull(response.data());
        Assertions.assertEquals(1, response.data().size());
    }

    @Test
    @DisplayName("시나리오 5 [성공]: 정상적으로 채팅이 저장됩니다.")
    void saveMessage_Success() {
        Long myMemberId = member.getId();
        Long otherMemberId = otherMember.getId();
        Long RoomId = singleChatRoom.getId();
        ChatMessageSendRequest chatMessageSendRequest = new ChatMessageSendRequest("안녕하세요 테스트 메시지입니다.", myMemberId);

        when(chatRoomRepository.findById(RoomId)).thenReturn(Optional.of(singleChatRoom));
        when(memberService.getMemberChatInfo(myMemberId)).thenReturn(memberChatInfo);
        ChatParticipant participant = ChatParticipant.builder()
            .id(1L)
            .chatRoom(singleChatRoom)
            .memberId(myMemberId)
            .build();
        ChatParticipant otherParticipant = ChatParticipant.builder()
            .id(2L)
            .chatRoom(singleChatRoom)
            .memberId(otherMemberId)
            .build();
        when(chatParticipantRepository.findByChatRoom(singleChatRoom)).thenReturn(List.of(participant, otherParticipant));

        ApiResponse<Void> response = chatService.saveMessage(RoomId, chatMessageSendRequest);
        Assertions.assertEquals(response.code(), "SUCCESS");

    }
    @Test
    @DisplayName("시나리오 6 [성공]: 정상적으로 채팅 내용을 불러올수 있습니다.")
    void read_Message_Success() {
        Long myMemberId = member.getId();
        Long chatRoomId = groupChatRoom.getId();
        when(chatRoomRepository.findById(chatRoomId)).thenReturn(Optional.of(groupChatRoom));
        ChatParticipant participant = ChatParticipant.builder()
            .id(1L)
            .chatRoom(groupChatRoom)
            .memberId(myMemberId)
            .build();
        when(chatParticipantRepository.findByChatRoom(groupChatRoom)).thenReturn(List.of(participant));
        ChatMessage message = ChatMessage.builder()
            .id(1L)
            .memberId(myMemberId)
            .chatRoom(groupChatRoom)
            .content("안녕하세요 메시지 조회 테스트입니다.")
            .isBlind(false)
            .build();
        ChatMessage message2 = ChatMessage.builder()
            .id(2L)
            .memberId(2L)
            .chatRoom(groupChatRoom)
            .content("비속어")
            .isBlind(true)
            .build();
        when(chatMessageRepository.findByChatRoomOrderByCreatedAtAsc(groupChatRoom)).thenReturn(List.of(message, message2));

        ApiResponse<List<ChatMessageSendResponse>> response = chatService.getChatHistory(chatRoomId);
        Assertions.assertEquals(response.code(), "SUCCESS");
        Assertions.assertNotNull(response.data());
        Assertions.assertEquals(2, response.data().size());
        Assertions.assertEquals(message.getContent(), response.data().get(0).message());
        Assertions.assertEquals(response.data().get(1).message(), "관리자에 의해 가려진 메시지입니다");

    }
    @Test
    @DisplayName("시나리오 8 [성공]: 유저가 해당 채팅방의 참여자가 맞으면 true를 반환합니다.")
    void isRoomParticipant_True() {
        Long roomId = singleChatRoom.getId();
        Long myId = member.getId();

        when(chatRoomRepository.findById(roomId)).thenReturn(Optional.of(singleChatRoom));

        ChatParticipant participant = ChatParticipant.builder().build();
        when(chatParticipantRepository.findByChatRoomAndMemberId(singleChatRoom, myId))
            .thenReturn(Optional.of(participant));

        Boolean result = chatService.isRoomParticipant(myId, roomId);

        Assertions.assertTrue(result);
    }

    @Test
    @DisplayName("시나리오 9 [성공]: 유저가 해당 채팅방의 참여자가 아니면 false를 반환합니다.")
    void isRoomParticipant_False() {

        Long roomId = singleChatRoom.getId();
        Long myId = member.getId();

        when(chatRoomRepository.findById(roomId)).thenReturn(Optional.of(singleChatRoom));

        when(chatParticipantRepository.findByChatRoomAndMemberId(singleChatRoom, myId))
            .thenReturn(Optional.empty());

        Boolean result = chatService.isRoomParticipant(myId, roomId);

        Assertions.assertFalse(result);
    }
    @Test
    @DisplayName("시나리오 10 [예외]: 존재하지 않는 채팅방에 참여 여부를 조회합니다.")
    void isRoomParticipant_Fail_NotFound() {
        Long roomId = singleChatRoom.getId();
        Long myMemberId = member.getId();
        when(chatRoomRepository.findById(roomId)).thenReturn(Optional.empty());

        AppException exception = Assertions.assertThrows(AppException.class,
            () -> chatService.isRoomParticipant(myMemberId, roomId));
        Assertions.assertEquals(exception.getErrorCode(), ChatErrorCode.CHAT_ROOM_NOT_FOUND);
    }
    @Test
    @DisplayName("시나리오 11 [예외]: 존재하지 않는 채팅방에 메시지 저장 요청할 수 없습니다.")
    void messageSave_Fail_RoomNotFound() {
        Long RoomId = groupChatRoom.getId();
        when(chatRoomRepository.findById(RoomId)).thenReturn(Optional.empty());
        ChatMessageSendRequest request = new ChatMessageSendRequest("안녕하세요", member.getId());
        AppException exception = Assertions.assertThrows(AppException.class,
            () -> chatService.saveMessage(RoomId, request));
        Assertions.assertEquals(exception.getErrorCode(), ChatErrorCode.CHAT_ROOM_NOT_FOUND);
    }

}