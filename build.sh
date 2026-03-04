#!/usr/bin/env bash
# B2B Platform - Build and collect all service JARs for deployment
# Run from project root: ./build.sh
# Requires: Maven (mvn) on PATH
# Output: all runnable JARs in ./deploy/

set -e
ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DEPLOY_DIR="$ROOT_DIR/deploy"

# Build order: common-api first (install), then runnable modules
# Format: "relative-path|install_or_package"
MODULES=(
    "common-api|install"
    "eureka-server|package"
    "api-gateway|package"
    "services/authentication-service|package"
    "services/b2c-integration-service|package"
    "services/bet-service|package"
    "services/operator-service|package"
    "services/session-service|package"
    "services/wallet-service|package"
)

get_executable_jar() {
    local target_dir="$1"
    find "$target_dir" -maxdepth 1 -name "*.jar" -type f \
        ! -name "*-sources.jar" \
        ! -name "*-javadoc.jar" \
        ! -name "*.original.jar" \
        -exec ls -S {} + 2>/dev/null | head -1
}

# Clean and create deploy folder
rm -rf "$DEPLOY_DIR"
mkdir -p "$DEPLOY_DIR"

cd "$ROOT_DIR"
for entry in "${MODULES[@]}"; do
    path="${entry%%|*}"
    goal="${entry##*|}"
    dir="$ROOT_DIR/$path"
    name=$(basename "$path")
    if [[ ! -d "$dir" ]]; then
        echo "Skip (not found): $path"
        continue
    fi
    echo ""
    echo ">>> Building $name ..."
    (cd "$dir" && mvn clean "$goal" -q -DskipTests)
    if [[ "$goal" == "package" ]]; then
        target_dir="$dir/target"
        jar=$(get_executable_jar "$target_dir")
        if [[ -n "$jar" ]]; then
            cp "$jar" "$DEPLOY_DIR/$(basename "$jar")"
            echo "    Copied: $(basename "$jar")"
        else
            echo "    Warning: No executable JAR found in target"
        fi
    fi
done
cd "$ROOT_DIR"

echo ""
echo "Done. JARs are in: $DEPLOY_DIR"
ls -1 "$DEPLOY_DIR"/*.jar 2>/dev/null | while read -r f; do echo "  - $(basename "$f")"; done
