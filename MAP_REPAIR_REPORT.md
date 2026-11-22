# PathAsan Map Repair Report

## Problems found in the supplied project

1. The evacuation screen deliberately displayed no destinations or markers.
2. No evacuation destination model or shared destination data source existed.
3. `GeoapifyRepository.kt` and evacuation map models did not exist.
4. There was no Geoapify request, response parser, route state, distance, duration,
   or map route overlay.
5. GPS used only `FusedLocationProviderClient.lastLocation`, so it could return a
   stale value or no value and did not provide live updates.
6. The map supported only a current-location marker. It had no destination markers,
   selected marker, marker click handling, or route camera fitting.
7. The My Location map control did nothing when permission/location was unavailable.
8. There was no cancellation path for an in-flight route when selection changed.
9. The four coordinates in the task brief had no attached authoritative source.
   Treating them as official evacuation centers would be unsafe.
10. No duplicate competing destination or routing implementation was present. The
    expected functionality was missing rather than duplicated.

## Corrections implemented

- Added one immutable destination catalog used by the list, selected panel, map
  markers, and routing.
- Added unique IDs, descriptions, coordinates, and designation status to every
  destination.
- Preserved the four supplied coordinates without inventing replacements and marked
  every record `PROPOSED_UNVERIFIED`.
- Added normal red destination markers, a larger dark selected marker, and a blue
  current-device marker.
- Added marker click handling and synchronized map/list selection.
- Added actual Geoapify walking requests with `mode=walk`, start-to-destination
  waypoint order, HTTP/auth/rate-limit/timeout/network/empty-response handling, and
  cancellable connections.
- Added recursive GeoJSON coordinate parsing with the required longitude/latitude to
  latitude/longitude conversion.
- Added actual route polyline replacement, route camera fitting, distance, and
  estimated walking time.
- Added continuous fused-location updates, a 15-second initial timeout, support for
  coarse or fine permission, disabled-services handling, retry, location settings,
  and My Location recovery behavior.
- Kept the existing Baguio CBD camera center, OpenFreeMap style, interactive
  gestures, MapLibre lifecycle bridge, offline-region implementation, application
  ID, namespace, SDK levels, Room data, navigation, and unrelated features.
- Added secure local Geoapify configuration through ignored `local.properties` or a
  build environment variable. No API key is included.
- Added explicit UI and documentation that directions are not available offline.

## Destination data status

| ID | Name | Latitude | Longitude | Status |
|---|---|---:|---:|---|
| `melvin-jones-football-grounds` | Melvin Jones Football Grounds | 16.409338 | 120.595391 | Proposed; manual verification required |
| `sunshine-park` | Sunshine Park | 16.408913 | 120.596516 | Proposed; manual verification required |
| `burnham-park` | Burnham Park | 16.409938 | 120.595438 | Proposed; manual verification required |
| `baguio-athletic-bowl` | Baguio Athletic Bowl | 16.410438 | 120.595297 | Proposed; manual verification required |

No reliable official source was included in the project archive, so none of these
records is labeled as an officially designated evacuation center. Baguio CDRRMO
must verify the records before that designation is changed.

## Modified files

- `app/build.gradle.kts`
- `app/src/main/java/com/pathasan/app/PathAsanApplication.kt`
- `app/src/main/java/com/pathasan/app/location/LocationClient.kt`
- `app/src/main/java/com/pathasan/app/navigation/AppNavigation.kt`
- `app/src/main/java/com/pathasan/app/ui/components/PathAsanMap.kt`
- `app/src/main/java/com/pathasan/app/ui/screens/EvacuationRoutesScreen.kt`
- `app/src/main/java/com/pathasan/app/viewmodel/AppViewModels.kt`
- `gradlew` (restored the executable bit lost in the supplied ZIP)

## New files

- `app/src/main/java/com/pathasan/app/data/EvacuationMapModels.kt`
- `app/src/main/java/com/pathasan/app/data/GeoapifyRepository.kt`
- `local.properties.example`
- `README.md`
- `MAP_REPAIR_REPORT.md`
- `app/src/test/java/com/pathasan/app/data/EvacuationDestinationsTest.kt`

## Removed files

None.

## Validation performed

- Inspected the complete supplied archive and searched for all map, marker,
  destination, location, routing, API-key, and offline-map implementations.
- Checked package declarations, imports, dependency versions, namespace,
  application ID, SDK levels, and navigation/ViewModel wiring.
- Confirmed all map/list/routing destination consumers use the same catalog.
- Confirmed the routing parser reverses GeoJSON `[longitude, latitude]` values.
- Confirmed selecting a different destination cancels the previous request and
  clears the previous route.
- Confirmed source delimiter counts are balanced as a lightweight source check.

## Testing still required

Native compilation and device testing remain pending in the delivery environment.
JDK 17 and Gradle 8.9 were provisioned successfully and dependency resolution was
reached, but no Android SDK is installed (`ANDROID_HOME` and `sdk.dir` are absent),
so Gradle stops before Kotlin compilation. In Android Studio, run:

```text
./gradlew testDebugUnitTest
./gradlew assembleDebug
```

Then test on at least one physical device:

- approximate-only and precise location permission;
- denied permission and later grant from Android settings;
- GPS disabled, cold location acquisition, timeout, and live movement;
- missing, invalid, and valid Geoapify keys;
- no network, API errors, repeated destination changes, and repeated routes;
- marker/list synchronization and camera fit;
- offline map after a complete download and process restart;
- screen sizes and font-scale settings supported by the app.

The current hosted map provider's offline-use terms must also be reviewed manually
before production distribution.
