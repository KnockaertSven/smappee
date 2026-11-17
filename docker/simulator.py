#!/usr/bin/env python3
import json
import os
import random
import signal
import sys
import time
import urllib.error
import urllib.request
from datetime import datetime, timezone

CHARGER_ID = os.getenv("CHARGER_ID", "charger-01")
BACKEND_URL = os.getenv("BACKEND_URL", "http://backend:8080/api/telemetry/samples")
BEHAVIOR = os.getenv("BEHAVIOR", "standard")
PUSH_INTERVAL = int(os.getenv("PUSH_INTERVAL_SECONDS", "300"))

running = True


def handle_shutdown(signum, frame):
    global running
    print(f"[{CHARGER_ID}] Shutting down gracefully...", flush=True)
    running = False


signal.signal(signal.SIGTERM, handle_shutdown)
signal.signal(signal.SIGINT, handle_shutdown)


def send_payload(payload):
    data = json.dumps(payload).encode("utf-8")
    req = urllib.request.Request(
        BACKEND_URL,
        data=data,
        headers={"Content-Type": "application/json"}
    )
    try:
        with urllib.request.urlopen(req, timeout=5) as resp:
            print(f"[{CHARGER_ID}] Push successful: HTTP {resp.status}", flush=True)
            return resp.status
    except urllib.error.HTTPError as e:
        print(f"[{CHARGER_ID}] Backend HTTP error: {e.code} - {e.read().decode('utf-8', errors='replace')}", flush=True)
        return e.code
    except Exception as e:
        print(f"[{CHARGER_ID}] Connection error: {e}", flush=True)
        return None


def main():
    print("==================================================", flush=True)
    print("  Charger Simulator Initialized", flush=True)
    print(f"  ID:          {CHARGER_ID}", flush=True)
    print(f"  Backend:     {BACKEND_URL}", flush=True)
    print(f"  Behavior:    {BEHAVIOR}", flush=True)
    print(f"  Cadence:     {PUSH_INTERVAL}s", flush=True)
    print("==================================================", flush=True)

    sequence_number = 0
    cumulative_wh = 1000
    buffer = []
    last_sent_sample = None
    tick_count = 0

    while running:
        tick_count += 1
        sequence_number += 1
        cumulative_wh += random.randint(20, 80)
        temp = round(20.0 + random.uniform(-2.0, 5.0), 2)
        now_utc = datetime.now(timezone.utc).isoformat()

        sample = {
            "chargerId": CHARGER_ID,
            "eventTime": now_utc,
            "activeEnergyWh": cumulative_wh,
            "temperature": temp,
            "sequenceNumber": sequence_number
        }
        buffer.append(sample)
        print(f"[{CHARGER_ID}] Tick #{sequence_number} sample generated (Buffer: {len(buffer)})", flush=True)

        if BEHAVIOR == "flaky_disconnect":
            # Simulate offline buffering: only attempt push every 3 ticks
            if tick_count % 3 == 0:
                print(f"[{CHARGER_ID}] Reconnecting: pushing buffered batch of {len(buffer)} samples...", flush=True)
                status = send_payload(buffer)
                if status in (200, 202):
                    last_sent_sample = buffer[-1]
                    buffer.clear()
            else:
                print(f"[{CHARGER_ID}] Offline mode: buffered {len(buffer)} samples so far", flush=True)

        elif BEHAVIOR == "revisions_and_chaos":
            # Normal push, but occasionally replay duplicate or push revision
            if tick_count % 4 == 0 and last_sent_sample:
                # Replay identical sample to verify idempotent handling
                print(f"[{CHARGER_ID}] Chaos: replaying previous sample #{last_sent_sample['sequenceNumber']}", flush=True)
                send_payload(last_sent_sample)
            elif tick_count % 5 == 0 and last_sent_sample:
                # Send revision with same eventTime but higher sequenceNumber
                sequence_number += 1
                revised_sample = dict(last_sent_sample)
                revised_sample["sequenceNumber"] = sequence_number
                revised_sample["activeEnergyWh"] += 10
                print(f"[{CHARGER_ID}] Chaos: pushing revision for event {revised_sample['eventTime']} with sequence #{sequence_number}", flush=True)
                send_payload(revised_sample)
            else:
                status = send_payload(buffer if len(buffer) > 1 else buffer[0])
                if status in (200, 202):
                    last_sent_sample = buffer[-1]
                    buffer.clear()

        else:
            # Standard behavior: immediate push, buffering only on network failure
            status = send_payload(buffer if len(buffer) > 1 else buffer[0])
            if status in (200, 202):
                last_sent_sample = buffer[-1]
                buffer.clear()
            else:
                print(f"[{CHARGER_ID}] Push failed, retaining {len(buffer)} samples in buffer", flush=True)

        # Sleep loop responding to shutdown signals
        for _ in range(PUSH_INTERVAL):
            if not running:
                break
            time.sleep(1)

    print(f"[{CHARGER_ID}] Simulator stopped cleanly.", flush=True)


if __name__ == "__main__":
    main()

