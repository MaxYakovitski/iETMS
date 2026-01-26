# Native Messaging Protocol (IET)

## Overview
This document defines the communication contract between:
- Chrome Extension
- Native Messaging Host
- Desktop Application (Bridge)

The protocol is stable and backward-compatible.

---

## Transport
- stdin / stdout
- Message framing: 4-byte little-endian length prefix
- Encoding: UTF-8 JSON

---

## Requests

### PING
Request:
{ "type": "PING" }

Response:
{ "type": "PONG" }

---

### GET_TOKEN
Request:
{ "type": "GET_TOKEN" }

Responses:

Success:
{ "type": "TOKEN", "token": "<jwt>" }

Desktop not running or user not authenticated:
{ "type": "NO_TOKEN" }

Error:
{ "type": "ERROR", "message": "..." }

---

## Error Handling
- Unknown message types MUST return ERROR
- Native host MUST NOT crash on malformed input

---

## Versioning
- Protocol version is implicit (v1)
- Any breaking change requires new message type or version prefix
