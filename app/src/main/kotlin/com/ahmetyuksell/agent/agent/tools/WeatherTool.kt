package com.ahmetyuksell.agent.agent.tools

import com.ahmetyuksell.agent.domain.agent.Tool
import com.ahmetyuksell.agent.domain.agent.ToolResult
import com.ahmetyuksell.agent.security.SecureKeyStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URLEncoder
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class WeatherTool @Inject constructor(
    private val secureKeyStore: SecureKeyStore
) : Tool {

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    private val lenientJson = Json { ignoreUnknownKeys = true }

    override val name = "get_weather"
    override val description = "Get current weather and 3-day forecast for any city worldwide. No API key needed."
    override val parametersSchema = """
        {
            "type": "object",
            "properties": {
                "city": {
                    "type": "string",
                    "description": "City name e.g. 'Istanbul', 'New York', 'London'"
                },
                "units": {
                    "type": "string",
                    "description": "Temperature units: 'celsius' (default) or 'fahrenheit'"
                }
            },
            "required": ["city"]
        }
    """.trimIndent()

    override suspend fun execute(argsJson: String): ToolResult = withContext(Dispatchers.IO) {
        try {
            val json = Json.parseToJsonElement(argsJson).jsonObject
            val city = json["city"]?.jsonPrimitive?.content
                ?: return@withContext ToolResult.failure("Missing 'city' parameter")
            val units = json["units"]?.jsonPrimitive?.content ?: "celsius"

            val coords = geocodeCity(city)
                ?: return@withContext ToolResult.failure("Could not find city: $city")

            val (lat, lon, displayName) = coords
            val tempUnit = if (units == "fahrenheit") "fahrenheit" else "celsius"
            val unitSymbol = if (tempUnit == "fahrenheit") "°F" else "°C"

            val weatherUrl = "https://api.open-meteo.com/v1/forecast" +
                    "?latitude=$lat&longitude=$lon" +
                    "&current=temperature_2m,weathercode,windspeed_10m,relative_humidity_2m" +
                    "&daily=temperature_2m_max,temperature_2m_min,weathercode" +
                    "&temperature_unit=$tempUnit" +
                    "&forecast_days=3" +
                    "&timezone=auto"

            val weatherRequest = Request.Builder().url(weatherUrl).get().build()
            val weatherResponse = client.newCall(weatherRequest).execute()

            if (!weatherResponse.isSuccessful) {
                return@withContext ToolResult.failure("Weather API error: ${weatherResponse.code}")
            }

            val weatherBody = weatherResponse.body?.string()
                ?: return@withContext ToolResult.failure("Empty weather response")

            val weatherJson = lenientJson.parseToJsonElement(weatherBody).jsonObject
            val current = weatherJson["current"]?.jsonObject
            val daily = weatherJson["daily"]?.jsonObject

            val temp = current?.get("temperature_2m")?.jsonPrimitive?.content ?: "N/A"
            val windspeed = current?.get("windspeed_10m")?.jsonPrimitive?.content ?: "N/A"
            val humidity = current?.get("relative_humidity_2m")?.jsonPrimitive?.content ?: "N/A"
            val weatherCode = current?.get("weathercode")?.jsonPrimitive?.content?.toIntOrNull() ?: 0

            // Open-Meteo returns daily arrays, not objects — access via jsonArray
            val maxTemps = daily?.get("temperature_2m_max")?.jsonArray
                ?.take(3)?.map { it.jsonPrimitive.content } ?: emptyList()
            val minTemps = daily?.get("temperature_2m_min")?.jsonArray
                ?.take(3)?.map { it.jsonPrimitive.content } ?: emptyList()

            val result = buildString {
                appendLine("Weather for $displayName:")
                appendLine("Current: ${temp}$unitSymbol — ${describeWeatherCode(weatherCode)}")
                appendLine("Wind: ${windspeed} km/h | Humidity: ${humidity}%")
                appendLine()
                appendLine("3-Day Forecast:")
                for (i in 0..2) {
                    val day = listOf("Today", "Tomorrow", "Day after tomorrow")[i]
                    val max = maxTemps.getOrNull(i) ?: "N/A"
                    val min = minTemps.getOrNull(i) ?: "N/A"
                    appendLine("  $day: ${min}$unitSymbol – ${max}$unitSymbol")
                }
            }

            ToolResult.success(result.trim())
        } catch (e: Exception) {
            ToolResult.failure("Weather tool error: ${e.message}")
        }
    }

    private fun geocodeCity(city: String): Triple<Double, Double, String>? {
        return try {
            val encoded = URLEncoder.encode(city, "UTF-8")
            val url = "https://geocoding-api.open-meteo.com/v1/search?name=$encoded&count=1&language=en&format=json"
            val request = Request.Builder().url(url).get().build()
            val response = client.newCall(request).execute()

            val body = response.body?.string() ?: return null
            val json = lenientJson.parseToJsonElement(body).jsonObject

            // Open-Meteo geocoding returns "results" as a JSON array, not an object
            val firstResult = json["results"]?.jsonArray?.firstOrNull()?.jsonObject ?: return null

            val lat = firstResult["latitude"]?.jsonPrimitive?.content?.toDoubleOrNull() ?: return null
            val lon = firstResult["longitude"]?.jsonPrimitive?.content?.toDoubleOrNull() ?: return null
            val name = firstResult["name"]?.jsonPrimitive?.content ?: city
            val country = firstResult["country"]?.jsonPrimitive?.content ?: ""

            Triple(lat, lon, "$name, $country")
        } catch (e: Exception) {
            null
        }
    }

    private fun describeWeatherCode(code: Int): String = when (code) {
        0 -> "Clear sky"
        1, 2, 3 -> "Partly cloudy"
        45, 48 -> "Foggy"
        51, 53, 55 -> "Drizzle"
        61, 63, 65 -> "Rain"
        71, 73, 75 -> "Snow"
        80, 81, 82 -> "Rain showers"
        95 -> "Thunderstorm"
        96, 99 -> "Thunderstorm with hail"
        else -> "Unknown conditions"
    }
}
