# Run from any directory; uses the installed Android Studio JDK when available.
$ErrorActionPreference = 'Stop'
$project = Split-Path $PSScriptRoot -Parent
$studioJdk = Join-Path $env:ProgramFiles 'Android/Android Studio/jbr'
if (Test-Path (Join-Path $studioJdk 'bin/java.exe')) { $env:JAVA_HOME = $studioJdk }
Push-Location $project
try {
    & ./gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:lintDebug :app:assembleDebugAndroidTest --console=plain
    if ($LASTEXITCODE -ne 0) { throw "Gradle finalizó con código $LASTEXITCODE" }
    Write-Output 'APK: app/build/outputs/apk/debug/app-debug.apk'
} finally { Pop-Location }
