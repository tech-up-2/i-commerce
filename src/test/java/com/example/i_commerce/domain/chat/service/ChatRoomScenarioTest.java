package com.example.i_commerce.domain.chat.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.example.i_commerce.domain.chat.entity.ChatParticipant;
import com.example.i_commerce.domain.chat.entity.ChatRoom;
import com.example.i_commerce.domain.chat.exception.ChatErrorCode;
import com.example.i_commerce.domain.chat.repository.ChatParticipantRepository;
import com.example.i_commerce.domain.chat.repository.ChatRoomRepository;
import com.example.i_commerce.domain.chat.service.fixture.ChatMemberFixture;
import com.example.i_commerce.domain.chat.service.fixture.ChatRoomFixture;
import com.example.i_commerce.domain.chat.util.ChatHealthCheck;
import com.example.i_commerce.domain.chat.util.ChatRoomNameGenerator;
import com.example.i_commerce.domain.member.entity.Member;
import com.example.i_commerce.domain.member.repository.MemberRepository;
import com.example.i_commerce.domain.member.service.member.MemberService;
import com.example.i_commerce.domain.member.service.member.dto.MemberChatInfo;
import com.example.i_commerce.domain.product.entity.Category;
import com.example.i_commerce.domain.product.entity.Product;
import com.example.i_commerce.domain.product.repository.ProductRepository;
import com.example.i_commerce.global.common.response.ApiResponse;
import com.example.i_commerce.global.exception.AppException;
import com.example.i_commerce.global.security.principal.CustomUserPrincipal;
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
import org.testcontainers.shaded.org.checkerframework.checker.units.qual.A;

@ExtendWith(MockitoExtension.class)
public class ChatRoomScenarioTest {

    @InjectMocks
    ChatRoomService chatRoomService;
    @Mock
    ChatRoomRepository chatRoomRepository;
    @Mock
    MemberService memberService;
    @Mock
    ChatParticipantRepository chatParticipantRepository;
    @Mock
    ChatRoomNameGenerator chatRoomNameGenerator;
    @Mock //Product Service 메서드가 완성되기 전까지 사용하는 임시 Repository
    ProductRepository productRepository;
    @Spy
    private ChatHealthCheck chatRoleChecker = new ChatHealthCheck();

    private Member member;
    private Member member2;
    private Member member3;
    private Member member4;
    private ChatRoom singleChatRoom;
    private ChatRoom groupChatRoom;
    private CustomUserPrincipal customUserPrincipal;
    private Product product;
    private Category category;

    private MemberChatInfo memberChatInfo;
    private MemberChatInfo memberChatInfo2;
    private MemberChatInfo otherChatInfo;
    private String roomKey;

    @BeforeEach
    public void setUp() {
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(ChatMemberFixture.createPrincipal(), null,
                List.of())
        );
        member = ChatMemberFixture.createMember(1L, "user1@naver.com");
        member2 = ChatMemberFixture.createMember(2L, "user2@naver.com");
        member3 = ChatMemberFixture.createSeller(3L, "seller1@naver.com");
        member4 = ChatMemberFixture.createSeller(4L, "seller4@naver.com");
        memberChatInfo = new MemberChatInfo
            (member.getId(), "user1", member.getRole(), member.getStatus());
        memberChatInfo2 = new MemberChatInfo
            (member2.getId(), "user2", member2.getRole(), member2.getStatus());
        otherChatInfo = new MemberChatInfo
            (member3.getId(), "seller1", member3.getRole(), member3.getStatus());
        category = ChatRoomFixture.createCategory(1L);
        product = ChatRoomFixture.createProduct(1L, 3L, category);
        singleChatRoom = ChatRoomFixture.createChatPrivateRoom(1L, "1:1 테스트 채팅방");
        groupChatRoom = ChatRoomFixture.createChatGroupRoom(1L, "상품 그룹 테스트 채팅방", 1L);

        roomKey = ChatRoom.generateRoomKey(member.getId(), member3.getId());

    }

    @Test
    @DisplayName("시나리오 1 [성공]: 기존 대화하는 채팅방이 없는 구매자와 판매자가 처음으로 1:1 대화를 시작할 때")
    void getCreatePrivateRoom_Success() {
        //      Given
        Long myMemberId = 1L;
        Long otherMemberId = member3.getId();
        String PrivateRoomName = "user1이 요청한 seller1님과의 채팅";

        //      When
        when(memberService.getMemberChatInfo(myMemberId)).thenReturn(memberChatInfo);
        when(memberService.getMemberChatInfo(otherMemberId)).thenReturn(otherChatInfo);

        when(chatRoomRepository.findByRoomKey(roomKey)).thenReturn(Optional.empty());
        when(chatRoomNameGenerator.getPrivateRoomName(any(), any())).thenReturn(PrivateRoomName);
        when(chatRoomRepository.save(any())).thenReturn(singleChatRoom);

        ApiResponse<Long> response = chatRoomService.getOrCreatePrivateRoom(otherMemberId);
        //      Then
        Assertions.assertEquals(response.code(), "SUCCESS");

    }

    @Test
    @DisplayName("시나리오 2 [성공]: 이미 대화 중인 1:1 채팅방이 존재하면 기존 방 ID를 반환")
    void getCreatePrivateRoom_Success_ReturnId() {
        Long myMemberId = 1L;
        Long otherMemberId = member3.getId();

        when(memberService.getMemberChatInfo(myMemberId)).thenReturn(memberChatInfo);
        when(memberService.getMemberChatInfo(otherMemberId)).thenReturn(otherChatInfo);
        when(chatRoomRepository.findByRoomKey(roomKey)).thenReturn(Optional.of(singleChatRoom));

        ApiResponse<Long> response = chatRoomService.getOrCreatePrivateRoom(otherMemberId);

        Assertions.assertEquals(response.code(), "SUCCESS");
        Assertions.assertEquals(response.data(), singleChatRoom.getId());
    }

    @Test
    @DisplayName("시나리오 3 [예외]: 유저가 유저에게 채팅을 요청할 시 에러 메시지를 반환합니다.")
    void getCreatePrivateRoom_Fail_EqualRole() {
        Long myMemberId = 1L;
        Long otherMemberId = member2.getId();
        when(memberService.getMemberChatInfo(myMemberId)).thenReturn(memberChatInfo);
        when(memberService.getMemberChatInfo(otherMemberId)).thenReturn(memberChatInfo2);

        AppException exception = Assertions.assertThrows(AppException.class,
            () -> {
                chatRoomService.getOrCreatePrivateRoom(otherMemberId);
            });
        Assertions.assertEquals(exception.getErrorCode(), ChatErrorCode.CANNOT_CHAT_SAME_ROLE);
    }

    @Test
    @DisplayName("시나리오 4 [예외]: 나 자신에게 채팅을 요청시 에러 메시지를 반환합니다.")
    void getCreatePrivateRoom_Fail_EqualMember() {
        Long myMemberId = 1L;
        when(memberService.getMemberChatInfo(myMemberId)).thenReturn(memberChatInfo);

        AppException exception = Assertions.assertThrows(AppException.class,
            () -> {
                chatRoomService.getOrCreatePrivateRoom(myMemberId);
            });
        Assertions.assertEquals(exception.getErrorCode(), ChatErrorCode.CANNOT_REQUEST_TO_SELF);
    }

    @Test
    @DisplayName("시나리오 5 [예외]: 1:1 채팅방은 퇴장할 수 없습니다. 요청시 에러 메시지를 반환합니다.")
    void leavePrivateRoom_Fail() {
        Long myMemberId = 1L;
        Long privateRoomId = singleChatRoom.getId();
        when(chatRoomRepository.findById(privateRoomId)).thenReturn(Optional.of(singleChatRoom));
        when(memberService.getMemberChatInfo(myMemberId)).thenReturn(memberChatInfo);

        AppException exception = Assertions.assertThrows(AppException.class,
            () -> {
                chatRoomService.leaveGroupRoom(privateRoomId);
            });
        Assertions.assertEquals(exception.getErrorCode(), ChatErrorCode.ONLY_GROUP_CHAT_CAN_LEAVE);
    }

    @Test
    @DisplayName("시나리오 6 [예외]: 개설된 1:1 채팅방에는 다른 유저가 참여할 수 없습니다.")
    void getPrivateRoom_Can_Not_Join() {
        Long privateRoomId = singleChatRoom.getId();
        when(chatRoomRepository.findById(privateRoomId)).thenReturn(Optional.of(singleChatRoom));

        AppException exception = Assertions.assertThrows(AppException.class,
            () -> {
                chatRoomService.joinGroupRoom(privateRoomId);
            });
        Assertions.assertEquals(exception.getErrorCode(), ChatErrorCode.CANNOT_JOIN_PRIVATE_ROOM);

    }

    @Test
    @DisplayName("시나리오 7 [성공]: roomKey를 이용하여 기존 방 ID를 찾을 수 있습니다.")
    void getFindPrivateRoom_Success_ReturnId() {
        when(chatRoomRepository.findByRoomKey(roomKey)).thenReturn(Optional.of(singleChatRoom));
        Long resulRoomId = chatRoomService.getPrivateRoomIdByRoomKey(roomKey);
        Assertions.assertEquals(resulRoomId, singleChatRoom.getId());

    }

    @Test
    @DisplayName("시나리오 8 [예외]: roomKey를 사용하여 방번호 검색 결과가 존재하지 않습니다.")
    void getNotFound_PrivateRoomId_RoomKey() {
        when(chatRoomRepository.findByRoomKey(roomKey)).thenReturn(Optional.empty());
        AppException exception = Assertions.assertThrows(AppException.class,
            () -> {
                chatRoomService.getPrivateRoomIdByRoomKey(roomKey);
            });
        Assertions.assertEquals(exception.getErrorCode(), ChatErrorCode.CHAT_ROOM_NOT_FOUND);

    }

    //        그룹 채팅방 테스트코드
    @Test
    @DisplayName("시나리오 9 [성공]: 기존에 존재하지 않는 그룹 채팅방을 정상적으로 생성할 수 있습니다.")
    void getCreateGroupRoom_Success() {
        Long myMemberId = member.getId();
        Long productId = product.getId();
        when(memberService.getMemberChatInfo(myMemberId)).thenReturn(memberChatInfo);
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        when(chatRoomRepository.existsByProductIdAndIsGroupChat(productId, true)).thenReturn(false);
        when(chatRoomNameGenerator.getProductRoomName(product.getName())).thenReturn(
            groupChatRoom.getName());
        when(chatRoomRepository.save(any())).thenReturn(groupChatRoom);

        ApiResponse<Long> response = chatRoomService.createGroupRoom(productId);

        Assertions.assertEquals(response.code(), "SUCCESS");
    }
    @Test
    @DisplayName("시나리오 10 [예외]: 이미 만들어져있는 상품의 그룹채팅방을 다시 생성하려고 시도 ")
    void getCreateGroupRoom_Fail_Duplication(){
        Long myMemberId = member.getId();
        Long productId = product.getId();
        when(memberService.getMemberChatInfo(myMemberId)).thenReturn(memberChatInfo);
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(chatRoomRepository.existsByProductIdAndIsGroupChat(productId, true)).thenReturn(true);
        AppException exception = Assertions.assertThrows(AppException.class,
            () -> {chatRoomService.createGroupRoom(productId);});

        Assertions.assertEquals(exception.getErrorCode(), ChatErrorCode.CHAT_ROOM_ALREADY_EXISTS);
    }
    @Test
    @DisplayName("시나리오 11 [성공]: 개설된 상품 그룹 채팅방에 새로운 유저가 정상적으로 참여할 수 있습니다.")
    void groupChatRoom_Join_Success() {
        Long myMemberId = member.getId();
        Long productId = product.getId();
        when(chatRoomRepository.findById(productId)).thenReturn(Optional.of(groupChatRoom));
        when(memberService.getMemberChatInfo(myMemberId)).thenReturn(memberChatInfo);
        when(chatParticipantRepository.findByChatRoomAndMemberId(groupChatRoom, myMemberId)).thenReturn(Optional.empty());

        ApiResponse<Void> response = chatRoomService.joinGroupRoom(groupChatRoom.getId());
        Assertions.assertEquals(response.code(), "SUCCESS");
    }
    @Test
    @DisplayName("시나리오 12 [예외]: 존재하지 않는 채팅방에 입장을 시도합니다.")
    void groupRoom_Fail_NotFound(){

        Long productId = product.getId();
        when(chatRoomRepository.findById(productId)).thenReturn(Optional.empty());
        AppException exception = Assertions.assertThrows(AppException.class,
            () -> {chatRoomService.joinGroupRoom(productId);});

        Assertions.assertEquals(exception.getErrorCode(), ChatErrorCode.CHAT_ROOM_NOT_FOUND);
    }
    @Test
    @DisplayName("시나리오 13 [예외]: 이미 참여중인 그룹 채팅방에 다시 입장을 시도합니다.")
    void groupRoom_Join_Fail_Duplication(){
        Long myMemberId = member.getId();
        Long productId = product.getId();

        when(chatRoomRepository.findById(productId)).thenReturn(Optional.of(groupChatRoom));
        when(memberService.getMemberChatInfo(myMemberId)).thenReturn(memberChatInfo);
        when(chatParticipantRepository.findByChatRoomAndMemberId(groupChatRoom, myMemberId)).thenReturn(Optional.of(mock(
            ChatParticipant.class)));
        AppException exception = Assertions.assertThrows(AppException.class,
            () -> {chatRoomService.joinGroupRoom(productId);});
        Assertions.assertEquals(exception.getErrorCode(), ChatErrorCode.ALREADY_PARTICIPANT);
    }

    @Test
    @DisplayName("시나리오 14 [성공]: 그룹 채팅방에서 정상적으로 퇴장합니다.")
    void groupRoom_Leave_Success() {
        Long myMemberId = member.getId();
        Long productId = product.getId();
        when(chatRoomRepository.findById(productId)).thenReturn(Optional.of(groupChatRoom));
        when(memberService.getMemberChatInfo(myMemberId)).thenReturn(memberChatInfo);
        when(chatParticipantRepository.findByChatRoomAndMemberId(groupChatRoom, myMemberId))
            .thenReturn(Optional.of(mock(ChatParticipant.class)));
        ApiResponse<Void> response = chatRoomService.leaveGroupRoom(productId);
        Assertions.assertEquals(response.code(), "SUCCESS");
    }

    @Test
    @DisplayName("시나리오 15 [예외]: 그룹 채팅방에 참여하지 않은 유저가 퇴장을 요청합니다.")
    void groupRoom_Leave_Fail_NotParticipant(){
        Long myMemberId = member.getId();
        Long productId = product.getId();
        when(chatRoomRepository.findById(productId)).thenReturn(Optional.of(groupChatRoom));
        when(memberService.getMemberChatInfo(myMemberId)).thenReturn(memberChatInfo);
        when(chatParticipantRepository.findByChatRoomAndMemberId(groupChatRoom, myMemberId)).thenReturn(Optional.empty());
        AppException exception = Assertions.assertThrows(AppException.class,
            () -> {chatRoomService.leaveGroupRoom(productId);});
        Assertions.assertEquals(exception.getErrorCode(), ChatErrorCode.NOT_A_ROOM_MEMBER);
    }
    @Test
    @DisplayName("시나리오 16 [예외]: 존재하지 않는 그룹채팅방에 퇴장 요청을 합니다.")
    void groupRoom_Leave_Fail_NotFoundRoom(){
        Long productId = product.getId();
        when(chatRoomRepository.findById(productId)).thenReturn(Optional.empty());
        AppException exception = Assertions.assertThrows(AppException.class,
            () -> {chatRoomService.leaveGroupRoom(productId);});
        Assertions.assertEquals(exception.getErrorCode(), ChatErrorCode.CHAT_ROOM_NOT_FOUND);

    }
    @Test
    @DisplayName("시나리오 17 [성공]: 그룹 채팅방에서 정상적으로 퇴장합니다.")
    void groupRoom_Leave_Success_Delete() {
        Long myMemberId = member.getId();
        Long productId = product.getId();
        when(chatRoomRepository.findById(productId)).thenReturn(Optional.of(groupChatRoom));
        when(memberService.getMemberChatInfo(myMemberId)).thenReturn(memberChatInfo);
        when(chatParticipantRepository.findByChatRoomAndMemberId(groupChatRoom, myMemberId))
            .thenReturn(Optional.of(mock(ChatParticipant.class)));
        when(chatParticipantRepository.findByChatRoom(groupChatRoom))
            .thenReturn(List.of());
        ApiResponse<Void> response = chatRoomService.leaveGroupRoom(productId);
        Assertions.assertEquals(response.code(), "SUCCESS");
    }

}


