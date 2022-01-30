package com.carlos.grabredenvelope.services

import android.os.Bundle
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.EditText
import com.carlos.cutils.util.AppUtils
import com.carlos.cutils.util.LogUtils
import com.carlos.grabredenvelope.data.RedEnvelopePreferences
import com.carlos.grabredenvelope.util.WechatConstants
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 *                             _ooOoo_
 *                            o8888888o
 *                            88" . "88
 *                            (| -_- |)
 *                            O\  =  /O
 *                         ____/`---'\____
 *                       .'  \\|     |//  `.
 *                      /  \\|||  :  |||//  \
 *                     /  _||||| -:- |||||-  \
 *                     |   | \\\  -  /// |   |
 *                     | \_|  ''\---/''  |   |
 *                     \  .-\__  `-`  ___/-. /
 *                   ___`. .'  /--.--\  `. . __
 *                ."" '<  `.___\_<|>_/___.'  >'"".
 *               | | :  `- \`.;`\ _ /`;.`/ - ` : | |
 *               \  \ `-.   \_ __\ /__ _/   .-` /  /
 *          ======`-.____`-.___\_____/___.-`____.-'======
 *                             `=---='
 *          ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
 *                     佛祖保佑        永无BUG
 *            佛曰:
 *                   写字楼里写字间，写字间里程序员；
 *                   程序人员写程序，又拿程序换酒钱。
 *                   酒醒只在网上坐，酒醉还来网下眠；
 *                   酒醉酒醒日复日，网上网下年复年。
 *                   但愿老死电脑间，不愿鞠躬老板前；
 *                   奔驰宝马贵者趣，公交自行程序员。
 *                   别人笑我忒疯癫，我笑自己命太贱；
 *                   不见满街漂亮妹，哪个归得程序员？
 */

/**
 * Created by Carlos on 2021/2/9.
 */
class EmojiService : BaseAccessibilityService() {

    private var windowClassName = ""
    private val CHAT_ACTIVITY = "com.tencent.mm.ui.LauncherUI" //微信红包弹框
    private var text = ""
    private var times = 0
    private var interval = 0
    private var count = 0

    override fun onCreate() {
        super.onCreate()
        WechatConstants.setVersion(AppUtils.getVersionName(WechatConstants.WECHAT_PACKAGE))
    }

    override fun monitorContentChanged(event: AccessibilityEvent) {
    }

    override fun monitorWindowChanged(event: AccessibilityEvent) {
        LogUtils.d("monitorWindowChanged:$event")
        windowClassName = event.className.toString()
        loadConfig()
        sendMessage()
    }

    private fun loadConfig() {
        text = RedEnvelopePreferences.autoText
        times = RedEnvelopePreferences.emojiTimes
        interval = RedEnvelopePreferences.emojiInterval
        count = 0
        LogUtils.d("text:$text")
        LogUtils.d("times:$times")
        LogUtils.d("interval:$interval")
        LogUtils.d("count:$count")
    }

    /**
     * 找到文本框输入表情，找到发送按钮点击循环执行
     */
    private fun sendMessage() {
        if (windowClassName != CHAT_ACTIVITY) return
        if (count >= times && times != 0) {
            return
        }
        LogUtils.d("count:$count")

        val accessibilityNodeInfo =
            rootInActiveWindow?.findAccessibilityNodeInfosByViewId(WechatConstants.CHAT_EDITTEXT_ID)
                ?: return

        for (editText in accessibilityNodeInfo) {
            if (editText.className == EditText::class.java.name) {
                val arguments = Bundle()
                arguments.putCharSequence(
                    AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                    text
                )
                editText.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)

                send()
                break

            }

        }

        count++
        GlobalScope.launch {
            delay(interval.toLong())
            sendMessage()
        }

    }

    private fun send() {
        val accessibilityNodeInfos =
            rootInActiveWindow?.findAccessibilityNodeInfosByText("发送") ?: return
        for (accessibilityNodeInfo in accessibilityNodeInfos) {
            accessibilityNodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK)
        }
    }


}