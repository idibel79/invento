# Invento (inventaire-bam)

Native Android app (Kotlin, Jetpack Compose), `applicationId = ma.bam.inventaire`.
GitHub repo: `idibel79/invento`, remote `origin` = `git@github.com:idibel79/invento.git`, branch `master`.
GitHub Project board: "invento", project number **6**, owner `idibel79`
(project id `PVT_kwHOAuiUNs4Bl6Fc`, Status field id `PVTSSF_lAHOAuiUNs4Bl6FczhklFj8`).

## Project board workflow (standing instruction, from 2026-10-12)

Status column option ids:
- Backlog `676ef411` — **ignore these**, never pick up work from Backlog.
- Todo `f75ad846` — the only column to pull work from.
- In progress `47fc9ee4`
- Test `080a96eb`
- Done `98236657`

Flow for every task:
1. Only take cards that are in **Todo**. Never start work on a Backlog card on your own
   initiative — it only moves to Todo when the user decides to prioritize it.
2. When starting work on a card, move it to **In progress**.
3. Implement the change following "Architecture & code conventions" below, run the quality
   checks (see "Quality checks"), **without** bumping the version.
4. Install a test build on the user's phone (see "Build & install"). Do **not** test or
   drive the app yourself (see "Testing").
5. When the implementation is installed, move the card to **Test** and tell the user it's ready
   to verify on-device. Do not put `Closes #N` / `Fixes #N` in commit messages — that would
   auto-close the issue and auto-move the card, bypassing this flow. Mention the issue number in
   prose instead (e.g. "issue #5").
6. Wait for the user's explicit validation ("valide et push"). Only then run the release steps
   in "Versioning" (bump, clean build, install, commit, PR, merge once CI is green, tag) and move
   the card to **Done**.
   If the user instead reports a problem, keep the card in Test (or move back to In progress)
   and fix it — expect it to return to Test after the fix.

Moving cards: `gh project item-list 6 --owner idibel79 --format json` to get the item id, then
`gh project item-edit --id <item-id> --project-id PVT_kwHOAuiUNs4Bl6Fc --field-id PVTSSF_lAHOAuiUNs4Bl6FczhklFj8 --single-select-option-id <option-id>`.

`gh` may not be on PATH in older shells — use the full path
`C:\Users\013259.BAMAD\AppData\Local\Programs\gh\bin\gh.exe` if needed. Auth token needs the
`project` scope (`gh auth refresh -s project`) in addition to `repo`/`read:org`/`gist`.

## Versioning

While the user is trying a change: incremental build + install, **no** version bump, nothing
committed. Only once the user validates explicitly ("valide et push"):
- Bump `versionCode` (+1) and `versionName` (semantic patch, e.g. `2.6.29` → `2.6.30`) in
  `app/build.gradle.kts`. Nothing else needs updating — `HomeScreen.kt` reads the version live
  via `BuildConfig.VERSION_NAME`.
- Clean build with every check (`gradlew.bat clean testDebugUnitTest lintDebug detekt ktlintCheck
  assembleDebug assembleRelease bundleRelease compileDebugAndroidTestKotlin`), then reinstall the
  validated build on the phone (`adb install -r`, never uninstall).
- Commit on a branch `release/vX.Y.Z` created from an up-to-date `master` (never on `master`),
  with a message describing the change (UTF-8 **without BOM**: `git commit -F <file>` with a file
  written without BOM, or several `-m`).
- `git push -u origin release/vX.Y.Z`, then open a PR to `master` (`gh pr create --base master`).
- `master` is protected by a GitHub ruleset: no direct push, no force push, and the `build` and
  `instrumented` checks must be green before merging. Wait for the CI, fix it on the branch if it
  fails, then merge (`gh pr merge --squash --delete-branch`). Never bypass or disable the ruleset.
- After the merge: `git checkout master && git pull`, then an annotated tag `vX.Y.Z` on that
  merged commit and `git push origin vX.Y.Z` (tags don't trigger the CI).

## Build & install

- Test build: `./gradlew.bat installDebug` (incremental; or `compileDebugKotlin` to just check
  compilation). Behind the BAM proxy, add `-Djavax.net.ssl.trustStoreType=Windows-ROOT` when
  Gradle needs to download something.
- Install an APK on the connected phone: `adb install -r <path-to-apk>` (adb is in
  `%LOCALAPPDATA%\Android\Sdk\platform-tools` if not on PATH). Never `adb uninstall`: it wipes
  the inventories.
- `.gitignore` already excludes `app/build`, `.gradle`, `local.properties`, `*.apk`, `/backups`,
  `/.claude`, `/.kotlin`, `keystore.properties`, `*.jks`, `*.keystore` — don't commit build
  output, the APK or signing material.

## Architecture & code conventions

Rules in force since the refactoring lots 1–5 (v2.6.101–v2.6.105). Follow the existing code as
the model; `ui/inventorydetail` is the reference screen.
- **UI**: one Hilt ViewModel per screen exposing a single `StateFlow<…UiState>`, collected with
  `collectAsStateWithLifecycle`. Each screen is split into a stateful composable (`hiltViewModel()`
  + collection + one-shot events) and a stateless one (`uiState` + lambdas, e.g.
  `InventoryDetailActions`). Sub-components live in their own files. Main components get a
  `@LightDarkPreviews` preview wrapped in `PreviewSurface`.
- **No `Context` or Android API in ViewModels**: go through injected interfaces
  (`SessionExporter`, `ImportFileSource`) and injected dispatchers (`@IoDispatcher`,
  `@DefaultDispatcher` from `di/DispatchersModule.kt`) so ViewModels stay JVM-testable.
- **Business logic** is pure Kotlin in `domain/` and unit-tested on the JVM: `EcartCalculator`
  is the only definition of the écart and its thresholds (the SQL counters interpolate
  `SEUIL_ECART_NUL`), plus `ArticleFilters`, `ArticleSearch`, `ArticleStats`.
- **Data**: `InventoryRepository` (interface) + `InventoryRepositoryImpl`. Counters and lists are
  exposed as Room `Flow`s (no manual refresh); several writes that belong together go in one
  `@Transaction` (e.g. `InventorySessionDao.insertWithArticles` for imports).
- **Texts** shown to the user live in `res/values/strings.xml` (plurals via `pluralLabel` in
  `ui/components/Plurals.kt`). Visuals follow the `invento-design` skill.
- **Tests**: every new piece of logic, ViewModel or DAO comes with tests — JVM tests with
  `FakeInventoryRepository` and `kotlinx-coroutines-test` (`app/src/test`), DAO and migration
  tests in `app/src/androidTest` (`InventoryDaoTest`, `MigrationTest`).

## Database

The inventories on the phone must **always** survive an update.
- Never use `fallbackToDestructiveMigration()`.
- Any entity change = `AppDatabase.VERSION` + 1, a `Migration` added to
  `AppDatabase.ALL_MIGRATIONS`, the exported schema committed (`app/schemas/.../<version>.json`)
  and a migration test following the model of `MigrationTest` (create the DB in the old version,
  insert data, migrate, check nothing is lost).
- `DatabaseMigrationsTest` (JVM) fails the CI if a migration step or an exported schema is
  missing. The 5→6 migration can't be tested (no `5.json` was ever exported — don't fake one).

## Quality checks

- Before every test install: `gradlew.bat testDebugUnitTest detekt ktlintCheck lintDebug`
  (`-Djavax.net.ssl.trustStoreType=Windows-ROOT` behind the BAM proxy if something must be
  downloaded). All existing tests stay green.
- Never add entries to the baselines (`app/config/ktlint/baseline.xml`,
  `app/config/detekt/baseline.xml`, `app/lint-baseline.xml`): fix the code instead, and shrink the
  baselines for the files you touch when practical.
- GitHub CI (`.github/workflows/android.yml`) runs on push to `master` and on pull requests (one
  run per push, older runs cancelled): build, release build, unit tests, lint, detekt, ktlint,
  and instrumented tests (`connectedDebugAndroidTest`) on an emulator. Both jobs (`build`,
  `instrumented`) are required checks for merging into `master`.

## Release / Play Store

- The release is minified (R8 + `isShrinkResources`); keep rules in `app/proguard-rules.pro`.
  When touching dependencies, Room entities or reflected models, install and have the user test
  the release build (`assembleRelease` + `adb install -r`), not only the debug one.
- Signing: `signingConfigs.release` reads `keystore.properties` at the repo root (never
  committed; template `keystore.properties.example`). Without it the release is signed with the
  debug key. The user creates the keystore — never generate keys or passwords.
- `gradlew.bat bundleRelease` produces the `.aab` for the Play Store (Play App Signing, the
  keystore is the upload key).
- Switching the phone from the debug-signed app to the Play-signed one requires an uninstall,
  which wipes the data: the user must export the inventories first.

## Testing

The user tests every change manually on their own device. Do not open the app, tap through
screens, or take screenshots via adb to "confirm" a feature works — build, install, stop, and
report what was built. This was a hard lesson from burning significant time on an
adb-tap-coordinate mismatch while self-testing issue #32 and a filter menu; the user now owns
all manual verification.

## Multi-user support

Not planned/started yet (tracked as issue #33, kept in Backlog deliberately). Future design
will need zone-level locking, hybrid online/offline support, and a 3-state session lifecycle —
revisit this when the user decides to prioritize it, don't start it from Backlog on your own.
