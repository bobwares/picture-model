#!/usr/bin/env bash

set -euo pipefail

SERVER_IP="chad"
SHARE_NAME="C"
MOUNT_POINT="$HOME/mnt"

SMB_USER="robertadelmann@yahoo.com"
SMB_PASS="9Monthsold"

# Use single quotes and let the shell handle special chars
SMB_URL="//'${SMB_USER}':'${SMB_PASS}'@${SERVER_IP}/${SHARE_NAME}"

if [[ ! -d "${MOUNT_POINT}" ]]; then
  mkdir -p "${MOUNT_POINT}"
fi

echo "Mounting ${SERVER_IP}/${SHARE_NAME}..."
mount_smbfs "${SMB_URL}" "${MOUNT_POINT}"

echo "Mounted at ${MOUNT_POINT}"