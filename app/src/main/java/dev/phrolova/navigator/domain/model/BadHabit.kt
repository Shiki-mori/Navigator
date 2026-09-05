package dev.phrolova.navigator.domain.model

enum class BadHabit {
    STAY_UP_LATE,
    ALL_NIGHTER,
    ;

    val label: String
        get() = when (this) {
            STAY_UP_LATE -> "熬夜"
            ALL_NIGHTER -> "通宵"
        }

    companion object {
        fun fromStored(value: String?): BadHabit? {
            if (value.isNullOrBlank()) return null
            return when (value.trim()) {
                "STAY_UP_LATE", "熬夜" -> STAY_UP_LATE
                "ALL_NIGHTER", "通宵" -> ALL_NIGHTER
                else -> null
            }
        }
    }
}
