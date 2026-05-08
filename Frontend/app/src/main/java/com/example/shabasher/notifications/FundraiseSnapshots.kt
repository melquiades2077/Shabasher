package com.example.shabasher.notifications

import com.example.shabasher.data.dto.FundStatus
import com.example.shabasher.data.dto.FundraiseParticipantStatus
import java.util.concurrent.ConcurrentHashMap

/**
 * Снэпшоты последних виденных значений — чтобы детектить изменения,
 * сделанные ИЗВНЕ (другим пользователем / админом) между перезагрузками экрана.
 *
 * Только в памяти процесса. Очищается при kill приложения — это намеренно,
 * чтобы не показывать stale-уведомления через несколько дней.
 */
object FundraiseSnapshots {

    private data class Snapshot(
        val fundStatus: FundStatus,
        val myPaymentStatus: FundraiseParticipantStatus?
    )

    private val snapshots = ConcurrentHashMap<String, Snapshot>()
    /** eventId → известные id сборов в этом событии (для детекта новых) */
    private val listSnapshots = ConcurrentHashMap<String, Set<String>>()

    /** Возвращает diff с предыдущим состоянием. null = нет предыдущего (первая загрузка). */
    fun diff(
        fundraiseId: String,
        fundStatus: FundStatus,
        myPaymentStatus: FundraiseParticipantStatus?
    ): FundraiseDiff? {
        val previous = snapshots[fundraiseId]
        // Обновляем snapshot ВСЕГДА, чтобы следующий вызов сравнивал с новым.
        snapshots[fundraiseId] = Snapshot(fundStatus, myPaymentStatus)

        if (previous == null) return null

        return FundraiseDiff(
            fundStatusChanged = previous.fundStatus != fundStatus,
            previousFundStatus = previous.fundStatus,
            myPaymentChanged = previous.myPaymentStatus != myPaymentStatus,
            previousMyPaymentStatus = previous.myPaymentStatus
        )
    }

    /** Возвращает множество id, которых не было в предыдущем снепшоте. */
    fun diffList(eventId: String, currentIds: Collection<String>): Set<String> {
        val previous = listSnapshots[eventId]
        listSnapshots[eventId] = currentIds.toSet()
        if (previous == null) return emptySet()
        return currentIds.toSet() - previous
    }

    fun resetForFundraise(fundraiseId: String) {
        snapshots.remove(fundraiseId)
    }
}

data class FundraiseDiff(
    val fundStatusChanged: Boolean,
    val previousFundStatus: FundStatus,
    val myPaymentChanged: Boolean,
    val previousMyPaymentStatus: FundraiseParticipantStatus?
)
