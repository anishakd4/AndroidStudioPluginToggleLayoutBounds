import com.android.ddmlib.IDevice
import com.android.ddmlib.MultiLineReceiver
import com.android.ddmlib.NullOutputReceiver
import com.intellij.notification.NotificationDisplayType
import com.intellij.notification.NotificationGroup
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import org.jetbrains.android.sdk.AndroidSdkUtils

class ShowLayoutBounds : AnAction() {
    override fun actionPerformed(event: AnActionEvent) {
        val devices = event.project?.let { AndroidSdkUtils.getDebugBridge(it)?.devices }

        devices?.takeIf { it.isNotEmpty() }?.forEach { device ->
            device.executeShellCommand("getprop debug.layout", SingleLineReceiver { firstLine ->
                val enable = firstLine.toBoolean().not()
                device.setLayoutBounds(enable)
            })
        } ?: run {
            event.project?.showNotifications("No devices connected")
        }

    }

}

class SingleLineReceiver(val processFirstLine: (response: String) -> Unit) : MultiLineReceiver() {

    private var cancelled: Boolean = false

    override fun isCancelled() = cancelled

    override fun processNewLines(lines: Array<out String>?) {
        lines?.getOrNull(0)?.let{firstLine->
            processFirstLine(firstLine)
            cancelled = true
        }
    }

}

fun IDevice.setLayoutBounds(enable: Boolean) {
    executeShellCommand("setprop debug.layout $enable ;service call activity 1599295570", NullOutputReceiver())
}

fun Project.showNotifications(message: String) {
    NotificationGroup("someid", NotificationDisplayType.BALLOON).createNotification("title", message, NotificationType.WARNING, null).notify(this)
}