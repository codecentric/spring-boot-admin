#!/usr/bin/env bash
set -euo pipefail

show_help() {
  cat <<EOF
Usage:
  GITHUB_TOKEN=ghp_xxx ./create_milestone_for_tag.sh [--dry-run] owner repo tag [base]

Positional arguments:
  owner     GitHub owner/org (required)
  repo      GitHub repository name (required)
  tag       Tag to create milestone for (required)
  base      Optional base tag/branch/sha to compute commits from (optional)

Flags:
  --dry-run  Print what would be done, do not call APIs that modify data.
  --help     Show this help and exit.

Examples:
  GITHUB_TOKEN=ghp_xxx ./create_milestone_for_tag.sh octocat Hello-World v1.2.0
  ./create_milestone_for_tag.sh --dry-run octocat Hello-World v1.2.0
  ./create_milestone_for_tag.sh octocat Hello-World v1.2.0 previous-tag --dry-run
EOF
}

# Parse args: flags can be anywhere
DRYRUN=0
POSITIONAL=()
for arg in "$@"; do
  case "$arg" in
    --dry-run) DRYRUN=1; shift || true ;;
    --help) show_help; exit 0 ;;
    *) POSITIONAL+=("$arg"); shift || true ;;
  esac
done

# After parsing, positional args are in POSITIONAL array
if [[ ${#POSITIONAL[@]} -lt 3 ]]; then
  echo "ERROR: owner, repo and tag are required."
  show_help
  exit 1
fi

OWNER="${POSITIONAL[0]}"
REPO="${POSITIONAL[1]}"
TAG="${POSITIONAL[2]}"
BASE="${POSITIONAL[3]:-}"  # optional

if [[ $DRYRUN -eq 1 ]]; then
  echo "⚠️  Dry-run mode enabled — no changes will be made."
fi

if [[ -z "${GITHUB_TOKEN:-}" ]]; then
  echo "ERROR: GITHUB_TOKEN environment variable must be set (PAT with repo permissions)."
  exit 1
fi

API="https://api.github.com"
AUTH="Authorization: token ${GITHUB_TOKEN}"
ACCEPT="Accept: application/vnd.github.groot-preview+json"

if ! command -v jq >/dev/null 2>&1; then
  echo "ERROR: jq is required. Install jq and try again."
  exit 1
fi
if ! command -v git >/dev/null 2>&1; then
  echo "ERROR: git is required. Install git and try again."
  exit 1
fi

# If base not provided, attempt to find previous tag ordered by creation date (descending)
if [[ -z "$BASE" ]]; then
  if git rev-parse --verify "$TAG" >/dev/null 2>&1; then
    mapfile -t TAGS < <(git tag --sort=-creatordate)
    PREV_TAG=""
    found=0
    for i in "${!TAGS[@]}"; do
      if [[ "${TAGS[$i]}" == "$TAG" ]]; then
        found=1
        if [[ $((i+1)) -lt ${#TAGS[@]} ]]; then
          PREV_TAG="${TAGS[$((i+1))]}"
        fi
        break
      fi
    done
    if [[ $found -eq 0 ]]; then
      echo "Warning: tag '$TAG' not found locally. You may need to 'git fetch --tags'."
      BASE=""
    else
      if [[ -n "$PREV_TAG" ]]; then
        echo "Detected previous tag: $PREV_TAG"
        BASE="$PREV_TAG"
      else
        echo "No previous tag found (this looks like the oldest tag). Continuing with single tag commit."
        BASE=""
      fi
    fi
  else
    echo "Warning: tag '$TAG' not found locally. Continuing; will attempt API-based fallback if needed."
    BASE=""
  fi
fi

# Build commit list: if BASE is empty, use the single tag's commit; otherwise range base..tag
COMMITS=()
if [[ -n "$BASE" ]]; then
  # verify both reachable locally; if not present, try to fetch tags/branches from remote
  if ! git rev-parse --verify "$BASE" >/dev/null 2>&1 || ! git rev-parse --verify "$TAG" >/dev/null 2>&1; then
    echo "One of base or tag is not present locally. Attempting to fetch tags/heads from origin..."
    git fetch --tags --prune --no-recurse-submodules --quiet || true
  fi

  # Re-check
  git rev-parse --verify "$BASE" >/dev/null 2>&1 || { echo "ERROR: base '$BASE' not found locally after fetch"; exit 1; }
  git rev-parse --verify "$TAG" >/dev/null 2>&1 || { echo "ERROR: tag '$TAG' not found locally after fetch"; exit 1; }

  # list commits from base (exclusive) to tag (inclusive)
  while IFS= read -r sha; do
    COMMITS+=("$sha")
  done < <(git rev-list --reverse "${BASE}..${TAG}")
else
  # try to get tag commit locally; if not present, fall back to GitHub compare API to get commits
  if git rev-parse --verify "$TAG" >/dev/null 2>&1; then
    TAG_SHA="$(git rev-list -n 1 "$TAG")"
    COMMITS+=("$TAG_SHA")
  else
    echo "Tag not present locally. Falling back to GitHub compare API to get commits for tag ${TAG}..."
    # We attempt to compare default branch...tag to get commits — best-effort
    default_branch="$(curl -sSL -H "$AUTH" "${API}/repos/${OWNER}/${REPO}" | jq -r '.default_branch')"
    if [[ -z "$default_branch" || "$default_branch" == "null" ]]; then
      echo "ERROR: could not determine default branch via API."
      exit 1
    fi
    # Use compare endpoint: default_branch...tag
    cmp=$(curl -sSL -H "$AUTH" "${API}/repos/${OWNER}/${REPO}/compare/${default_branch}...${TAG}")
    if echo "$cmp" | jq -e 'has("commits")' >/dev/null 2>&1; then
      mapfile -t commits_from_api < <(echo "$cmp" | jq -r '.commits[]?.sha')
      COMMITS+=("${commits_from_api[@]}")
    else
      echo "API compare did not return commits. Response:"
      echo "$cmp" | jq -C .
      exit 1
    fi
  fi
fi

echo "Found ${#COMMITS[@]} commit(s)."

# For each commit, call GitHub API to get associated PRs
declare -A PRS_MAP=()
for sha in "${COMMITS[@]}"; do
  echo "Querying PRs for commit $sha..."
  resp="$(curl -sSL -H "$AUTH" -H "$ACCEPT" \
    "${API}/repos/${OWNER}/${REPO}/commits/${sha}/pulls")"

  if echo "$resp" | jq -e 'has("message")' >/dev/null 2>&1; then
    msg=$(echo "$resp" | jq -r '.message // empty')
    if [[ -n "$msg" ]]; then
      echo "GitHub API error for commit $sha: $msg"
      echo "Full response:"
      echo "$resp" | jq -C .
      exit 1
    fi
  fi

  pr_numbers=$(echo "$resp" | jq -r '.[]?.number' || true)
  if [[ -n "$pr_numbers" ]]; then
    while IFS= read -r pr; do
      if [[ -n "$pr" ]]; then
        PRS_MAP["$pr"]=1
        echo "  -> PR #$pr"
      fi
    done <<<"$pr_numbers"
  fi
done

if [[ ${#PRS_MAP[@]} -eq 0 ]]; then
  echo "No PRs associated with commits found. Exiting."
  exit 0
fi

PR_LIST=()
for k in "${!PRS_MAP[@]}"; do PR_LIST+=("$k"); done
echo "Total unique PRs to update: ${#PR_LIST[@]}"

# Check existing milestones for a matching title
echo "Checking for existing milestone named '$TAG'..."
MILESTONES_RESP="$(curl -sSL -H "$AUTH" "${API}/repos/${OWNER}/${REPO}/milestones?state=all&per_page=100")"
MILESTONE_NUMBER="$(echo "$MILESTONES_RESP" | jq -r --arg TITLE "$TAG" '.[] | select(.title==$TITLE) | .number' | head -n1 || true)"

if [[ -n "$MILESTONE_NUMBER" && "$MILESTONE_NUMBER" != "null" ]]; then
  echo "Found existing milestone '$TAG' (number: $MILESTONE_NUMBER)."
else
  if [[ $DRYRUN -eq 1 ]]; then
    echo "Would create milestone '$TAG' (dry-run)."
    MILESTONE_NUMBER="DUMMY_ID"
  else
    echo "Creating milestone '$TAG'..."
    create_resp="$(curl -sSL -H "$AUTH" -H "Content-Type: application/json" \
      -d "{\"title\": \"${TAG}\", \"description\": \"Auto-created milestone for tag ${TAG}\"}" \
      "${API}/repos/${OWNER}/${REPO}/milestones")"

    if echo "$create_resp" | jq -e 'has("message")' >/dev/null 2>&1; then
      err=$(echo "$create_resp" | jq -r '.message // empty')
      echo "Error creating milestone: $err"
      echo "Response: $create_resp" | jq -C .
      exit 1
    fi

    MILESTONE_NUMBER="$(echo "$create_resp" | jq -r '.number')"
    if [[ -z "$MILESTONE_NUMBER" || "$MILESTONE_NUMBER" == "null" ]]; then
      echo "Failed to create milestone. Response: $create_resp"
      exit 1
    fi
    echo "Created milestone '${TAG}' (number: $MILESTONE_NUMBER)."
  fi
fi

# Patch each PR (issues endpoint) to set milestone
for prnum in "${PR_LIST[@]}"; do
  if [[ $DRYRUN -eq 1 ]]; then
    echo "Would update PR #${prnum} -> milestone ${MILESTONE_NUMBER}"
    continue
  fi

  echo "Updating PR #${prnum} -> milestone ${MILESTONE_NUMBER}..."
  patch_resp="$(curl -sSL -X PATCH -H "$AUTH" -H "Content-Type: application/json" \
    -d "{\"milestone\": ${MILESTONE_NUMBER}}" \
    "${API}/repos/${OWNER}/${REPO}/issues/${prnum}")"

  if echo "$patch_resp" | jq -e 'has("message")' >/dev/null 2>&1; then
    err=$(echo "$patch_resp" | jq -r '.message // empty')
    echo "Warning: failed to update PR #${prnum}: $err"
    echo "Response: $patch_resp" | jq -C .
    # continue so we attempt remaining PRs
    continue
  fi

  echo "PR #${prnum} updated."
done

echo "Done."
exit 0
