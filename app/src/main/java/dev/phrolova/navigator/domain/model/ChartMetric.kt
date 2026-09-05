package dev.phrolova.navigator.domain.model

enum class ChartMetric(val label: String) {
    RELAPSE("破戒"),
    MASTURBATION("自慰"),
    PORN("看黄"),
    INTERCOURSE("房事"),
    EMISSION("遗精"),
    STAY_UP_LATE("熬夜"),
    ALL_NIGHTER("通宵"),
}

data class MetricSeries(
    val metric: ChartMetric,
    val values: List<Float>,
)

data class TimeSeriesChart(
    val xLabels: List<String>,
    val series: List<MetricSeries>,
)
