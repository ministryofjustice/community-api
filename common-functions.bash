#!/bin/bash

# ensures that we are consistent - relies on gsed for mac users
SED=$([[ $(uname) == 'Darwin' ]] && echo 'gsed' || echo 'sed')
# shellcheck disable=SC2086
if ! type $SED &>/dev/null; then
  echo "Unable to find $SED - on a mac you'll need to install it"
  exit 1
fi
export SED

function git_current_branch() {
  local ref
  ref=$(command git symbolic-ref --quiet HEAD 2> /dev/null)
  local ret=$?
  if [[ $ret != 0 ]]; then
    [[ $ret == 128 ]] && return  # no git repo.
    ref=$(command git rev-parse --short HEAD 2> /dev/null) || return
  fi
  echo "${ref#refs/heads/}"
}

# stashes any changes and switches to main - used at the beginning of scripts
function git_stash_changes_to_main() {
  CHANGES=$(git status -s | grep -v '??' | wc -l)
  [[ $CHANGES -eq 0 ]] || git stash
  git checkout main || git checkout master
  git pull
  export CHANGES
}

# re-applies any local changes - used at the end of scripts
function git_stash_pop_changes() {
  [[ $CHANGES -eq 0 ]] || git stash pop
}