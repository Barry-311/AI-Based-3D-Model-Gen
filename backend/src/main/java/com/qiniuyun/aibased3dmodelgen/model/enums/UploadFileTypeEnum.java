package com.qiniuyun.aibased3dmodelgen.model.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

/**
 * 图片生成类型枚举
 */
@Getter
public enum UploadFileTypeEnum {

    USER_UPLOADED("用户上传图片", "user_uploaded"),
    RENDERED_IMAGE("生成模型预览图", "rendered_image"),
    PBR_MODEL("生成PBR模型", "pbr_model");


    private final String text;
    private final String value;

    UploadFileTypeEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 根据 value 获取枚举
     *
     * @param value 枚举值的value
     * @return 枚举值
     */
    public static UploadFileTypeEnum getEnumByValue(String value) {
        if (ObjUtil.isEmpty(value)) {
            return null;
        }
        for (UploadFileTypeEnum anEnum : UploadFileTypeEnum.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }
}
