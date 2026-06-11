#!/usr/bin/env bash
# =============================================================================
# check-private-names.sh
# UserPromptSubmit hook — blocks prompts that contain private/confidential names.
#
# Exit codes:
#   0  = prompt is clean, allow it through
#   2  = private name detected, block the prompt and show error to the user
# =============================================================================

# ---------------------------------------------------------------------------
# Read the hook payload from stdin and extract the prompt text
# ---------------------------------------------------------------------------
INPUT=$(cat)
PROMPT=$(echo "$INPUT" | python3 -c "
import sys, json
try:
    data = json.load(sys.stdin)
    print(data.get('prompt', ''))
except Exception:
    pass
" 2>/dev/null)

if [[ -z "$PROMPT" ]]; then
  # Could not parse payload — fail open (allow) to avoid blocking all prompts
  exit 0
fi

# ---------------------------------------------------------------------------
# Private names / identifiers to block
# Add one entry per line. Matching is case-insensitive.
# ---------------------------------------------------------------------------
PRIVATE_NAMES=(
  "John Doe"
  "Jane Doe"
  # Add real names, internal project codenames, or other sensitive identifiers below:
  # "Project Phoenix"
  # "Alice Müller"
)

# ---------------------------------------------------------------------------
# Check each name against the prompt
# ---------------------------------------------------------------------------
for name in "${PRIVATE_NAMES[@]}"; do
  if echo "$PROMPT" | grep -qi "$name"; then
    echo "BLOCKED: Your prompt contains a private name: \"$name\"." >&2
    echo "Please remove sensitive identifiers before submitting." >&2
    exit 2
  fi
done

exit 0
