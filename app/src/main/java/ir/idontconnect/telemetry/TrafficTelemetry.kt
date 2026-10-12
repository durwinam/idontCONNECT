package ir.idontconnect.telemetry

import ir.idontconnect.core.model.TrafficSample
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/** The UI consumes only real counters; an absent native metrics endpoint produces no samples. */
interface TrafficTelemetry {
    fun samples(intervalMs: Long = 3_000L): Flow<TrafficSample>
}

class UnavailableTrafficTelemetry : TrafficTelemetry {
    override fun samples(intervalMs: Long): Flow<TrafficSample> = flow {
        // Deliberately emit no fabricated traffic values. Replace with Xray metrics polling.
        while (true) delay(intervalMs.coerceAtLeast(1_000L))
    }
}

class TrafficAverager {
    fun average(samples: List<TrafficSample>): Pair<Double, Double>? {
        if (samples.isEmpty()) return null
        val download = samples.map { it.downloadBytesPerSecond.coerceAtLeast(0L) }.average()
        val upload = samples.map { it.uploadBytesPerSecond.coerceAtLeast(0L) }.average()
        return download to upload
    }
}
