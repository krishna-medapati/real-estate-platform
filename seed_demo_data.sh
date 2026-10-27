#!/usr/bin/env bash
# PrimeHaven demo data seeder
# Usage:  chmod +x seed_demo_data.sh && ./seed_demo_data.sh
#
# Distributes property listings across agent2/agent3 (agent1 already has
# plenty) and creates visits + inquiries for client1/client2/client3.
# Property IDs are read back from the API responses, so nothing is hardcoded.

set -u
BASE="${BASE:-http://localhost:8000}"
PASS="test1234"

jq_get() { python3 -c "import sys,json;d=json.load(sys.stdin);print(d.get('$1',''))"; }

login() {
  curl -s -X POST "$BASE/api/auth/login" \
    -H "Content-Type: application/json" \
    -d "{\"username\":\"$1\",\"password\":\"$PASS\"}" | jq_get token
}

register_if_needed() {
  curl -s -X POST "$BASE/api/auth/register" \
    -H "Content-Type: application/json" \
    -d "{\"username\":\"$1\",\"password\":\"$PASS\",\"role\":\"$2\"}" > /dev/null
}

create_property() {
  curl -s -X POST "$BASE/api/properties" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $1" \
    -d "{\"location\":\"$2\",\"price\":$3,\"type\":\"$4\"}" | jq_get propertyId
}

echo "=== Ensuring users exist ==="
register_if_needed agent1  AGENT
register_if_needed agent2  AGENT
register_if_needed agent3  AGENT
register_if_needed client1 CLIENT
register_if_needed client2 CLIENT
register_if_needed client3 CLIENT

echo "=== Logging in ==="
A1=$(login agent1);  A2=$(login agent2);  A3=$(login agent3)
C1=$(login client1); C2=$(login client2); C3=$(login client3)

for t in A1 A2 A3 C1 C2 C3; do
  if [ -z "${!t}" ]; then
    echo "ERROR: login failed for $t -- is the gateway up on $BASE?" >&2
    exit 1
  fi
done
echo "All six tokens acquired."

echo "=== Creating properties (spread across agent2/agent3) ==="
P1=$(create_property "$A2" "7 Jubilee Hills, Hyderabad"           890000  "Residential")
P2=$(create_property "$A2" "221 Gachibowli Tech Park, Hyderabad" 1200000  "Commercial")
P3=$(create_property "$A3" "55 Kondapur Main Road, Hyderabad"     320000  "Residential")
P4=$(create_property "$A3" "9 HITEC City, Hyderabad"              750000  "Commercial")
P5=$(create_property "$A2" "14 Financial District, Hyderabad"    1450000  "Commercial")

echo "Created property IDs: $P1 $P2 $P3 $P4 $P5"

echo "=== Scheduling visits ==="
schedule_visit() {
  curl -s -X POST "$BASE/api/visits" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $1" \
    -d "{\"propertyId\":$2,\"visitDate\":\"$3\"}" | python3 -m json.tool
}

schedule_visit "$C1" "$P1" "2026-10-06T11:30:00"
schedule_visit "$C2" "$P2" "2026-10-07T15:00:00"
schedule_visit "$C3" "$P3" "2026-10-08T09:00:00"
schedule_visit "$C2" "$P4" "2026-10-09T16:00:00"
schedule_visit "$C1" "$P5" "2026-10-10T10:00:00"

echo "=== Sending inquiries ==="
send_inquiry() {
  curl -s -X POST "$BASE/api/inquiries" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $1" \
    -d "{\"propertyId\":$2,\"message\":\"$3\"}" | python3 -m json.tool
}

send_inquiry "$C1" "$P1" "Does this come with reserved parking?"
send_inquiry "$C2" "$P2" "What is the total built-up area?"
send_inquiry "$C3" "$P3" "Is the property ready to move in?"
send_inquiry "$C2" "$P4" "Can we negotiate the price?"

echo "=== Final property list (as agent1) ==="
curl -s -X GET "$BASE/api/properties" -H "Authorization: Bearer $A1" | python3 -m json.tool

echo "=== All visits (agent view) ==="
curl -s -X GET "$BASE/api/visits" -H "Authorization: Bearer $A1" | python3 -m json.tool

echo "=== All inquiries (agent view) ==="
curl -s -X GET "$BASE/api/inquiries" -H "Authorization: Bearer $A1" | python3 -m json.tool

echo "=== Done ==="
