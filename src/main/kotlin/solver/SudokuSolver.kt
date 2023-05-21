package solver

import SudokuState

class SudokuSolver(
    private val methods: List<SudokuSolverMethod> = listOf(
        LastMissingInRowColSquare,
        OnlyPossibilityForCell,
        OnlyPossibleForColRowSquare,
    )
) {
    constructor(vararg methods: SudokuSolverMethod) : this(methods.toList())

    fun solve(sudokuState: SudokuState): Result {
        var state = sudokuState
        val methodsUsedCounter = mutableMapOf<String, Int>()
        while (true) {
            val (method, newState) = methods.firstNotNullOfOrNull { method ->
                method.apply(state)?.let { method to it }
            } ?: methods.firstNotNullOfOrNull { method -> // TODO: Remove
                method.apply(state)?.let { method to it }
            }
            ?: return Result(state, state.isFilled(), methodsUsedCounter)

            state = newState
            methodsUsedCounter[method.name] = (methodsUsedCounter[method.name] ?: 0) + 1
            if (state.isFilled()) {
                return Result(state, true, methodsUsedCounter)
            }
        }
    }

    class Result(
        val state: SudokuState,
        val isSolved: Boolean,
        val methodsUsedCounter: Map<String, Int>
    )
}