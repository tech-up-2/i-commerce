package com.example.i_commerce.domain.chat.service.oldtest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.example.i_commerce.domain.chat.entity.ChatParticipant;
import com.example.i_commerce.domain.chat.entity.ChatRoom;
import com.example.i_commerce.domain.chat.exception.ChatErrorCode;
import com.example.i_commerce.domain.chat.repository.ChatMessageRepository;
import com.example.i_commerce.domain.chat.repository.ChatParticipantRepository;
import com.example.i_commerce.domain.chat.repository.ChatRoomRepository;
import com.example.i_commerce.domain.chat.service.ChatRoomService;
import com.example.i_commerce.domain.chat.util.ChatRoleChecker;
import com.example.i_commerce.domain.chat.util.ChatRoomNameGenerator;
import com.example.i_commerce.domain.member.entity.Member;
import com.example.i_commerce.domain.member.entity.enums.Gender;
import com.example.i_commerce.domain.member.entity.enums.MemberStatus;
import com.example.i_commerce.domain.member.entity.enums.MemberType;
import com.example.i_commerce.domain.member.repository.MemberRepository;
import com.example.i_commerce.domain.product.entity.Category;
import com.example.i_commerce.domain.product.entity.Product;
import com.example.i_commerce.domain.product.entity.ProductOptionType;
import com.example.i_commerce.domain.product.entity.ProductStatus;
import com.example.i_commerce.domain.product.repository.ProductRepository;
import com.example.i_commerce.global.common.response.ApiResponse;
import com.example.i_commerce.global.exception.AppException;
import com.example.i_commerce.global.security.principal.CustomUserPrincipal;
import com.example.i_commerce.global.security.principal.CustomUserPrincipal.PrincipalType;
import java.util.List;
import java.util.Optional;
import org.assertj.core.api.Assertions;
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
public class ChatRoomServiceTest {
    @InjectMocks
    private ChatRoomService chatRoomService;
    @Mock
    private ChatMessageRepository chatMessageRepository;
    @Mock
    private ChatRoomRepository chatRoomRepository;
    @Mock
    private ChatParticipantRepository chatParticipantRepository;
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private ProductRepository productRepository;

    private Member member;
    private Member member2;
    private Member otherMember;
    private ChatRoom singlechatRoom;
    private ChatRoom groupchatRoom;
    private Product product;
    private ChatParticipant chatParticipant;
    private Category category;
    @Mock
    private ChatRoomNameGenerator chatRoomNameGenerator;

    @Spy
    private ChatRoleChecker chatRoleChecker = new ChatRoleChecker();


    @BeforeEach
    public void MemberInit() {
        CustomUserPrincipal customUserPrincipal = new CustomUserPrincipal(PrincipalType.MEMBER, 1L,
            "test1@naver.com", "1234", List.of());
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(customUserPrincipal, null, List.of()));

        // 카테고리 생성
        category = Category.builder()
            .id(1L)
            .name("테스트 카테고리")
            .depth(1)
            .build();

        // 상품 생성
        product = Product.builder()
            .id(1L)
            .storeId(1L)
            .category(category)
            .name("테스트 상품")
            .description("테스트 상품 설명")
            .optionType(ProductOptionType.SINGLE)
            .status(ProductStatus.ON_SALE)
            .build();

        member = Member.builder()
            .id(1L)
            .emailHash("user1@test.com")
            .password("1234")
            .name("테스트 유저1".getBytes())
            .sex(Gender.MALE)
            .birthday("20050505".getBytes())
            .phoneNumber("01011111111".getBytes())
            .point(0)
            .status(MemberStatus.ACTIVE)
            .role(MemberType.CUSTOMER)
            .build();

        otherMember = Member.builder()
            .id(2L)
            .emailHash("user2@test.com")
            .password("4321")
            .name("테스트 유저2".getBytes())
            .sex(Gender.MALE)
            .birthday("20060606".getBytes())
            .phoneNumber("01022222222".getBytes())
            .point(0)
            .status(MemberStatus.ACTIVE)
            .role(MemberType.SELLER)
            .build();

        member2 = Member.builder()
            .id(3L)
            .emailHash("user3@test.com")
            .password("1234")
            .name("테스트 유저3".getBytes())
            .sex(Gender.MALE)
            .birthday("20030303".getBytes())
            .phoneNumber("01033333333".getBytes())
            .point(0)
            .status(MemberStatus.ACTIVE)
            .role(MemberType.CUSTOMER)
            .build();

        singlechatRoom = ChatRoom.builder()
            .id(5L)
            .isGroupChat(false)
            .name("테스트 1번")
            .build();
        groupchatRoom = ChatRoom.builder()

            .id(6L)
            .isGroupChat(true)
            .name("테스트 그룹 채팅")
            .build();

        chatParticipant = ChatParticipant.builder()
            .id(1L)
            .chatRoom(groupchatRoom)
            .memberId(member.getId())
            .isBan(false)
            .build();

    }

//  1:1 채팅방 테스트코드

    @Test
    @DisplayName("1:1 채팅방이 정상적으로 생성됩니다.")
    void successCreatePrivateChatRoom() {
        when(memberRepository.findById(member.getId())).thenReturn(Optional.of(member));
        when(memberRepository.findById(otherMember.getId())).thenReturn(Optional.of(otherMember));
        when(chatParticipantRepository.findExistingPrivateRoom
            (member.getId(), otherMember.getId())).thenReturn(Optional.empty());

        ApiResponse<Long> response = chatRoomService.getOrCreatePrivateRoom(otherMember.getId());

        assertEquals("SUCCESS", response.code());

    }

    @Test
    @DisplayName("1:1 채팅방이 이미 존재하면 예외를 터트립니다.")
    void existingChatRoom() {

        when(memberRepository.findById(member.getId())).thenReturn(Optional.of(member));
        when(memberRepository.findById(otherMember.getId())).thenReturn(Optional.of(otherMember));
        when(chatParticipantRepository.findExistingPrivateRoom(member.getId(),
            otherMember.getId())).thenReturn(Optional.of(singlechatRoom));

        AppException exception = assertThrows(AppException.class,
            () -> chatRoomService.getOrCreatePrivateRoom(otherMember.getId()));

        Assertions.assertThat(exception.getErrorCode())
            .isEqualTo(ChatErrorCode.CHAT_ROOM_ALREADY_EXISTS);
    }

    @Test
    @DisplayName("1:1 채팅 생성시 서로의 권한이 같으면 에러를 터트립니다.")
    void cannotSameRole(){
        when(memberRepository.findById(member.getId())).thenReturn(Optional.of(member));
        when((memberRepository.findById(member2.getId()))).thenReturn(Optional.of(member2));

        AppException exception = assertThrows(AppException.class, () ->
            chatRoomService.getOrCreatePrivateRoom(member2.getId())); //

        Assertions.assertThat(exception.getErrorCode()).isEqualTo(ChatErrorCode.CANNOT_CHAT_SAME_ROLE);
    }

    //    그룹채팅 테스트코드
    @Test
    @DisplayName("그룹 채팅방이 이미 존재하면 예외를 터트립니다.")
    void createGroupChatRoom() {

        when(memberRepository.findById(member.getId())).thenReturn(Optional.of(member));
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(chatRoomRepository.existsByProductIdAndIsGroupChat(product.getId(), true)).thenReturn(
            true);

        AppException exception = assertThrows(AppException.class, () ->
            chatRoomService.createGroupRoom(product.getId()));

        Assertions.assertThat(exception.getErrorCode())
            .isEqualTo(ChatErrorCode.CHAT_ROOM_ALREADY_EXISTS);
    }

    @Test
    @DisplayName("그룹 채팅방이 없으면 새로 생성합니다.")
    void createGroupRoomSuccess() {
        when(memberRepository.findById(member.getId())).thenReturn(Optional.of(member));
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(chatRoomRepository.existsByProductIdAndIsGroupChat(product.getId(), true)).thenReturn(
            false);

        ApiResponse<Long> response = chatRoomService.createGroupRoom(product.getId());

        assertEquals("SUCCESS", response.code());
    }


    @Test
    @DisplayName("그룹 채팅방에 참여합니다.")
    void joinGroupChatRoom() {
        //when
        when(memberRepository.findById(member.getId())).thenReturn(Optional.of(member));
        when(chatRoomRepository.findById(groupchatRoom.getId())).thenReturn(
            Optional.of(groupchatRoom));

        ApiResponse<Void> response = chatRoomService.joinGroupRoom(groupchatRoom.getId());

        assertEquals("SUCCESS", response.code());
    }

//        @Test
//    @DisplayName("그룹 채팅방에서 퇴장합니다.")
//    void leaveGroupRoom() {
//        when(chatRoomRepository.findById(groupchatRoom.getId())).thenReturn(
//            Optional.of(groupchatRoom));
//        when(memberRepository.findById(member.getId())).thenReturn(Optional.of(member));
//        when(chatParticipantRepository.findByChatRoomAndMember(groupchatRoom, member))
//            .thenReturn(Optional.of(chatParticipant));
//
//        ApiResponse<Void> response = chatService.leaveGroupRoom(groupchatRoom.getId(),
//            member.getId());
//
//        assertEquals("SUCCESS", response.code());
//
//    }
    @Test
    @DisplayName("그룹 채팅방에서 퇴장합니다.")
    void leaveGroupRoom() {
        when(chatRoomRepository.findById(groupchatRoom.getId())).thenReturn(
            Optional.of(groupchatRoom));
        when(memberRepository.findById(member.getId())).thenReturn(Optional.of(member));
        when(chatParticipantRepository.findByChatRoomAndMemberId(groupchatRoom, member.getId()))
            .thenReturn(Optional.of(chatParticipant));

        ApiResponse<Void> response = chatRoomService.leaveGroupRoom(groupchatRoom.getId());

        assertEquals("SUCCESS", response.code());

    }
}
