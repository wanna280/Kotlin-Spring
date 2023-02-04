package com.wanna.debugger.jvm

/**
 * 单步调试的类型枚举值
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/29
 */
enum class StepType(val index: Int) {
    /**
     * 步入
     */
    STEP_INTO(1),

    /**
     * 步过
     */
    STEP_OVER(2),

    /**
     * 步出
     */
    STEP_OUT(3);

    companion object {
        /**
         * 根据index去获取StepType的枚举值
         *
         * @param index stepType index
         * @return StepType
         */
        @JvmStatic
        fun valueOf(index: Int): StepType {
            return when (index) {
                1 -> STEP_INTO
                2 -> STEP_OVER
                3 -> STEP_OUT
                else -> throw IllegalStateException("Illegal StepType index")
            }
        }
    }
}