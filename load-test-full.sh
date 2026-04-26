#!/bin/bash

echo "========================================"
echo "  Job Queue System — Load Test"
echo "========================================"

BASE_URL="http://localhost:8080"
JOBS=50

# Step 1 — Check app is running
echo "Checking app health..."
HEALTH=$(curl -s $BASE_URL/api/v1/health)
echo "Health: $HEALTH"

# Step 2 — Record metrics before
echo ""
echo "Metrics BEFORE load test:"
curl -s $BASE_URL/actuator/prometheus | grep -E "^jobs_(submitted|success|failed)_total"

# Step 3 — Submit jobs and measure time
echo ""
echo "Submitting $JOBS jobs..."
START=$(date +%s%N)

for i in $(seq 1 $JOBS); do
    curl -s -X POST $BASE_URL/api/v1/jobs \
        -H "Content-Type: application/json" \
        -d "{\"jobType\":\"send_email\",\"payload\":{\"to\":\"test$i@gmail.com\",\"index\":$i}}" \
        > /dev/null
done

END=$(date +%s%N)
TOTAL_MS=$(( (END - START) / 1000000 ))
AVG_MS=$((TOTAL_MS / JOBS))

echo "$JOBS jobs submitted in ${TOTAL_MS}ms"
echo "⚡ Average submission latency: ${AVG_MS}ms per job"
echo "Throughput: $((JOBS * 1000 / TOTAL_MS)) jobs/second"

# Step 4 — Wait for worker to process all jobs
echo ""
echo "Waiting for worker to process all jobs..."
sleep 30

# Step 5 — Check queue is empty
QUEUE_DEPTH=$(docker exec jqs_redis redis-cli llen job_queue)
echo "Queue depth after processing: $QUEUE_DEPTH"

# Step 6 — Record metrics after
echo ""
echo "Metrics AFTER load test:"
curl -s $BASE_URL/actuator/prometheus | grep -E "^jobs_(submitted|success|failed)_total"

# Step 7 — Check DB
echo ""
echo "Database job counts:"
docker exec jqs_postgres psql -U jqs_user -d jqs_db -c \
    "SELECT status, COUNT(*) FROM jobs GROUP BY status;"

echo ""
echo "========================================"
echo "  Load Test Complete"
echo "========================================"