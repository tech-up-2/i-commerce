package com.example.i_commerce.temp_member.service;

import com.example.i_commerce.temp_member.domain.Member;
import com.example.i_commerce.temp_member.dto.MemberListResDto;
import com.example.i_commerce.temp_member.dto.MemberLoginReqDto;
import com.example.i_commerce.temp_member.dto.MemberSaveReqDto;
import com.example.i_commerce.temp_member.repository.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;


    public MemberService(MemberRepository memberRepository, PasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Member create(MemberSaveReqDto memberSaveReqDto) {
        //이미 가입되어 있는 이메일 검증
        if(memberRepository.findByEmail(memberSaveReqDto.getEmail()).isPresent()){
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }
        Member newMember = Member.builder()
            .name(memberSaveReqDto.getName())
            .email(memberSaveReqDto.getEmail())
            .password(passwordEncoder.encode(memberSaveReqDto.getPassword()))
            .build();
        Member member = memberRepository.save(newMember);

        return member;
    }
    public Member login(MemberLoginReqDto memberLoginReqDto) {
        Member member = memberRepository.findByEmail(memberLoginReqDto.getEmail()).orElseThrow(()->new EntityNotFoundException("존재하지 않는 이메일 입니다."));

        if(!passwordEncoder.matches(memberLoginReqDto.getPassword(), member.getPassword())){
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
        return member;
    }
    public List<MemberListResDto> findAll(){
        List<Member> members = memberRepository.findAll();
        List<MemberListResDto> memberListResDtos = new ArrayList<>();
        for(Member m : members){
            MemberListResDto memberListResDto = new MemberListResDto();
            memberListResDto.setId(m.getId());
            memberListResDto.setName(m.getName());
            memberListResDto.setEmail(m.getEmail());
            memberListResDtos.add(memberListResDto);
        }
        return  memberListResDtos;
    }
}
