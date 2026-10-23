package ua.ofkkt.navshowcase.crud

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import ua.ofkkt.navshowcase.data.Course
import ua.ofkkt.navshowcase.data.NEW_COURSE_ID
import ua.ofkkt.navshowcase.data.SampleData
import ua.ofkkt.navshowcase.data.ScreenUiState

/** Одноразове повідомлення для Snackbar (з можливістю «Повторити»). */
data class UiMessage(val text: String, val canRetry: Boolean = false)

/**
 * Лекція 9: «Спільний ViewModel (Graph-scoped)». Один екземпляр на граф "courses_graph" —
 * його спільно використовують екрани списку, деталей і редагування.
 * Джерело даних — імітація репозиторію в пам'яті із затримкою.
 */
class CrudViewModel : ViewModel() {
    private val store = SampleData.courses.toMutableList()
    private var nextId = (store.maxOfOrNull { it.id } ?: 0) + 1

    private val _state = MutableStateFlow<ScreenUiState<List<Course>>>(ScreenUiState.Loading)
    val state: StateFlow<ScreenUiState<List<Course>>> = _state.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _simulateError = MutableStateFlow(false)
    val simulateError: StateFlow<Boolean> = _simulateError.asStateFlow()

    private val messageChannel = Channel<UiMessage>(Channel.BUFFERED)
    val messages = messageChannel.receiveAsFlow()

    val instanceId: Int = System.identityHashCode(this)

    init {
        load()
    }

    fun setSimulateError(value: Boolean) {
        _simulateError.value = value
    }

    /** Перше завантаження → стан Loading; оновлення, коли дані вже є → лише isRefreshing. */
    fun load() {
        viewModelScope.launch {
            val hasData = _state.value is ScreenUiState.Success
            if (hasData) _isRefreshing.value = true else _state.value = ScreenUiState.Loading
            delay(1200) // імітація запиту до репозиторію
            if (_simulateError.value) {
                if (hasData) {
                    messageChannel.trySend(UiMessage("Немає з'єднання. Показано збережені дані", canRetry = true))
                } else {
                    _state.value = ScreenUiState.Error("Не вдалося завантажити курси. Перевірте з'єднання.")
                }
            } else {
                publish()
            }
            _isRefreshing.value = false
        }
    }

    fun save(course: Course) {
        if (course.id == NEW_COURSE_ID) {
            store.add(course.copy(id = nextId++))
            messageChannel.trySend(UiMessage("Курс додано"))
        } else {
            val index = store.indexOfFirst { it.id == course.id }
            if (index >= 0) store[index] = course
            messageChannel.trySend(UiMessage("Зміни збережено"))
        }
        publish()
    }

    fun toggleDone(id: Int) {
        val index = store.indexOfFirst { it.id == id }
        if (index >= 0) {
            store[index] = store[index].copy(done = !store[index].done)
            publish()
        }
    }

    fun delete(id: Int) {
        store.removeAll { it.id == id }
        messageChannel.trySend(UiMessage("Курс видалено"))
        publish()
    }

    private fun publish() {
        _state.value = if (store.isEmpty()) ScreenUiState.Empty else ScreenUiState.Success(store.toList())
    }
}
