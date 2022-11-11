package com.wanna.boot

/**
 * ExitCode的Generators，组合了ExitCodeGenerator的列表
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/2
 */
class ExitCodeGenerators : Iterable<ExitCodeGenerator> {
    private val generators = ArrayList<ExitCodeGenerator>()

    fun addAll(vararg exitCodeGenerators: ExitCodeGenerator) {
        generators += exitCodeGenerators
    }

    fun addAll(exitCodeGenerators: Iterable<ExitCodeGenerator>) {
        generators += exitCodeGenerators
    }

    fun add(exitCodeGenerator: ExitCodeGenerator) {
        generators += exitCodeGenerator
    }

    override fun iterator(): Iterator<ExitCodeGenerator> = generators.iterator()

    /**
     * 根据内部组合的ExitCodeGenerator列表去生成ExitCode
     *
     * @return ExitCode
     */
    fun getExitCode(): Int {
        var exitCode = 0
        for (generator in generators) {
            try {
                val value = generator.getExitCode()
                if (value > 0 && value > exitCode || value < 0 && value < exitCode) {
                    exitCode = value
                }
            } catch (ex: Exception) {
                exitCode = if (exitCode != 0) exitCode else 1
                ex.printStackTrace()
            }
        }
        return exitCode
    }
}