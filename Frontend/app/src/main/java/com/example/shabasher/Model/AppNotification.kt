package com.example.shabasher.Model

import kotlinx.serialization.Serializable

/**
 * Категория уведомления — определяет иконку, канал Android и группировку.
 */
@Serializable
enum class NotificationCategory {
    /** Сборы: отметка оплаты, подтверждения, закрытие сбора и т.д. */
    Fundraise,

    /** События: приглашения, изменения, роли */
    Event,

    /** Прочее (системные, advisory) */
    System
}

/**
 * Уведомление в локальном центре уведомлений.
 *
 * Хранится только на клиенте (DataStore). Используется и для in-app экрана,
 * и для системного уведомления в шторке Android.
 */
@Serializable
data class AppNotification(
    val id: String,
    val category: NotificationCategory,
    val title: String,
    val body: String,
    /** Deep-link route в навигации, например "donation/{id}" или "event/{id}". null = не кликабельно. */
    val targetRoute: String? = null,
    val createdAtIso: String,
    val isRead: Boolean = false,
    /** Идентификатор связанной сущности — чтобы дедуплицировать «новый сбор X» и т.п. */
    val sourceKey: String? = null
)
