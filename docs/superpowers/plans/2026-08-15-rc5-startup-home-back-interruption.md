# GameFold PC RC5 Startup HOME/BACK Interruption Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make HOME/BACK safely cancel or leave every GameFold PC launch phase without a permanent loading surface.

**Architecture:** A generation-scoped pre-launch ticket owns the asynchronous pipeline through the final navigation event. Home and root Back use pure policy decisions to cancel that ticket and clear launch presentation. XServer owns a nullable Back callback and clears launch presentation before preserving a not-yet-mapped runtime.

**Tech Stack:** Kotlin, Compose, coroutines, JUnit 4, Android Gradle Plugin.

## Global Constraints

- Base remains the accepted `0.8.2-rc4 / 10134` candidate; RC5 becomes `0.8.2-rc5 / 10135`.
- Preserve V0.8.1 formal baseline and RC4 controller reattachment behavior.
- Do not clear app data during vivo installation.

---

### Task 1: Cancellable pre-launch ownership

**Files:**
- Modify: `app/src/main/java/app/gamenative/gamefold/startup/GameFoldPcRuntimeProvisionCoordinator.kt`
- Modify: `app/src/test/java/app/gamenative/gamefold/startup/GameFoldPcRuntimeProvisionerTest.kt`

**Interfaces:**
- Produces: `GameFoldPcPreLaunchTicket`, `tryStart(appId): GameFoldPcPreLaunchTicket?`, `cancel(appId): Boolean`, `isActive(ticket): Boolean`, `finish(ticket)`.

- [ ] Add failing tests proving cancellation invalidates only the old ticket and permits an immediate same-game restart.
- [ ] Run the focused test and confirm the old Boolean single-flight API fails the new contract.
- [ ] Implement ticket ownership and exact-ticket cleanup.
- [ ] Run the focused test green and commit.

### Task 2: Input and route interruption policy

**Files:**
- Modify: `app/src/main/java/app/gamenative/gamefold/ui/GameFoldPcFrontendPolicy.kt`
- Modify: `app/src/main/java/app/gamenative/gamefold/session/GameFoldSessionPolicy.kt`
- Modify: corresponding policy tests.

**Interfaces:**
- Produces: Home launch-control decision and `GameFoldRootBackAction` routing decision.

- [ ] Add failing tests for HOME/B/Y canceling a Home launch card and for Back prioritizing cancel, live game callback, navigation, then ignore.
- [ ] Run focused policy tests red.
- [ ] Implement the minimal pure policies.
- [ ] Run focused policy tests green and commit.

### Task 3: Wire cancellation across Home, pre-launch, ViewModel, and XServer

**Files:**
- Modify: `HomeScreen.kt`, `PluviaMain.kt`, `MainViewModel.kt`, `XServerScreen.kt`.

**Interfaces:**
- Consumes: ticket ownership and policy decisions from Tasks 1-2.
- Produces: one cancel-to-Home action, guarded launch handoff, nullable game Back ownership, and pre-pause splash cleanup.

- [ ] Route Home controller cancel buttons and system Back into the shared cancellation action.
- [ ] Guard every asynchronous pre-launch boundary and hand the ticket to `MainViewModel.launchApp` until navigation is emitted.
- [ ] Clear both loading states before XServer HOME pauses the runtime.
- [ ] Release XServer Back ownership with `null`.
- [ ] Run focused tests plus production compilation and commit.

### Task 4: RC5 release and device verification

**Files:**
- Modify: `app/build.gradle.kts`, version test, RC5 candidate manifest.
- Create: `releases/candidates/V0.8.2-DUAL-SCREEN-RC5/GameFold-PC-V0.8.2-DUAL-SCREEN-RC5-10135.apk`.

- [ ] Red/green the version identity test for `0.8.2-rc5 / 10135`.
- [ ] Run all Legacy Release unit tests and assemble the signed APK.
- [ ] Verify badging, signer, size, and SHA-256; archive the candidate.
- [ ] Install in place on vivo, verify installed/pulled APK identity, and exercise early and late HOME/BACK startup paths.
