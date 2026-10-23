package ua.ofkkt.navshowcase.data

/** Модель предметної області для демо: навчальний курс. */
data class Course(
    val id: Int,
    val title: String,
    val teacher: String,
    val hours: Int,
    val done: Boolean = false,
)

const val NEW_COURSE_ID = -1

object SampleData {
    val courses = listOf(
        Course(1, "Kotlin: основи", "О. Мельник", 24),
        Course(2, "Jetpack Compose", "Р. Ключник", 32),
        Course(3, "Архітектура MVVM", "І. Петренко", 20),
        Course(4, "Room і DataStore", "Н. Коваль", 16),
        Course(5, "Ktor-клієнт", "Д. Шевчук", 12),
        Course(6, "Kotlin Multiplatform", "Р. Ключник", 28),
    )
}

/**
 * Лекція 9, слайд «Стани екрана: Loading / Success / Error».
 * Моделюємо стани через sealed-тип і рендеримо через when(uiState).
 * Empty — окремий стан, а не «Success з порожнім списком».
 */
sealed interface ScreenUiState<out T> {
    data object Loading : ScreenUiState<Nothing>
    data class Success<T>(val data: T) : ScreenUiState<T>
    data class Error(val message: String) : ScreenUiState<Nothing>
    data object Empty : ScreenUiState<Nothing>
}
