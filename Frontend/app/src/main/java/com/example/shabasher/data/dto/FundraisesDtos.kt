package com.example.shabasher.data.dto

import kotlinx.serialization.Contextual
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.math.BigDecimal
import java.time.Instant

@Serializable
data class FundraisingItemResponseDto(

    @SerialName("id") val id: String,
    @SerialName("title") val title: String,
    @SerialName("shabashId") val shabashId: String,
    @SerialName("creatorId") val creatorId: String,
    @SerialName("paymentPhone") val paymentPhone: String,
    @SerialName("paymentRecipient") val paymentRecipient: String,
    @SerialName("description") val description: String?,
    @SerialName("targetAmount")
    @Serializable(with = BigDecimalSerializer::class)
    val targetAmount: BigDecimal?,
    @SerialName("currentAmount")
    @Serializable(with = BigDecimalSerializer::class)
    val currentAmount: BigDecimal,
    @SerialName("fundStatus")
    @Serializable(with = FundStatusSerializer::class)  // ✅ Добавь это!
    val fundStatus: FundStatus,
    @SerialName("createdAt")
    @Serializable(with = InstantSerializer::class)
    val createdAt: Instant,
    @SerialName("myPaymentStatus")
    @Serializable(with = FundraiseParticipantStatusSerializer::class)  // ✅ Добавляем это!
    val myPaymentStatus: FundraiseParticipantStatus?
)

@Serializable
data class FundraiseParticipantInfoResponseDto(
    @SerialName("userId") val userId: String,
    @SerialName("status") val status: FundraiseParticipantStatus,
    @SerialName("amount")
    @Serializable(with = BigDecimalSerializer::class)
    val amount: BigDecimal,
    @SerialName("paidAt")
    @Serializable(with = InstantSerializer::class)
    val paidAt: Instant,
    @SerialName("checkedAt")
    @Serializable(with = InstantSerializer::class)
    val checkedAt: Instant?
)

@Serializable
data class FundraiseDetailsResponseDto(
    @SerialName("fundraising") val fundraising: FundraisingItemResponseDto,
    @SerialName("confirmedCount") val confirmedCount: Int,
    @SerialName("participantsCount") val participantsCount: Int,
    @SerialName("participants") val participants: List<FundraiseParticipantInfoResponseDto>?
)

@Serializable
data class FundraisesListResponseDto(
    @SerialName("fundraisings") val fundraisings: List<FundraisingItemResponseDto>
)

@Serializable
data class CreateFundraiseRequestDto(
    @SerialName("title") val title: String,
    @SerialName("description") val description: String?,
    @SerialName("targetAmount")
    @Serializable(with = BigDecimalSerializer::class)
    val targetAmount: BigDecimal?,
    @SerialName("paymentPhone") val paymentPhone: String,
    @SerialName("paymentRecipient") val paymentRecipient: String
)

// ⚠️ Внимание: этот DTO не используется в теле запроса!
// Бэкенд ожидает голое decimal? (примитив), а не объект.
// Оставляем файл для документации, но в репозитории отправляем JsonPrimitive
@Serializable
data class ConfirmFundraisePaymentRequestDto(
    @Serializable(with = BigDecimalSerializer::class)
    @SerialName("amount") val amount: BigDecimal?
)

@Serializable
enum class FundStatus {
    @SerialName("Active") Active,
    @SerialName("Closed") Closed,
    @SerialName("Completed") Completed
}

@Serializable
enum class FundraiseParticipantStatus {
    @SerialName("NotPaid") NotPaid,
    @SerialName("Paid") Paid,
    @SerialName("Confirmed") Confirmed,
    @SerialName("Reverted") Reverted,
    @SerialName("Pending") Pending
}

object InstantSerializer : KSerializer<Instant> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("Instant", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Instant) {
        encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): Instant {
        return Instant.parse(decoder.decodeString())
    }
}

object BigDecimalSerializer : KSerializer<BigDecimal> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("BigDecimal", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: BigDecimal) {
        encoder.encodeString(value.toPlainString())
    }

    override fun deserialize(decoder: Decoder): BigDecimal {
        return BigDecimal(decoder.decodeString())
    }
}


fun FundraisingItemResponseDto.toDomain(currentUserId: String): Fundraise {
    return Fundraise(
        id = id,
        title = title,
        shabashId = shabashId,
        creatorId = creatorId,
        paymentPhone = paymentPhone,
        paymentRecipient = paymentRecipient,
        description = description,
        targetAmount = targetAmount,
        currentAmount = currentAmount,
        fundStatus = fundStatus,
        createdAt = createdAt,
        isCreator = creatorId == currentUserId,
        myPaymentStatus = myPaymentStatus,
        // Для списка эти поля могут быть null
        confirmedCount = null,
        participantsCount = null,
        participants = null
    )
}

fun FundraiseDetailsResponseDto.toDomain(currentUserId: String): Fundraise {
    return Fundraise(
        id = fundraising.id,
        title = fundraising.title,
        shabashId = fundraising.shabashId,
        creatorId = fundraising.creatorId,
        paymentPhone = fundraising.paymentPhone,
        paymentRecipient = fundraising.paymentRecipient,
        description = fundraising.description,
        targetAmount = fundraising.targetAmount,
        currentAmount = fundraising.currentAmount,
        fundStatus = fundraising.fundStatus,
        createdAt = fundraising.createdAt,
        isCreator = fundraising.creatorId == currentUserId,
        myPaymentStatus = fundraising.myPaymentStatus,
        // Детали содержат полную информацию
        confirmedCount = confirmedCount,
        participantsCount = participantsCount,
        participants = participants?.map { it.toDomain() }
    )
}

fun FundraiseParticipantInfoResponseDto.toDomain(): FundraiseParticipant {
    return FundraiseParticipant(
        userId = userId,
        status = status,
        amount = amount,
        paidAt = paidAt,
        checkedAt = checkedAt
    )
}

/**
 * Модель сбора средств на мероприятие
 */
data class Fundraise(
    val id: String,
    val title: String,
    val shabashId: String,
    val creatorId: String,
    val paymentPhone: String,
    val paymentRecipient: String,
    val description: String?,
    val targetAmount: BigDecimal?,
    val currentAmount: BigDecimal,
    val fundStatus: FundStatus,
    val createdAt: Instant,
    val isCreator: Boolean = false,
    val myPaymentStatus: FundraiseParticipantStatus? = null,
    val confirmedCount: Int? = null,
    val participantsCount: Int? = null,
    val participants: List<FundraiseParticipant>? = null
) {
    // ✅ Вычисленные свойства

    /**
     * Сколько ещё осталось собрать
     */
    val remainingAmount: BigDecimal?
        get() = targetAmount?.minus(currentAmount)

    /**
     * Прогресс сбора в процентах (0-100)
     */
    val progressPercent: Int
        get() = if (targetAmount == null || targetAmount.compareTo(BigDecimal.ZERO) == 0) {
            0
        } else {
            ((currentAmount.divide(targetAmount, 4, java.math.RoundingMode.HALF_UP))
                .multiply(BigDecimal(100)))
                .toInt()
                .coerceIn(0, 100)
        }

    /**
     * Сбор активен (можно принимать оплаты)
     */
    val isActive: Boolean
        get() = fundStatus == FundStatus.Active

    /**
     * Сбор закрыт (нельзя принимать новые оплаты)
     */
    val isClosed: Boolean
        get() = fundStatus == FundStatus.Closed

    /**
     * Сбор завершён (цель достигнута или вручную закрыт)
     */
    val isCompleted: Boolean
        get() = fundStatus == FundStatus.Completed

    /**
     * Текущий пользователь может подтвердить оплату (админ/создатель)
     */
    fun canConfirmPayments(): Boolean = isCreator

    /**
     * Текущий пользователь может закрыть сбор (админ/создатель)
     */
    fun canCloseFundraise(): Boolean = isCreator

    /**
     * Текущий пользователь может отметить оплату (участник, ещё не оплатил)
     */
    fun canMarkPaid(): Boolean {
        return isActive && (myPaymentStatus == null ||
                myPaymentStatus == FundraiseParticipantStatus.NotPaid ||
                myPaymentStatus == FundraiseParticipantStatus.Reverted)
    }

    /**
     * Текущий пользователь уже оплатил (ожидает подтверждения)
     */
    fun isPendingConfirmation(): Boolean {
        return myPaymentStatus == FundraiseParticipantStatus.Pending
    }

    /**
     * Оплата текущего пользователя подтверждена
     */
    fun isPaymentConfirmed(): Boolean {
        return myPaymentStatus == FundraiseParticipantStatus.Confirmed
    }

    /**
     * Прогресс бар с текстом (например, "5000 из 10000 ₽")
     */
    fun getProgressText(): String {
        return if (targetAmount != null) {
            "${formatAmount(currentAmount)} из ${formatAmount(targetAmount)} ₽"
        } else {
            "${formatAmount(currentAmount)} ₽"
        }
    }

    /**
     * Форматирование суммы (разделение тысяч)
     */
    private fun formatAmount(amount: BigDecimal): String {
        return amount.toPlainString().replace(
            Regex("(\\d)(?=(\\d{3})+(?!\\d))"),
            "$1 "
        )
    }
}

/**
 * Участник сбора (кто оплатил или должен оплатить)
 */
data class FundraiseParticipant(
    val userId: String,
    val status: FundraiseParticipantStatus,
    val amount: BigDecimal,
    val paidAt: Instant,
    val checkedAt: Instant?
) {
    // ✅ Вычисленные свойства

    /**
     * Оплата подтверждена админом
     */
    val isConfirmed: Boolean
        get() = status == FundraiseParticipantStatus.Confirmed

    /**
     * Оплата ожидает подтверждения
     */
    val isPending: Boolean
        get() = status == FundraiseParticipantStatus.Pending

    /**
     * Оплата не подтверждена / отменена
     */
    val isNotPaid: Boolean
        get() = status == FundraiseParticipantStatus.NotPaid ||
                status == FundraiseParticipantStatus.Reverted

    /**
     * Дата подтверждения (или null если не подтверждено)
     */
    val confirmedAt: Instant?
        get() = if (isConfirmed) checkedAt else null
}

object FundStatusSerializer : KSerializer<FundStatus> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("FundStatus", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: FundStatus) {
        // Отправляем на бэкенд в ожидаемом формате
        when (value) {
            FundStatus.Active -> encoder.encodeString("Active")
            FundStatus.Closed -> encoder.encodeString("Closed")
            FundStatus.Completed -> encoder.encodeString("Completed")
        }
    }

    override fun deserialize(decoder: Decoder): FundStatus {
        // ✅ Принимаем ОБА формата: строки и числа
        val value = decoder.decodeString()
        return when (value) {
            // Строковые значения (ожидаемые)
            "Active" -> FundStatus.Active
            "Closed" -> FundStatus.Closed
            "Completed" -> FundStatus.Completed
            // ✅ Числовые значения (реальность бэкенда)
            "1", "0" -> FundStatus.Active      // 0 или 1 = Active
            "2" -> FundStatus.Closed           // 2 = Closed
            "3" -> FundStatus.Completed        // 3 = Completed
            // Fallback: пробуем распарсить как Int и сопоставить
            else -> try {
                when (value.toIntOrNull()) {
                    0, 1 -> FundStatus.Active
                    2 -> FundStatus.Closed
                    3 -> FundStatus.Completed
                    else -> throw SerializationException("Unknown FundStatus: $value")
                }
            } catch (e: NumberFormatException) {
                throw SerializationException("Unknown FundStatus: $value")
            }
        }
    }
}

// ═══════════════════════════════════════════════════════
// FundraiseParticipantStatusSerializer.kt
// ═══════════════════════════════════════════════════════

object FundraiseParticipantStatusSerializer : KSerializer<FundraiseParticipantStatus> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("FundraiseParticipantStatus", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: FundraiseParticipantStatus) {
        // Отправляем на бэкенд в строковом формате
        when (value) {
            FundraiseParticipantStatus.NotPaid -> encoder.encodeString("NotPaid")
            FundraiseParticipantStatus.Paid -> encoder.encodeString("Paid")
            FundraiseParticipantStatus.Confirmed -> encoder.encodeString("Confirmed")
            FundraiseParticipantStatus.Reverted -> encoder.encodeString("Reverted")
            FundraiseParticipantStatus.Pending -> encoder.encodeString("Pending")
        }
    }

    override fun deserialize(decoder: Decoder): FundraiseParticipantStatus {
        // ✅ Принимаем ОБА формата: строки и числа
        val value = decoder.decodeString()
        return when (value) {
            // 📝 Строковые значения (ожидаемые)
            "NotPaid" -> FundraiseParticipantStatus.NotPaid
            "Paid" -> FundraiseParticipantStatus.Paid
            "Confirmed" -> FundraiseParticipantStatus.Confirmed
            "Reverted" -> FundraiseParticipantStatus.Reverted
            "Pending" -> FundraiseParticipantStatus.Pending

            // 🔢 Числовые значения (реальность бэкенда)
            // Предположительная маппинг-схема (уточните у бэкенда!)
            "0" -> FundraiseParticipantStatus.NotPaid      // ⚠️ уточните!
            "1" -> FundraiseParticipantStatus.Paid         // ⚠️ уточните!
            "2" -> FundraiseParticipantStatus.Confirmed    // ⚠️ уточните!
            "3" -> FundraiseParticipantStatus.Reverted     // ⚠️ уточните!
            "4" -> FundraiseParticipantStatus.Pending      // ⚠️ уточните!

            // 🔄 Fallback: пробуем распарсить как Int
            else -> try {
                when (value.toIntOrNull()) {
                    0 -> FundraiseParticipantStatus.NotPaid
                    1 -> FundraiseParticipantStatus.Paid
                    2 -> FundraiseParticipantStatus.Confirmed
                    3 -> FundraiseParticipantStatus.Reverted
                    4 -> FundraiseParticipantStatus.Pending
                    else -> throw SerializationException("Unknown FundraiseParticipantStatus: $value")
                }
            } catch (e: NumberFormatException) {
                throw SerializationException("Unknown FundraiseParticipantStatus: $value")
            }
        }
    }
}