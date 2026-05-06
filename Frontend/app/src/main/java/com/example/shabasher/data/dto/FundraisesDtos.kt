package com.example.shabasher.data.dto

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

// ═══════════════════════════════════════════════════════
// DTOs (контракт бэкенда)
// ═══════════════════════════════════════════════════════

@Serializable
data class FundraisingItemResponseDto(
    @SerialName("id") val id: String,
    @SerialName("title") val title: String,
    @SerialName("shabashId") val shabashId: String,
    @SerialName("creatorId") val creatorId: String,
    @SerialName("paymentPhone") val paymentPhone: String,
    @SerialName("paymentRecipient") val paymentRecipient: String? = null,
    @SerialName("description") val description: String? = null,
    @SerialName("targetAmount")
    @Serializable(with = BigDecimalNullableSerializer::class)
    val targetAmount: BigDecimal? = null,
    @SerialName("currentAmount")
    @Serializable(with = BigDecimalSerializer::class)
    val currentAmount: BigDecimal = BigDecimal.ZERO,
    @SerialName("fundStatus")
    @Serializable(with = FundStatusSerializer::class)
    val fundStatus: FundStatus = FundStatus.Active,
    @SerialName("createdAt")
    @Serializable(with = InstantSerializer::class)
    val createdAt: Instant = Instant.EPOCH,
    @SerialName("myPaymentStatus")
    @Serializable(with = FundraiseParticipantStatusNullableSerializer::class)
    val myPaymentStatus: FundraiseParticipantStatus? = null
)

@Serializable
data class FundraiseParticipantInfoResponseDto(
    @SerialName("userId") val userId: String,
    @SerialName("status")
    @Serializable(with = FundraiseParticipantStatusSerializer::class)
    val status: FundraiseParticipantStatus,
    @SerialName("amount")
    @Serializable(with = BigDecimalNullableSerializer::class)
    val amount: BigDecimal? = null,
    @SerialName("paidAt")
    @Serializable(with = InstantNullableSerializer::class)
    val paidAt: Instant? = null,
    @SerialName("checkedAt")
    @Serializable(with = InstantNullableSerializer::class)
    val checkedAt: Instant? = null
)

@Serializable
data class FundraiseDetailsResponseDto(
    @SerialName("fundraising") val fundraising: FundraisingItemResponseDto,
    @SerialName("confirmedCount") val confirmedCount: Int = 0,
    @SerialName("participantsCount") val participantsCount: Int = 0,
    @SerialName("participants") val participants: List<FundraiseParticipantInfoResponseDto>? = null
)

@Serializable
data class FundraisesListResponseDto(
    @SerialName("fundraisings") val fundraisings: List<FundraisingItemResponseDto> = emptyList()
)

@Serializable
data class CreateFundraiseRequestDto(
    @SerialName("title") val title: String,
    @SerialName("description") val description: String?,
    @SerialName("targetAmount")
    @Serializable(with = BigDecimalNullableSerializer::class)
    val targetAmount: BigDecimal?,
    @SerialName("paymentPhone") val paymentPhone: String,
    @SerialName("paymentRecipient") val paymentRecipient: String
)

// ═══════════════════════════════════════════════════════
// Enums
// ═══════════════════════════════════════════════════════

enum class FundStatus { Active, Closed, Completed }

enum class FundraiseParticipantStatus { NotPaid, Paid, Confirmed, Reverted, Pending }

// ═══════════════════════════════════════════════════════
// Serializers
// ═══════════════════════════════════════════════════════

object InstantSerializer : KSerializer<Instant> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("Instant", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Instant) {
        encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): Instant {
        return parseInstantOrEpoch(decoder.decodeString())
    }
}

object InstantNullableSerializer : KSerializer<Instant?> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("InstantNullable", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Instant?) {
        if (value == null) encoder.encodeString("") else encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): Instant? {
        val raw = decoder.decodeString()
        if (raw.isBlank()) return null
        // .NET DateTime.MinValue → "0001-01-01T00:00:00"
        if (raw.startsWith("0001-01-01")) return null
        return parseInstantOrEpoch(raw).takeIf { it != Instant.EPOCH }
    }
}

private fun parseInstantOrEpoch(raw: String): Instant {
    if (raw.isBlank()) return Instant.EPOCH
    return try {
        // Прямой ISO-формат
        Instant.parse(raw)
    } catch (_: Exception) {
        try {
            // .NET-стиль "2026-01-15T10:30:00.1234567" (без зоны)
            val withZ = if (raw.endsWith("Z") || raw.contains("+") || raw.matches(Regex(".*-\\d\\d:\\d\\d$"))) {
                raw
            } else {
                "${raw}Z"
            }
            Instant.parse(withZ.take(30).let {
                // обрезать «лишние» наносекунды до 9 знаков
                it
            })
        } catch (_: Exception) {
            Instant.EPOCH
        }
    }
}

object BigDecimalSerializer : KSerializer<BigDecimal> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("BigDecimal", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: BigDecimal) {
        encoder.encodeString(value.toPlainString())
    }

    override fun deserialize(decoder: Decoder): BigDecimal {
        val raw = decoder.decodeString()
        return raw.toBigDecimalOrNull() ?: BigDecimal.ZERO
    }
}

object BigDecimalNullableSerializer : KSerializer<BigDecimal?> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("BigDecimalNullable", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: BigDecimal?) {
        encoder.encodeString(value?.toPlainString() ?: "")
    }

    override fun deserialize(decoder: Decoder): BigDecimal? {
        val raw = decoder.decodeString()
        if (raw.isBlank()) return null
        return raw.toBigDecimalOrNull()
    }
}

object FundStatusSerializer : KSerializer<FundStatus> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("FundStatus", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: FundStatus) {
        encoder.encodeString(value.name)
    }

    override fun deserialize(decoder: Decoder): FundStatus = decodeFundStatus(decoder.decodeString())
}

private fun decodeFundStatus(value: String): FundStatus {
    return when (value) {
        "Active" -> FundStatus.Active
        "Closed" -> FundStatus.Closed
        "Completed" -> FundStatus.Completed
        else -> when (value.toIntOrNull()) {
            0, 1 -> FundStatus.Active
            2 -> FundStatus.Closed
            3 -> FundStatus.Completed
            else -> throw SerializationException("Unknown FundStatus: $value")
        }
    }
}

object FundraiseParticipantStatusSerializer : KSerializer<FundraiseParticipantStatus> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("FundraiseParticipantStatus", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: FundraiseParticipantStatus) {
        encoder.encodeString(value.name)
    }

    override fun deserialize(decoder: Decoder): FundraiseParticipantStatus =
        decodeParticipantStatus(decoder.decodeString())
}

object FundraiseParticipantStatusNullableSerializer : KSerializer<FundraiseParticipantStatus?> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("FundraiseParticipantStatusNullable", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: FundraiseParticipantStatus?) {
        encoder.encodeString(value?.name ?: "")
    }

    override fun deserialize(decoder: Decoder): FundraiseParticipantStatus? {
        val raw = decoder.decodeString()
        if (raw.isBlank()) return null
        return decodeParticipantStatus(raw)
    }
}

private fun decodeParticipantStatus(value: String): FundraiseParticipantStatus {
    return when (value) {
        "NotPaid" -> FundraiseParticipantStatus.NotPaid
        "Paid" -> FundraiseParticipantStatus.Paid
        "Confirmed" -> FundraiseParticipantStatus.Confirmed
        "Reverted" -> FundraiseParticipantStatus.Reverted
        "Pending" -> FundraiseParticipantStatus.Pending
        else -> when (value.toIntOrNull()) {
            0 -> FundraiseParticipantStatus.NotPaid
            1 -> FundraiseParticipantStatus.Paid
            2 -> FundraiseParticipantStatus.Confirmed
            3 -> FundraiseParticipantStatus.Reverted
            4 -> FundraiseParticipantStatus.Pending
            else -> throw SerializationException("Unknown FundraiseParticipantStatus: $value")
        }
    }
}

// ═══════════════════════════════════════════════════════
// Domain models
// ═══════════════════════════════════════════════════════

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
    val remainingAmount: BigDecimal?
        get() = targetAmount?.minus(currentAmount)

    val progressPercent: Int
        get() = if (targetAmount == null || targetAmount.compareTo(BigDecimal.ZERO) == 0) {
            0
        } else {
            ((currentAmount.divide(targetAmount, 4, java.math.RoundingMode.HALF_UP))
                .multiply(BigDecimal(100)))
                .toInt()
                .coerceIn(0, 100)
        }

    val isActive: Boolean get() = fundStatus == FundStatus.Active
    val isClosed: Boolean get() = fundStatus == FundStatus.Closed
    val isCompleted: Boolean get() = fundStatus == FundStatus.Completed

    /** Может ли пользователь нажать «Я оплатил» */
    fun canMarkPaid(): Boolean {
        return isActive && (myPaymentStatus == null ||
                myPaymentStatus == FundraiseParticipantStatus.NotPaid ||
                myPaymentStatus == FundraiseParticipantStatus.Reverted)
    }

    fun isPendingConfirmation(): Boolean =
        myPaymentStatus == FundraiseParticipantStatus.Pending ||
                myPaymentStatus == FundraiseParticipantStatus.Paid

    fun isPaymentConfirmed(): Boolean =
        myPaymentStatus == FundraiseParticipantStatus.Confirmed

    fun getProgressText(): String =
        if (targetAmount != null) "${formatAmount(currentAmount)} из ${formatAmount(targetAmount)} ₽"
        else "${formatAmount(currentAmount)} ₽"

    private fun formatAmount(amount: BigDecimal): String =
        amount.toPlainString().replace(Regex("(\\d)(?=(\\d{3})+(?!\\d))"), "$1 ")
}

/**
 * Участник сбора
 */
data class FundraiseParticipant(
    val userId: String,
    val status: FundraiseParticipantStatus,
    val amount: BigDecimal,
    val paidAt: Instant?,
    val checkedAt: Instant?
) {
    val isConfirmed: Boolean get() = status == FundraiseParticipantStatus.Confirmed
    val isPending: Boolean
        get() = status == FundraiseParticipantStatus.Pending ||
                status == FundraiseParticipantStatus.Paid
    val isNotPaid: Boolean
        get() = status == FundraiseParticipantStatus.NotPaid ||
                status == FundraiseParticipantStatus.Reverted
}

// ═══════════════════════════════════════════════════════
// DTO → Domain
// ═══════════════════════════════════════════════════════

fun FundraisingItemResponseDto.toDomain(currentUserId: String): Fundraise {
    return Fundraise(
        id = id,
        title = title,
        shabashId = shabashId,
        creatorId = creatorId,
        paymentPhone = paymentPhone,
        paymentRecipient = paymentRecipient.orEmpty(),
        description = description,
        targetAmount = targetAmount,
        currentAmount = currentAmount,
        fundStatus = fundStatus,
        createdAt = createdAt,
        isCreator = creatorId == currentUserId,
        myPaymentStatus = myPaymentStatus,
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
        paymentRecipient = fundraising.paymentRecipient.orEmpty(),
        description = fundraising.description,
        targetAmount = fundraising.targetAmount,
        currentAmount = fundraising.currentAmount,
        fundStatus = fundraising.fundStatus,
        createdAt = fundraising.createdAt,
        isCreator = fundraising.creatorId == currentUserId,
        myPaymentStatus = fundraising.myPaymentStatus,
        confirmedCount = confirmedCount,
        participantsCount = participantsCount,
        participants = participants?.map { it.toDomain() }
    )
}

fun FundraiseParticipantInfoResponseDto.toDomain(): FundraiseParticipant {
    return FundraiseParticipant(
        userId = userId,
        status = status,
        amount = amount ?: BigDecimal.ZERO,
        paidAt = paidAt,
        checkedAt = checkedAt
    )
}
