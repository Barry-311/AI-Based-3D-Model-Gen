package com.qiniuyun.aibased3dmodelgen.model.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

/**
 * 模型请求类型生成枚举
 */
@Getter
public enum ModelGenTypeEnum {

    TEXT("文本生成模型", "text_to_model"),
    IMAGE("图片生成模型", "image_to_model");

    private final String text;
    private final String value;

    ModelGenTypeEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 根据 value 获取枚举
     *
     * @param value 枚举值的value
     * @return 枚举值
     */
    public static ModelGenTypeEnum getEnumByValue(String value) {
        if (ObjUtil.isEmpty(value)) {
            return null;
        }
        for (ModelGenTypeEnum anEnum : ModelGenTypeEnum.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }
}
