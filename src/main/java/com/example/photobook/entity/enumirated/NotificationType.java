package com.example.photobook.entity.enumirated;

public enum NotificationType {
    ORDER_ASSIGNED,
    TASK_ACTIVATED,
    ORDER_UPDATED,
    ORDER_STATUS_CHANGED,
    /** Operator bosqichni tugatganda (admin/menejer monitoring) */
    ADMIN_TASK_STEP_COMPLETED,
    /** Keyingi xodim navbatiga o‘tganda (admin/menejer monitoring) */
    ADMIN_TASK_HANDOFF
}
