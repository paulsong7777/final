package com.moeats.domain;

import java.sql.Timestamp;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Member {
        int memberIdx;
        String memberEmail;
        String memberPassword;
        String memberNickname;
        String memberPhone;
        String memberRoleType;
        Integer defaultDeliveryAddressIdx;
        String memberStatus;
        Timestamp createdAt;
        Timestamp updatedAt;
}