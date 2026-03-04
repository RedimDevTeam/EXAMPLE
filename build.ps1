# B2B Platform - Build and collect all service JARs for deployment
# Run from project root. Requires Maven (mvn) on PATH.
# Output: all runnable JARs in ./deploy/

$ErrorActionPreference = "Stop"
$RootDir = $PSScriptRoot
$DeployDir = Join-Path $RootDir "deploy"

# Build order: common-api first (install to local repo), then runnable modules
$Modules = @(
    @{ Path = "common-api";           Install = $true },   # library - install only
    @{ Path = "eureka-server";        Install = $false },
    @{ Path = "api-gateway";          Install = $false },
    @{ Path = "services\authentication-service"; Install = $false },
    @{ Path = "services\b2c-integration-service"; Install = $false },
    @{ Path = "services\bet-service"; Install = $false },
    @{ Path = "services\operator-service"; Install = $false },
    @{ Path = "services\session-service"; Install = $false },
    @{ Path = "services\wallet-service"; Install = $false }
)

function Get-ExecutableJar {
    param([string]$TargetDir)
    Get-ChildItem -Path $TargetDir -Filter "*.jar" -File |
        Where-Object {
            $_.Name -notmatch "\-sources\.jar$" -and
            $_.Name -notmatch "\-javadoc\.jar$" -and
            $_.Name -notmatch "\.original\.jar$"
        } |
        Sort-Object Length -Descending |
        Select-Object -First 1
}

# Clean and create deploy folder
if (Test-Path $DeployDir) {
    Remove-Item -Path $DeployDir -Recurse -Force
}
New-Item -ItemType Directory -Path $DeployDir -Force | Out-Null

Push-Location $RootDir
try {
    foreach ($m in $Modules) {
        $dir = Join-Path $RootDir $m.Path
        if (-not (Test-Path $dir)) {
            Write-Warning "Skip (not found): $($m.Path)"
            continue
        }
        $name = Split-Path -Leaf $dir
        Write-Host ""
        Write-Host ">>> Building $name ..." -ForegroundColor Cyan
        Set-Location $dir
        if ($m.Install) {
            mvn clean install -q -DskipTests
            if ($LASTEXITCODE -ne 0) { throw "mvn install failed: $name" }
        } else {
            mvn clean package -q -DskipTests
            if ($LASTEXITCODE -ne 0) { throw "mvn package failed: $name" }
            $targetDir = Join-Path $dir "target"
            $jar = Get-ExecutableJar -TargetDir $targetDir
            if ($jar) {
                Copy-Item -Path $jar.FullName -Destination (Join-Path $DeployDir $jar.Name) -Force
                Write-Host "    Copied: $($jar.Name)" -ForegroundColor Green
            } else {
                Write-Warning "    No executable JAR found in target"
            }
        }
        Set-Location $RootDir
    }
    Write-Host ""
    Write-Host "Done. JARs are in: $DeployDir" -ForegroundColor Green
    Get-ChildItem $DeployDir -Filter "*.jar" | ForEach-Object { Write-Host "  - $($_.Name)" }
} finally {
    Pop-Location
}
