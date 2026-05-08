package com.example.shabasher.ViewModels

import androidx.lifecycle.ViewModel
import com.example.shabasher.notifications.NotificationCenter

class NotificationsViewModel : ViewModel() {

    val notifications = NotificationCenter.notifications
    val unreadCount = NotificationCenter.unreadCountFlow()

    fun markAsRead(id: String) = NotificationCenter.markAsRead(id)
    fun markAllAsRead() = NotificationCenter.markAllAsRead()
    fun delete(id: String) = NotificationCenter.delete(id)
    fun clearAll() = NotificationCenter.clearAll()
}
