package com.bit.reservation.global.status;

import lombok.Getter;

public enum UserProfileImage {

    SMILE(1, "https://spam-image.s3.ap-northeast-2.amazonaws.com/smile.png"),
    SURPRISED(2, "https://spam-image.s3.ap-northeast-2.amazonaws.com/surprised.png"),
    SMILE_FACE(3, "https://spam-image.s3.ap-northeast-2.amazonaws.com/smiling-face.png"),
    CAT(4, "https://spam-image.s3.ap-northeast-2.amazonaws.com/tears.png"),
    TOGETHER(5, "https://spam-image.s3.ap-northeast-2.amazonaws.com/emoticons.png");

    @Getter
    private final int num;

    @Getter
    private final String image;

    UserProfileImage(int num, String image) {
        this.num = num;
        this.image = image;
    }
}
