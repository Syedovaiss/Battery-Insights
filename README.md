# Battery Monitorer

Battery Monitorer is an Android app for tracking battery behavior, visualizing trends, and giving practical battery-health insights.

## Current Scope

- Real-time battery tracking (charge, temperature, charging state, signal, screen status).
- Insights engine for drain anomalies and battery behavior.
- Settings-driven customization (theme, dynamic colors, alerts, polling interval, charge limit).
- Export flows (`CSV`, `JSON`, `PDF`) from Settings.
- Usage history chart with labels and animation.

## Production Readiness Plan

This project should now prioritize quality/hardening before adding more features.

### What To Do Before Production

1. **Stability pass**
   - Run full QA matrix: cold start, rotate, background/foreground, reboot, permissions denied.
   - Validate behavior when battery broadcast data is missing/partial.

2. **Crash/ANR monitoring**
   - Add Firebase Crashlytics.
   - Add analytics breadcrumbs for key flows (startup, worker schedule, export, navigation, settings changes).

3. **Data reliability**
   - Verify history/report generation over multiple days.
   - Cover edge cases: no data, one sample, charging transitions.

4. **Battery impact audit**
   - Validate polling intervals and receiver behavior.
   - Confirm app overhead remains low with background work enabled.

5. **Export hardening**
   - Test large datasets for `CSV`/`JSON`/`PDF`.
   - Validate share targets and storage/IO exceptions.

6. **Play compliance/docs**
   - Add privacy policy.
   - Complete Play Data Safety form.
   - Add disclosures for notifications and telemetry.

7. **UI consistency and accessibility**
   - Replace remaining hardcoded strings/colors.
   - Validate contrast, large font scaling, and TalkBack labels.

Detailed execution checklist: see `PRODUCTION_CHECKLIST.md`.
QA execution matrix: see `QA_TEST_MATRIX.md`.
Crash/ANR setup: see `FIREBASE_SETUP.md`.

## Add More Features Now?

- **For production:** prioritize quality/hardening over more features.
- **For marketing differentiation:** add only 1-2 polished features after stabilization:
  - battery degradation trend over weeks
  - charging session history with best/worst sessions

Suggested roadmap: see `ROADMAP.md`.
