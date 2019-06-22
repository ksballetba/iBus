package com.ksballetba.ibus.ui.listener

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager



class MyOrientationListener(private val mContext:Context):SensorEventListener{

    private var mSensorManager: SensorManager? = null// 传感器管理者
    private var mAccelerometer: Sensor? = null// 加速度传感器
    private var mMagnetic: Sensor? = null// 地磁传感器
    private var mOnOrientationListener: OnOrientationListener? = null// 方向监听
    private var mAccelerometerValues = FloatArray(3)// 用于保存加速度值
    private var mMagneticValues = FloatArray(3)// 用于保存地磁值

    init {
        start()
    }

    /**
     * 设置监听
     *
     * @param onOrientationListener
     */
    fun setOnOrientationListener(onOrientationListener: OnOrientationListener) {
        this.mOnOrientationListener = onOrientationListener
    }

    /**
     * 实例化方向监听所需的传感器
     */
    private fun start() {
        // 实例化传感器管理工具
        mSensorManager = mContext.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        // 初始化加速度传感器
        mAccelerometer = mSensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        // 初始化地磁场传感器
        mMagnetic = mSensorManager!!.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    }

    /**
     * <pre>
     * 注册加速度和地磁场传感器监听
     * 在onResume中调用
    </pre> *
     */
    fun registerListener() {
        if (mSensorManager != null && mAccelerometer != null && mMagnetic != null) {
            mSensorManager!!.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL)
            mSensorManager!!.registerListener(this, mMagnetic, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    /**
     * <pre>
     * 解除监听
     * onPause中进行调用
    </pre> *
     */
    fun unregisterListener() {
        mSensorManager!!.unregisterListener(this)
    }

    /**
     * <pre>
     * 计算方向
     * 根据传感器获取到的加速度值和地磁值先得出旋转矩阵
     * 再根据旋转矩阵得到最终结果，最终结果包含航向角、俯仰角、翻滚角
    </pre> *
     */
    private fun calculateOrientation() {
        val values = FloatArray(3)// 最终结果
        val R = FloatArray(9)// 旋转矩阵
        SensorManager.getRotationMatrix(R, null, mAccelerometerValues, mMagneticValues)// 得到旋转矩阵
        SensorManager.getOrientation(R, values)// 得到最终结果
        var azimuth = Math.toDegrees(values[0].toDouble()).toFloat()// 航向角
        if (azimuth < 0) {
            azimuth += 360f
        }
        azimuth = azimuth / 5 * 5// 做了一个处理，表示以5°的为幅度
        val pitch = Math.toDegrees(values[1].toDouble()).toFloat()// 俯仰角
        val roll = Math.toDegrees(values[2].toDouble()).toFloat()// 翻滚角
        if (mOnOrientationListener != null) {
            mOnOrientationListener!!.onOrientationChanged(azimuth, pitch, roll)
        }
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            mAccelerometerValues = event.values
        }
        if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            mMagneticValues = event.values
        }
        calculateOrientation()
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {

    }

    interface OnOrientationListener {
        fun onOrientationChanged(azimuth: Float, pitch: Float, roll: Float)
    }


}