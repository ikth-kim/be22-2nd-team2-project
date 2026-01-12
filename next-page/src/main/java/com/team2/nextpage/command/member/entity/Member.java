package com.team2.nextpage.command.member.entity;

import com.team2.nextpage.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

/**
 * 회원(Member) 엔티티 (Soft delete 적용)
 * @author 김태형
 */
@Entity
@Getter
@Builder
@Table(name = "users")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@SQLDelete(sql = "UPDATE users SET user_status = 'DELETED', left_at = NOW() WHERE user_id = ?")
@SQLRestriction("user_status = 'ACTIVE'")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "user_id")
  private Long userId;

  @Column(name = "user_email", nullable = false, unique = true, length = 100)
  private String userEmail;

  @Column(name = "user_pw", nullable = false)
  private String userPw;

  @Column(name = "user_nicknm", nullable = false, unique = true, length = 50)
  private String userNicknm;

  @Enumerated(EnumType.STRING)
  @Column(name = "user_role", nullable = false, length = 20, columnDefinition = "varchar(20) default 'USER'")
  private UserRole userRole;

  @Enumerated(EnumType.STRING)
  @Column(name = "user_status", nullable = false, length = 20, columnDefinition = "varchar(20) default 'ACTIVE'")
  private UserStatus userStatus; // 'ACTIVE', 'DELETE'

  @Column(name = "left_at")
  private LocalDateTime leftAt; // 탈퇴 일시


  public void setEncodedPassword(String encodedPassword) {
    this.userPw = encodedPassword;
  }

  public void modifyRole(String roleName) {
    this.userRole = UserRole.valueOf(roleName);
  }
}
