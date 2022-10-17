package bot

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.*
import com.github.kotlintelegrambot.entities.ChatAction
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.github.kotlintelegrambot.entities.TelegramFile
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton
import com.github.kotlintelegrambot.extensions.filters.Filter
import com.github.kotlintelegrambot.logging.LogLevel
import data.remote.WEATHER_API_KEY
import data.remote.models.CurrentWeather
import data.remote.repository.WeatherRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val BOT_ANSWER_TIMEOUT = 30
private const val BOT_TOKEN = "5649745843:AAE8x59F-cCLn5-PsAeW8RQNecQfCWohh2s"
private const val GIF_WAITING_URL = "https://img2.joyreactor.cc/pics/post/webm/%D0%B3%D0%B8%D1%84%D0%BA%D0%B8-%D0%B5%D0%BD%D0%BE%D1%82-%D0%96%D0%B4%D1%83%D0%BD-6651948.gif"

class WeatherBot (private val weatherRepository: WeatherRepository){

    private lateinit var country: String
    private var _chatId: ChatId? = null
    private val chatId by lazy {requireNotNull(_chatId)}

    fun createBot(): Bot {
        return bot{
            timeout = BOT_ANSWER_TIMEOUT
            token = BOT_TOKEN
            logLevel = LogLevel.Error

            dispatch {
                setUpCommands()
                setUpCallbacks()
            }
        }
    }
    private fun Dispatcher.setUpCallbacks() {
        callbackQuery (
            callbackData = "getMyLocation"
        ){
            bot.sendMessage(chatId = chatId, text = "Отправь мне свою локацию")
            location {
                val country = CoroutineScope(Dispatchers.IO).launch {
                    weatherRepository.getReverseGeocodingCountryName(
                        location.latitude.toString(),
                        location.longitude.toString(),
                        "json"
                    ).address.country

                    val inlineKeyboardMarkup = InlineKeyboardMarkup.create(
                        listOf(
                            InlineKeyboardButton.CallbackData(
                                text = "Правильно",
                                callbackData = "yes_label"
                            )
                        )
                    )
                    bot.sendMessage(
                        chatId = chatId,
                        text = "Твой город - ${country}, верно?\n Если нет - скинь еще раз свою локацию",
                        replyMarkup = inlineKeyboardMarkup
                    )
                }
            }
        }

        callbackQuery (callbackData = "enterManually") {
            bot.sendMessage(chatId = chatId, text = "Введи свой город")
            message (Filter.Text){
                country = message.text.toString()

                val inlineKeyboardMarkup = InlineKeyboardMarkup.create(
                    listOf(
                        InlineKeyboardButton.CallbackData(
                            text = "Правильно",
                            callbackData = "yes_label"
                        )
                    )
                )
                bot.sendMessage(
                    chatId = chatId,
                    text = "Твой город - ${country}, верно?\n Если нет - введи свой город повторно",
                    replyMarkup = inlineKeyboardMarkup
                )
            }
        }

        callbackQuery (callbackData = "yes_label") {
            bot.apply {
                sendAnimation(chatId = chatId, animation = TelegramFile.ByUrl(GIF_WAITING_URL))
                sendMessage(chatId = chatId, text = "Узнаю Вашу погоду...")
                sendChatAction(chatId = chatId, action = ChatAction.TYPING)}
            CoroutineScope(Dispatchers.IO).launch {
                val currentWeather = weatherRepository.getCurrentWeather(
                    WEATHER_API_KEY,
                    country,
                    "no"
                )
                bot.sendMessage(
                    chatId = chatId,
                    text = """
                        ☁ Облачность: ${currentWeather.cloud}
                         🌡 Температура (градусы): ${currentWeather.tempDegrees}
                         🙎 ‍Ощущается как: ${currentWeather.feelsLikeDegrees}
                         💧 Влажность: ${currentWeather.humidity}
                         🌪 Направление ветра: ${currentWeather.windDirection}
                         🧭 Давление: ${currentWeather.pressureIn}""".trimIndent()
                )

                bot.sendMessage(
                    chatId = chatId,
                    text ="Если Вы хотите запросить погоду еще раз, введите /weather")
                    country = ""

            }
        }
    }

    private fun Dispatcher.setUpCommands() {
        command("start") {
            _chatId = ChatId.fromId(message.chat.id)
            bot.sendMessage(
                chatId = chatId,
                text = "Привет! Это бот, показывающий погоду. \n Для запуска введи команду /weather"
            )
        }

        command("weather"){
            val inlineKeybordMarkup = InlineKeyboardMarkup.create(
                listOf(
                    InlineKeyboardButton.CallbackData(
                        text = "Ввести город",
                        callbackData = "enterManually"
                    )),
                listOf(
                    InlineKeyboardButton.CallbackData(
                        text = "Определить по геопозиции(для мобильных устройств)",
                        callbackData = "getMyLocation"
                    )
                )
            )
            bot.sendMessage(
                chatId = chatId,
                text = "Для того, чтобы отправить погоду,\nмне нужно знать город",
                replyMarkup = inlineKeybordMarkup
            )
        }
    }
}