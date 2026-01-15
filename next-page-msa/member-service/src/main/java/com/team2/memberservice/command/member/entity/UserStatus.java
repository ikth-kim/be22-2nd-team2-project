package com.team2.memberservice.command.member.entity;

/**
 * 사용자 상태 Enum (Soft Delete용)
 * ACTIVE: 활성 상태, DELETED: 탈퇴 상태
 *
 * @author 김태형
 */
public enum UserStatus {
    ACTIVE,
    DELETED,
    PENDING
}
