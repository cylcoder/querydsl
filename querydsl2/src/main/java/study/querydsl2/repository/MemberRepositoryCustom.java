package study.querydsl2.repository;

import study.querydsl2.dto.MemberSearchCondition;
import study.querydsl2.dto.MemberTeamDto;

import java.util.List;

public interface MemberRepositoryCustom {

    List<MemberTeamDto> search(MemberSearchCondition condition);

}
