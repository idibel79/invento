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
2. When starting work on a card, move it to **In progress** and create a branch `issue-N`
   (N = the issue number) from the latest `origin/master`. All commits for the card go there,
   never directly on `master`.
3. Implement the change, following the versioning workflow below.
4. Build and install the APK on the user's phone (see "Build & install"). Do **not** test or
   drive the app yourself (see "Testing").
5. When the implementation is installed:
   - `git push -u origin issue-N` and open a PR `issue-N` → `master` (`gh pr create`). CI
     (`.github/workflows/ci.yml`) builds the APK and runs the unit tests on it.
   - Move the card to **Test** and tell the user it's ready to verify on-device, with the PR link.
   Do not put `Closes #N` / `Fixes #N` in commit messages or the PR body — that would
   auto-close the issue and auto-move the card, bypassing this flow. Mention the issue number in
   prose instead (e.g. "issue #5").
6. Wait for the user to report back that manual testing passed. Only then:
   - merge the PR with a merge commit (`gh pr merge <N> --merge --delete-branch`, not squash or
     rebase, so the version tag stays on `master`'s history), once CI is green,
   - `git push origin vX.Y.Z` to publish the tag, and
   - move the card to **Done**.
   This is automatic at this point, no need to ask separately.
   If the user instead reports a problem, keep the card in Test (or move back to In progress),
   fix it on the same branch and push again — expect it to return to Test after the fix.

Moving cards: `gh project item-list 6 --owner idibel79 --format json` to get the item id, then
`gh project item-edit --id <item-id> --project-id PVT_kwHOAuiUNs4Bl6Fc --field-id PVTSSF_lAHOAuiUNs4Bl6FczhklFj8 --single-select-option-id <option-id>`.

`gh` may not be on PATH in older shells — use the full path
`C:\Users\013259.BAMAD\AppData\Local\Programs\gh\bin\gh.exe` if needed. Auth token needs the
`project` scope (`gh auth refresh -s project`) in addition to `repo`/`read:org`/`gist`.

## Versioning

After every code change (before considering a task done):
- Bump `versionCode` (+1) and `versionName` (semantic patch, e.g. `2.6.29` → `2.6.30`) in
  `app/build.gradle.kts`. Nothing else needs updating — `HomeScreen.kt` reads the version live
  via `BuildConfig.VERSION_NAME`.
- Commit with a message describing the change, then tag it `vX.Y.Z` (annotated tag).
- Push only to the card's `issue-N` branch (step 5 above). `master` only changes by merging the
  PR after the user confirmed Test passed, or when the user explicitly asks.
- If `master` moved while the branch was in Test and the version collides, merge `master` into
  the branch, bump to the next free version and re-tag before merging.

## Build & install

- Build: `./gradlew.bat assembleDebug` (or `compileDebugKotlin` to just check compilation).
- Install on the connected phone: `adb install -r <path-to-apk>`.
- `.gitignore` already excludes `app/build`, `.gradle`, `local.properties`, `*.apk`, `/backups`,
  `/.claude`, `/.kotlin` — don't commit build output or the APK.

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
