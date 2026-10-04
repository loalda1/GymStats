#!/bin/sh
set -eu
name=${1:-screen}
case "$name" in *[!a-zA-Z0-9_-]*|'') echo "Use a simple screenshot name." >&2; exit 1;; esac
cd "$(dirname "$0")/.."
mkdir -p docs/screenshots
adb exec-out screencap -p > "docs/screenshots/$name.png"
printf 'Saved docs/screenshots/%s.png\n' "$name"
