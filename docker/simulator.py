#!/usr/bin/env python3
import json
import os
import random
import time
import urllib.request
from datetime import datetime, timezone

CHARGER_ID = os.getenv("CHARGER_ID", "charger-01")
BACKEND_URL = os.getenv("BACKEND_URL", "http://backend:8080/api/telemetry/samples")
INTERVAL_SECONDS = int(os.getenv("PUSH_INTERVAL_SECONDS", "300"))


def main():
    print(f"[{CHARGER_ID}] Starting telemetry simulator (interval: {INTERVAL_SECONDS}s)", flush=True)
    sequence = 0
    cumulative_wh = 1000

    while True:
        sequence += 1
        cumulative_wh += random.randint(20, 80)
        temp = round(20.0 + random.uniform(-1.0, 3.0), 1)

        payload = {
            "chargerId": CHARGER_ID,
            "eventTime": datetime.now(timezone.utc).isoformat(),
            "activeEnergyWh": cumulative_wh,
            "temperature": temp,
            "sequenceNumber": sequence,
        }

        try:
            data = json.dumps(payload).encode("utf-8")
            req = urllib.request.Request(
                BACKEND_URL,
                data=data,
                headers={"Content-Type": "application/json"},
            )
            with urllib.request.urlopen(req, timeout=5) as resp:
                print(f"[{CHARGER_ID}] Sample #{sequence} pushed: HTTP {resp.status}", flush=True)
        except Exception as e:
            print(f"[{CHARGER_ID}] Push error: {e}", flush=True)

        time.sleep(INTERVAL_SECONDS)


if __name__ == "__main__":
    main()
