package com.aiagent.mobile.core.data.tool

import com.aiagent.mobile.core.domain.model.AgentTask
import com.aiagent.mobile.core.domain.model.AgentTaskType
import com.aiagent.mobile.core.domain.tool.ToolExecutor
import com.aiagent.mobile.core.domain.tool.ToolResult
import com.aiagent.mobile.core.domain.tool.ToolType
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class FinanceTool @Inject constructor(
    @Named("tool") private val httpClient: OkHttpClient
) : ToolExecutor() {

    override val toolType = ToolType.FINANCE
    override val activatesFor = setOf(AgentTaskType.PRICE_MONITOR, AgentTaskType.JOB_MONITOR)

    // CoinGecko public API — no auth required
    private val cryptoUrl = "https://api.coingecko.com/api/v3/simple/price" +
        "?ids=bitcoin,ethereum,solana,bnb,ripple,dogecoin,cardano,polkadot" +
        "&vs_currencies=usd,eur,gbp&include_24hr_change=true"

    // Open Exchange Rates (free tier) — no auth
    private val fxUrl = "https://open.er-api.com/v6/latest/USD"

    override suspend fun execute(task: AgentTask): ToolResult {
        val sb = StringBuilder()
        var fetchedAny = false

        // --- Crypto prices ---
        try {
            val cryptoJson = fetch(cryptoUrl)
            if (cryptoJson != null) {
                sb.appendLine("**Cryptocurrency Prices (USD):**")
                val obj = JSONObject(cryptoJson)
                obj.keys().forEach { coin ->
                    val data = obj.getJSONObject(coin)
                    val usd = data.optDouble("usd", Double.NaN)
                    val change24h = data.optDouble("usd_24h_change", Double.NaN)
                    if (!usd.isNaN()) {
                        val changeStr = if (!change24h.isNaN()) " (24h: %+.2f%%)".format(change24h) else ""
                        sb.appendLine("  • ${coin.capitalize()}: \$${"%,.2f".format(usd)}$changeStr")
                    }
                }
                fetchedAny = true
            }
        } catch (e: Exception) {
            Timber.w(e, "FinanceTool: failed to fetch crypto prices")
            sb.appendLine("Crypto data temporarily unavailable.")
        }

        // --- FX Rates ---
        try {
            val fxJson = fetch(fxUrl)
            if (fxJson != null) {
                val obj = JSONObject(fxJson)
                val rates = obj.optJSONObject("rates")
                if (rates != null) {
                    val relevantPairs = listOf("EUR", "GBP", "JPY", "CAD", "AUD", "CHF", "CNY", "TRY")
                    sb.appendLine("\n**FX Rates (base: USD):**")
                    relevantPairs.forEach { currency ->
                        val rate = rates.optDouble(currency, Double.NaN)
                        if (!rate.isNaN()) sb.appendLine("  • USD/${currency}: %.4f".format(rate))
                    }
                    fetchedAny = true
                }
            }
        } catch (e: Exception) {
            Timber.w(e, "FinanceTool: failed to fetch FX rates")
            sb.appendLine("FX data temporarily unavailable.")
        }

        return if (fetchedAny) {
            ToolResult.Success(
                toolType = toolType,
                content = "Live market data fetched at ${java.util.Date()}:\n\n${sb.toString().trim()}"
            )
        } else {
            ToolResult.Failure(toolType, "All finance data sources unavailable", isFatal = false)
        }
    }

    private fun fetch(url: String): String? {
        val request = Request.Builder().url(url).build()
        return httpClient.newCall(request).execute().use { response ->
            if (response.isSuccessful) response.body?.string() else null
        }
    }

    private fun String.capitalize() = replaceFirstChar { it.uppercase() }
}
