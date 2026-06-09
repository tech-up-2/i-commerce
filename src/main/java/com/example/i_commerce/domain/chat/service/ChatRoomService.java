package com.example.i_commerce.domain.chat.service;

import com.example.i_commerce.domain.chat.entity.ChatParticipant;
import com.example.i_commerce.domain.chat.entity.ChatRoom;
import com.example.i_commerce.domain.chat.exception.ChatErrorCode;
import com.example.i_commerce.domain.chat.repository.ChatParticipantRepository;
import com.example.i_commerce.domain.chat.repository.ChatRoomRepository;
import com.example.i_commerce.domain.chat.util.ChatHealthCheck;
import com.example.i_commerce.domain.chat.util.ChatRoomNameGenerator;
import com.example.i_commerce.domain.chat.util.TempChatUtil;
import com.example.i_commerce.domain.member.repository.MemberRepository;
import com.example.i_commerce.domain.member.service.member.MemberService;
import com.example.i_commerce.domain.member.service.member.dto.MemberChatInfo;
import com.example.i_commerce.domain.product.entity.Product;
import com.example.i_commerce.domain.product.exception.ProductErrorCode;
import com.example.i_commerce.domain.product.repository.ProductRepository;
import com.example.i_commerce.global.common.response.ApiResponse;
import com.example.i_commerce.global.exception.AppException;
import com.sun.net.httpserver.Authenticator.Success;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final ProductRepository productRepository;

    private final ChatHealthCheck chatRoleChecker;
    private final ChatRoomNameGenerator chatRoomNameGenerator;
    private final MemberService memberService;



    public void addParticipantToRoom(ChatRoom chatRoom, Long memberId) {
        ChatParticipant chatParticipant = ChatParticipant.builder()
            .chatRoom(chatRoom)
            .memberId(memberId)
            .build();
        chatParticipantRepository.save(chatParticipant);
    }

    public ApiResponse<Long> getOrCreatePrivateRoom(Long otherMemberId) {

        MemberChatInfo member = memberService.getMemberChatInfo(TempChatUtil.getCurrentUserId());
        MemberChatInfo otherMember = memberService.getMemberChatInfo(otherMemberId);

        reqToSelf(member, otherMember);
        chatRoleChecker.roleCheck(member, otherMember);

//       나와 상대방이 1:1 채팅을 이미 참여하고 있다면 에러코드를 return
//       사용자 입장에서는 에러코드 보다는 참여하고있는 채팅 리다이렉션이 훨씬 편리할 것 같음.
        String roomKey = ChatRoom.generateRoomKey(member.id(), otherMember.id());
        Optional<ChatRoom> chatRoom = chatRoomRepository.findByRoomKey(roomKey);
        if (chatRoom.isPresent()) {
            return ApiResponse.success(chatRoom.get().getId());
        }

//      만약에 1:1 채팅방이 없을 경우 기존 채팅방 개설
        String privateRoomName = chatRoomNameGenerator.getPrivateRoomName(member.name(),
            otherMember.name());
        ChatRoom newRoom = ChatRoom.builder()
            .isGroupChat(false)
            .name(privateRoomName)
            .roomKey(roomKey)
            .build();
        chatRoomRepository.save(newRoom);
//        두 사람을 채팅방에 참여자로 새롭게 추가
        addParticipantToRoom(newRoom, member.id());
        addParticipantToRoom(newRoom, otherMember.id());

        return ApiResponse.success(newRoom.getId());
    }
    @Transactional(readOnly = true)
    public Long getPrivateRoomIdByRoomKey(String roomKey) {
        return chatRoomRepository.findByRoomKey(roomKey)
            .map(ChatRoom::getId).orElseThrow(() -> new AppException(ChatErrorCode.CHAT_ROOM_NOT_FOUND));
    }

    public ApiResponse<Long> createGroupRoom(Long productId) {
//      채팅방 참여 멤버 검증
        MemberChatInfo member = memberService.getMemberChatInfo(TempChatUtil.getCurrentUserId());
//      상품 존재여부 검증
        Product product = productRepository.findById(productId).orElseThrow(() -> new AppException(
            ProductErrorCode.PRODUCT_NOT_FOUND));

//      채팅방 중복 여부 검증
        if (chatRoomRepository.existsByProductIdAndIsGroupChat(product.getId(), true)) {
            throw new AppException(ChatErrorCode.CHAT_ROOM_ALREADY_EXISTS);
        }

//      채팅방 생성
        String ProductRoomName = chatRoomNameGenerator.getProductRoomName(product.getName());
        ChatRoom chatRoom = ChatRoom.builder()
            .name(ProductRoomName)
            .isGroupChat(true)
            .productId(product.getId())
            .build();

        chatRoomRepository.save(chatRoom);

//      최초 생성자를 방에 참가시킨다.
        addParticipantToRoom(chatRoom, member.id());

        return ApiResponse.success(chatRoom.getId());
    }


    public ApiResponse<Void> joinGroupRoom(Long roomId) {
        //실제로 채팅방이 존재하는지 검증
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
            .orElseThrow(() -> new AppException(ChatErrorCode.CHAT_ROOM_NOT_FOUND));
        //해당 채팅방이 그룹채팅인지 검증
        if (!chatRoom.getIsGroupChat()) {
            throw new AppException(ChatErrorCode.CANNOT_JOIN_PRIVATE_ROOM);
        }
        //참여하고자 하는 멤버가 존재하는지 검증
        MemberChatInfo member = memberService.getMemberChatInfo(TempChatUtil.getCurrentUserId());
        //강퇴 여부 확인(미구현)

        //이미 참여하고 있는지 검증 및 멤버 추가
        Optional<ChatParticipant> participant = chatParticipantRepository.findByChatRoomAndMemberId(
            chatRoom, member.id());
        if (participant.isPresent()) {
            throw new AppException(ChatErrorCode.ALREADY_PARTICIPANT);
        }
        addParticipantToRoom(chatRoom, member.id());
        return ApiResponse.success();
    }

    public ApiResponse<Void> leaveGroupRoom(Long roomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
            .orElseThrow(() -> new AppException(ChatErrorCode.CHAT_ROOM_NOT_FOUND));
        MemberChatInfo member = memberService.getMemberChatInfo(TempChatUtil.getCurrentUserId());
        //해당 채팅방이 그룹채팅인지 검증
        if (!chatRoom.getIsGroupChat()) {
            throw new AppException(ChatErrorCode.ONLY_GROUP_CHAT_CAN_LEAVE);
        }
        //해당 사용자가 해당 채팅방에 참가하고 있는지 검증
        ChatParticipant chatParticipant = chatParticipantRepository.findByChatRoomAndMemberId(
                chatRoom, member.id())
            .orElseThrow(() -> new AppException(ChatErrorCode.NOT_A_ROOM_MEMBER));
        //유저를 해당 채팅방에서 제거
        chatParticipantRepository.delete(chatParticipant);

        //해당 방에 유저가 아무도 없는지 조회
        List<ChatParticipant> participants = chatParticipantRepository.findByChatRoom(chatRoom);
        //만약 유저가 한명도 없으면 채팅방을 제거한다.
        if (participants.isEmpty()) {
            //소프트 딜리트 적용
            chatRoom.delete();
            //실제로 DB에서 제거가 되지 않으므로 저장 해주어야함.
            chatRoomRepository.save(chatRoom);
        }

        return ApiResponse.success();
    }
    public void reqToSelf(MemberChatInfo member, MemberChatInfo otherMember){
        if(member.id().equals(otherMember.id())){
            throw new AppException(ChatErrorCode.CANNOT_REQUEST_TO_SELF);
        }
    }

}
