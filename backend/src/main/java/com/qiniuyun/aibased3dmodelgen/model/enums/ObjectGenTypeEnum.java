package com.qiniuyun.aibased3dmodelgen.model.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

/**
 * 模型类型生成枚举
 */
@Getter
public enum ObjectGenTypeEnum {

    OBJ("OBJ 格式模型", "obj");

    private final String text;
    private final String value;

    ObjectGenTypeEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 根据 value 获取枚举
     *
     * @param value 枚举值的value
     * @return 枚举值
     */
    public static ObjectGenTypeEnum getEnumByValue(String value) {
        if (ObjUtil.isEmpty(value)) {
            return null;
        }
        for (ObjectGenTypeEnum anEnum : ObjectGenTypeEnum.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }
}
