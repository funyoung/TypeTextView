package com.uperone.typetext.view

import android.content.Context
import android.media.MediaPlayer
import android.text.TextUtils
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import com.uperone.typetextview.R
import java.util.Timer
import java.util.TimerTask

/**
 * 模拟打字机效果
 *
 */
class TypeTextView : AppCompatTextView {
    private var mContext: Context? = null
    private var mMediaPlayer: MediaPlayer? = null
    private var mShowTextString: String? = null
    private var mTypeTimer: Timer? = null
    private var mOnTypeViewListener: OnTypeViewListener? = null
    private var mTypeTimeDelay = TYPE_TIME_DELAY // 打字间隔

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        initTypeTextView(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initTypeTextView(context)
    }

    constructor(context: Context) : super(context) {
        initTypeTextView(context)
    }

    fun setOnTypeViewListener(onTypeViewListener: OnTypeViewListener?) {
        mOnTypeViewListener = onTypeViewListener
    }

    @JvmOverloads
    fun start(textString: String?, typeTimeDelay: Int = TYPE_TIME_DELAY) {
        if (TextUtils.isEmpty(textString) || typeTimeDelay < 0) {
            return
        }
        post {
            mShowTextString = textString
            mTypeTimeDelay = typeTimeDelay
            text = ""
            startTypeTimer()
            mOnTypeViewListener?.onTypeStart()
        }
    }

    fun stop() {
        stopTypeTimer()
        stopAudio()
    }

    private fun initTypeTextView(context: Context) {
        mContext = context
    }

    private fun startTypeTimer() {
        stopTypeTimer()
        mTypeTimer = Timer().also { it.schedule(TypeTimerTask(), mTypeTimeDelay.toLong()) }
    }

    private fun stopTypeTimer() {
        mTypeTimer?.run {
            cancel()
            mTypeTimer = null
        }
    }

    private fun startAudioPlayer() {
        stopAudio()
        playAudio(R.raw.type_in)
    }

    private fun playAudio(audioResId: Int) {
        try {
            stopAudio()
            mMediaPlayer = MediaPlayer.create(mContext, audioResId).also { it.start() }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun stopAudio() {
        mMediaPlayer?.run {
            if (isPlaying) {
                stop()
                release()
                mMediaPlayer = null
            }
        }
    }

    internal inner class TypeTimerTask : TimerTask() {
        override fun run() {
            post {
                mShowTextString?.apply {
                    if (getText().toString().length < length) {
                        text = substring(0, getText().toString().length + 1)
                        startAudioPlayer()
                        startTypeTimer()
                        mOnTypeViewListener?.onTyping()
                    } else {
                        stopTypeTimer()
                        mOnTypeViewListener?.onTypeOver()
                    }
                }
            }
        }
    }

    interface OnTypeViewListener {
        fun onTypeStart()
        fun onTypeOver()
        fun onTyping()
    }

    companion object {
        private const val TYPE_TIME_DELAY = 80
    }
}
