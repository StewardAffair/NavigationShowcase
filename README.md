# Navigation Showcase — тестовий застосунок до Лекцій 7 і 9

Курс «Мобільна розробка: Android + Kotlin Multiplatform».

- **Лекція 7** — «Навігація (огляд), структура екранів»
- **Лекція 9** — «Побудова багатоекранних застосунків»

Кожен екран застосунку демонструє один аспект лекцій. Угорі кожного екрана є картка: **які слайди він ілюструє** і **що саме спробувати**.

## Запуск

1. Android Studio Ladybug (2024.2) або новіша, JDK 17.
2. `File → Open` → папка `NavigationShowcase` → дочекатися Gradle Sync.
3. Run на емуляторі або пристрої (minSdk 24, compileSdk 35).

> ⚠️ Проєкт згенеровано без доступу до Maven, тому код **не компілювався** автоматично.
> Імпорти й іконки перевірено скриптом, код вичитано окремо вручну, але якщо Gradle покаже помилку — надішліть лог.

## Лекція → слайд → екран → файл

### Лекція 7 · Навігація (огляд), структура екранів

| Слайд | Тема | Екран у застосунку | Файл |
|---|---|---|---|
| 4 | Single-Activity парадигма | Single-Activity vs Multi-Activity | `l7/SingleActivityDemo.kt`, `LegacyDetailActivity.kt` |
| 13 | Single-Activity vs Multi-Activity (таблиця) | там само | там само |
| 5 | Навігаційний граф: концепція | NavHost, NavController, destinations | `l7/NavBasicsDemo.kt` |
| 6 | NavHost та NavController | там само | там само |
| 7 | composable() destinations (route-константи) | там само (`sealed class SandboxRoute`) | там само |
| 8 | Передача аргументів між екранами | Аргументи навігації | `l7/ArgumentsDemo.kt` |
| 9 | Back stack: як це працює (popUpTo) | Back stack і popUpTo | `l7/BackStackDemo.kt` |
| 10 | Deep links: огляд | Deep links | `l7/DeepLinksDemo.kt`, `app/AppRoot.kt`, `AndroidManifest.xml` |
| 11 | Патерни навігації: Bottom Nav, Drawer | Bottom Navigation і Drawer | `l7/TopLevelPatternsDemo.kt` |
| 12 | Вкладені графи (nested graphs) | Вкладені графи | `l7/NestedGraphsDemo.kt` |
| 14 | Навігація та process death | Process death і SavedStateHandle | `l7/ProcessDeathDemo.kt` |
| 17 | SavedStateHandle і аргументи навігації | там само | там само |
| 15 | Планування навігації проєкту | Планування навігації: перевірка графа | `l7/NavPlanningDemo.kt` |
| 16 | Практика: ескіз навігаційного графа | там само | там само |
| 18 | Приклад: граф для CRUD-застосунку | Міні-застосунок «Курси» | `crud/*.kt` |
| 2, 3, 19, 20 | Вступ, план, підсумок, питання | Головний екран, зміст картки лекції | `app/HubScreen.kt` |

### Лекція 9 · Побудова багатоекранних застосунків

| Слайд | Тема | Екран у застосунку | Файл |
|---|---|---|---|
| 4 | Структурування composable-екранів | Структура composable-екрана | `l9/ScreenStructureDemo.kt` |
| 5 | Перевикористовувані компоненти | Перевикористовувані компоненти | `l9/ReusableComponentsDemo.kt`, `ui/common/AppComponents.kt` |
| 6 | State Hoisting: основи | State hoisting | `l9/StateHoistingDemo.kt` |
| 7 | Передача даних між екранами | Передача даних між екранами | `l9/SharedViewModelDemo.kt` |
| 8 | Обробка системної кнопки «Назад» | BackHandler | `l9/BackHandlerDemo.kt` |
| 9 | Scaffold: каркас екрана | Scaffold, TopAppBar, BottomBar | `l9/ScaffoldSlotsDemo.kt` |
| 10 | TopAppBar та BottomBar | там само + Bottom Navigation | `l9/ScaffoldSlotsDemo.kt`, `l7/TopLevelPatternsDemo.kt` |
| 11 | Патерн список / деталі | Міні-застосунок «Курси» | `crud/*.kt` |
| 12 | Стани екрана: Loading / Success / Error | UI-стани | `l9/UiStatesDemo.kt`, `data/Models.kt` |
| 13 | Адаптивні макети: базово | Адаптивні макети | `l9/AdaptiveLayoutsDemo.kt` |
| 14 | Типові помилки багатоекранних застосунків | Чек-лист міцного скелета | `l9/SkeletonChecklistDemo.kt` |
| 15 | Чек-лист міцного скелета застосунку | там само | там само |
| 16 | Практика: скелет навігації проєкту | там само | там само |
| 17 | Обробка помилок мережі в UI | Обробка помилок мережі в UI | `l9/ErrorHandlingDemo.kt` |
| 18 | Pull-to-refresh патерн | Pull-to-refresh | `l9/PullToRefreshDemo.kt` |
| 2, 3, 19, 20 | Вступ, план, підсумок, питання | Головний екран | `app/HubScreen.kt` |

## Структура проєкту

```
app/src/main/java/ua/ofkkt/navshowcase/
├── MainActivity.kt          Single-Activity: MainActivity → NavHost → Composable
├── LegacyDetailActivity.kt  «старий» підхід Multi-Activity (лише для порівняння)
├── app/
│   ├── Catalog.kt           список екранів-демо + мапінг на слайди (DemoItem)
│   ├── AppRoot.kt           кореневий NavHost, DemoHost, deep link, onNewIntent
│   └── HubScreen.kt         головний екран-каталог
├── l7/                      демо Лекції 7 (9 екранів)
├── l9/                      демо Лекції 9 (10 екранів)
├── crud/                    підсумковий мінізастосунок «Курси»
│   ├── CrudApp.kt           вкладений граф, route-константи, спільний ViewModel
│   ├── CrudViewModel.kt     graph-scoped ViewModel (StateFlow + Channel подій)
│   ├── CourseListScreen.kt  Route (stateful) + Screen (stateless)
│   ├── CourseDetailScreen.kt
│   └── CourseEditScreen.kt  валідація + BackHandler
├── data/Models.kt           Course, ScreenUiState (sealed interface)
└── ui/
    ├── common/AppComponents.kt   AppCard, PrimaryButton, LoadingIndicator, ErrorState, EmptyState, UiStateContent
    ├── common/Components.kt      DemoSection, SandboxFrame, BackStackViewer, MiniScreen, CompareTable…
    └── theme/                    MaterialTheme (світла/темна схема)
```

## Як влаштовано демо навігації

- **Пісочниця** (`SandboxFrame`) — рамка фіксованої висоти з власним `NavHost` і `NavController`. Безпечно експериментувати, не ламаючи навігацію самого застосунку.
- **`BackStackViewer`** — живе відображення back stack (`navController.currentBackStack`), верхівка — перший рядок.
- Системна кнопка «Назад» спершу знімає екран у пісочниці (її callback зареєстровано пізніше), а коли пісочниця на стартовому екрані — працює кореневий NavHost.

## Що виходить за межі слайдів (додано свідомо)

| Тема | Де |
|---|---|
| Route / Screen: stateful-обгортка + stateless-екран | `crud/CourseListScreen.kt` |
| Одноразові події (Snackbar) через `Channel` у ViewModel | `crud/CrudViewModel.kt` |
| Collapsing-шапки Medium/Large + `nestedScroll` | `l9/ScaffoldSlotsDemo.kt` |
| `saveState` / `restoreState` для вкладок | `l7/TopLevelPatternsDemo.kt` |
| Вкладений Scaffold: `WindowInsets(0)`, щоб не дублювати відступи | `ui/common/Components.kt` (`NoInsets`) |
| `onNewIntent` → `handleDeepLink` для `singleTop` Activity | `app/AppRoot.kt` |
| Стан «Не знайдено» замість краху для неіснуючого ID | `crud/CourseDetailScreen.kt` |

## Нотатки до слайдів

1. **Л7, слайд 17.** `savedStateHandle["itemId"] ?: ""` з типом `String` працює, лише якщо аргумент оголошено як `StringType` (за замовчуванням). Якщо для `itemId` вказано `NavType.IntType` (як на слайді 8), значення буде `Int` і з'явиться `ClassCastException`: треба `savedStateHandle.get<Int>("itemId")`.
2. **Л7, слайд 9.** `popUpTo(0)` — числовий id залишився від XML-навігації. У Compose зручніше `popUpTo(navController.graph.id) { inclusive = true }` або `popUpTo(route)`. У демо використано саме так.
3. **Л7, слайд 6.** `popBackStack()` на останньому записі стеку видаляє й його — NavHost стає порожнім. Це видно в демо «Back stack»; у кнопках пісочниць стоїть перевірка `previousBackStackEntry != null`.
4. **Л9, слайд 13.** У `BoxWithConstraints` значення `maxWidth` / `maxHeight` доступні лише у scope самого `BoxWithConstraints`. Усередині вкладеного `Column { }` неявним отримувачем є `ColumnScope`, тому значення треба спершу скопіювати в локальну змінну (див. `AdaptiveSample`).
5. **Л9, слайд 18.** `PullToRefreshBox` у Material3 1.3 ще позначений `@ExperimentalMaterial3Api` — потрібен `@OptIn`.
6. **Л9, слайди 9–10.** «TopAppBar — на рівні екрана, BottomBar — на рівні NavHost» означає вкладені `Scaffold`. Щоб відступи під статус-бар не додавалися двічі, внутрішнім Scaffold / TopAppBar передають `windowInsets = WindowInsets(0)`.
7. **Л9, слайд 8.** `BackHandler` має пріоритет над callback NavHost, бо його зареєстровано пізніше. Стрілка «Назад» у `TopAppBar` — це окремий клік: її треба обробити так само, як системну кнопку (див. `CourseEditScreen`).
8. **Л7, слайд 10.** Для зовнішнього deep link, окрім `navDeepLink` у графі, потрібен `intent-filter` у маніфесті; для `singleTop` Activity ще й передача нового Intent у `navController.handleDeepLink`.

## Перевірка deep link з комп'ютера

```
adb shell am start -a android.intent.action.VIEW -d "navshowcase://demo/item/42" ua.ofkkt.navshowcase
```

## Перевірка process death

```
adb shell am kill ua.ofkkt.navshowcase     # застосунок має бути згорнутий
```

Після цього відкрийте застосунок із Recent apps: збережуться лише `rememberSaveable` і `SavedStateHandle`.
