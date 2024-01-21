package com.moa.puzzlekit2;

public enum AdType {
    INTERSTITIAL("01", "전면"),
    REWARD("02", "보상"),
    BANNER("03", "배너");

    private final String code;
    private final String name;

    AdType(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return this.code;
    }

    public String getName() {
        return this.name;
    }

    public static AdType getEnum(String code) {
        for (AdType type : AdType.values()) {
            if (type.getCode().equals(code)) {
                return AdType.valueOf(type.toString());
            }
        }
        return null;
    }

}