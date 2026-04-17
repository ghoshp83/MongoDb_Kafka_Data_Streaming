# VHS tape: MongoDB -> Kafka resumable CDC -- the crash-and-resume test
# Render (from repo root):
#   vhs docs/demo/demo.tape
# Prereq (from the repo root):
#   docker compose -f docker-compose.quickstart.yml down -v
#   docker compose -f docker-compose.quickstart.yml up -d
#   # wait ~20s for mkds-streamer to settle

Output "docs/demo/demo.mp4"
Output "docs/demo/demo.gif"

Set FontSize 18
Set Width 1280
Set Height 720
Set Theme "Catppuccin Mocha"
Set Shell "bash"
Set TypingSpeed 35ms
Set PlaybackSpeed 1.0

Hide
Type "cd docs/demo/demo-steps"
Enter
Type "clear"
Enter
Show

Type "# MongoDB -> Kafka CDC : the crash-and-resume test"
Enter
Type "# Can the streamer resume without losing events -- or replaying them?"
Enter
Sleep 1200ms

Type "# 1. Stack: Mongo replica set + Kafka + the CDC streamer."
Enter
Type "docker ps --format 'table {{.Names}}\t{{.Status}}' | grep mkds"
Enter
Sleep 2500ms

Enter
Type "# 2. Insert 2 orders into MongoDB. Streamer forwards them to Kafka."
Enter
Type "./1-insert-first-batch.sh"
Enter
Sleep 3500ms

Type "# Kafka topic now has those 2 events."
Enter
Type "./2-kafka-peek.sh 2"
Enter
Sleep 3500ms

Enter
Type "# 3. *** The streamer pod crashes mid-flight. ***"
Enter
Type "docker stop mkds-streamer"
Enter
Sleep 2500ms

Enter
Type "# 4. Orders keep arriving while the streamer is DOWN."
Enter
Type "./3-insert-while-down.sh"
Enter
Sleep 3500ms

Type "# Kafka offset: still 2. No new events -- streamer can't forward what it isn't running."
Enter
Type "./4-kafka-end-offset.sh"
Enter
Sleep 2500ms

Enter
Type "# 5. Streamer comes back up. Reads last resume token from disk."
Enter
Type "docker start mkds-streamer && sleep 8"
Enter
Sleep 9s

Type "# Kafka offset: 5. The 3 missed orders were replayed from the oplog position"
Enter
Type "# stored in the resume token -- not lost, not duplicated."
Enter
Type "./4-kafka-end-offset.sh"
Enter
Sleep 2500ms

Enter
Type "# All 5 orders in Kafka, in order, exactly once:"
Enter
Type "./5-kafka-all-orders.sh"
Enter
Sleep 4s

Enter
Type "# Resume-token-in-object-storage. Exactly-once resumability, pod-death proof."
Sleep 3s
