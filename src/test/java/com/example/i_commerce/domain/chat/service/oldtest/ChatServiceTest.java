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
import com.example.i_commerce.domain.chat.service.ChatService;
import com.example.i_commerce.domain.chat.service.dto.ChatMessageSendResponse;
import com.example.i_commerce.domain.chat.util.ChatHealthCheck;
import com.example.i_commerce.domain.chat.util.ChatRoomNameGenerator;
import com.example.i_commerce.domain.member.entity.Member;
import com.example.i_commerce.domain.member.entity.enums.Gender;
import com.example.i_commerce.domain.member.entity.enums.MemberStatus;
import com.example.i_commerce.domain.member.entity.enums.MemberType;
import com.example.i_commerce.domain.product.entity.Category;
import com.example.i_commerce.domain.product.entity.Product;
import com.example.i_commerce.domain.product.entity.enums.ProductOptionType;
import com.example.i_commerce.domain.product.entity.enums.ProductStatus;
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
class ChatServiceTest {

    @InjectMocks
    private ChatService chatService;
    @Mock
    private ChatMessageRepository chatMessageRepository;
    @Mock
    private ChatRoomRepository chatRoomRepository;
    @Mock
    private ChatParticipantRepository chatParticipantRepository;
    @Mock
    private ChatRoomNameGenerator chatRoomNameGenerator;
    @Spy
    private ChatHealthCheck chatRoleChecker = new ChatHealthCheck();

    private Member member;
    private Member member2;
    private CustomUserPrincipal customUserPrincipal;
    private Member otherMember;
    private ChatRoom singlechatRoom;
    private ChatRoom groupchatRoom;
    private Product product;
    private ChatParticipant chatParticipant;
    private Category category;


    @BeforeEach
    public void MemberInit() {
/*      service 코드에서 memberId를 읽어올 때, Token의 SecurityContextHolder에서 각 값들을 빼서 사용하게 됨
        테스트 코드에서는 직접적인 HTTP 요청이 없으므로 이 값이 존재하지 않음 그러므로 사용해주기 위해 임의의 값을 채워주어야함
*/
        CustomUserPrincipal customUserPrincipal = new CustomUserPrincipal(PrincipalType.MEMBER, 1L,
            List.of());
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(customUserPrincipal, null, List.of()));

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

    //    채팅내역 조회
    @Test
    @DisplayName("채팅에 참여중이 아닌 사람이 조회를 시도하면 오류를 발생시킵니다.")
    void getChatHistory_NoMember() {
        when(chatRoomRepository.findById(singlechatRoom.getId())).thenReturn(
            Optional.of(singlechatRoom));
        when(chatParticipantRepository.findByChatRoom(singlechatRoom)).thenReturn(List.of());
        AppException exception = assertThrows(AppException.class, () ->
            chatService.getChatHistory(singlechatRoom.getId()));
        Assertions.assertThat(exception.getErrorCode()).isEqualTo(ChatErrorCode.NOT_A_ROOM_MEMBER);
    }

    @Test
    @DisplayName("채팅 내역을 정상적으로 불러옵니다")
    void getChatHistorySuccess() {
        when(chatRoomRepository.findById(singlechatRoom.getId())).thenReturn(
            Optional.of(singlechatRoom));
        when(chatParticipantRepository.findByChatRoom(singlechatRoom)).thenReturn(
            List.of(chatParticipant));
        when(chatMessageRepository.findByChatRoomOrderByCreatedAtAsc(singlechatRoom)).thenReturn(
            List.of());

        ApiResponse<List<ChatMessageSendResponse>> response = chatService.getChatHistory(
            singlechatRoom.getId());
        assertEquals("SUCCESS", response.code());
    }
}


