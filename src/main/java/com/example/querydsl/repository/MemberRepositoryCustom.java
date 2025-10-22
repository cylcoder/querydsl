package com.example.querydsl.repository;

import com.example.querydsl.dto.MemberSearchCondition;
import com.example.querydsl.dto.MemberTeamDto;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MemberRepositoryCustom {

  List<MemberTeamDto> search(MemberSearchCondition memberSearchCondition);

  Page<MemberTeamDto> searchPageSimple(
      MemberSearchCondition memberSearchCondition,
      Pageable pageable
  );

  Page<MemberTeamDto> searchPageComplex(
      MemberSearchCondition memberSearchCondition,
      Pageable pageable
  );

}
