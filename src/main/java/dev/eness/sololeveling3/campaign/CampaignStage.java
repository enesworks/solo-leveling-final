package dev.eness.sololeveling3.campaign;

/** Stable names are stored in saves; adding a stage cannot shift older saves. */
public enum CampaignStage {
    WAIT_BARAN,
    DOUBLE_READY, DOUBLE_ACTIVE,
    NORMAL_BEFORE_LEGIA, LEGIA_KEYS, LEGIA_READY, LEGIA_ACTIVE,
    NORMAL_BEFORE_FROST, FROST_READY, FROST_ACTIVE,
    NORMAL_BEFORE_TRIO, TRIO_READY, TRIO_ACTIVE,
    NORMAL_BEFORE_FINAL, FINAL_READY, FINAL_ACTIVE,
    COMPLETE;

    public boolean normalGates() {
        return this == NORMAL_BEFORE_LEGIA || this == NORMAL_BEFORE_FROST || this == NORMAL_BEFORE_TRIO || this == NORMAL_BEFORE_FINAL;
    }

    public boolean activeDungeon() {
        return this == DOUBLE_ACTIVE || this == LEGIA_ACTIVE || this == FROST_ACTIVE || this == TRIO_ACTIVE || this == FINAL_ACTIVE;
    }

    public CampaignStage afterNormalGates() {
        return switch(this) {
            case NORMAL_BEFORE_LEGIA -> LEGIA_KEYS;
            case NORMAL_BEFORE_FROST -> FROST_READY;
            case NORMAL_BEFORE_TRIO -> TRIO_READY;
            case NORMAL_BEFORE_FINAL -> FINAL_READY;
            default -> this;
        };
    }

    public static CampaignStage read(String name) {
        try { return valueOf(name); } catch (IllegalArgumentException ignored) { return WAIT_BARAN; }
    }
}
