#!/usr/bin/env python3
import os
import sys
import time
import signal
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

def main():
    print(f"==================================================", flush=True)
    print(f"  Charger Simulator Initialized", flush=True)
    print(f"  ID:          {CHARGER_ID}", flush=True)
    print(f"  Backend:     {BACKEND_URL}", flush=True)
    print(f"  Behavior:    {BEHAVIOR}", flush=True)
    print(f"  Cadence:     {PUSH_INTERVAL}s", flush=True)
    print(f"==================================================", flush=True)

    sequence_number = 0
    cumulative_wh = 1000

    while running:
        sequence_number += 1
        now_utc = datetime.now(timezone.utc).isoformat()
        print(f"[{CHARGER_ID}] Tick #{sequence_number} at {now_utc} (Profile: {BEHAVIOR})", flush=True)

        # Sleep loop that responds promptly to shutdown signals
        for _ in range(PUSH_INTERVAL):
            if not running:
                break
            time.sleep(1)

    print(f"[{CHARGER_ID}] Simulator stopped cleanly.", flush=True)

if __name__ == "__main__":
    main()
