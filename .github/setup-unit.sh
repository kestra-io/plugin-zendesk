docker compose -f docker-compose-ci.yml up -d

for i in {1..30}; do
  if curl -s http://localhost:8084/ >/dev/null; then break; fi
  sleep 1
done

Zendesk_CTN=$(docker compose -f docker-compose-ci.yml ps -q zendesk)
Zendesk_EMAIL=$(docker exec "$Zendesk_CTN" printenv MOCK_ZENDESK_USERNAME)
Zendesk_TOKEN=$(docker exec "$Zendesk_CTN" printenv MOCK_ZENDESK_API_KEY)

cat > src/test/resources/application-test.yaml <<EOF
zendesk:
  url: "http://localhost:8084"
  email: "$Zendesk_EMAIL"
  token: "$Zendesk_TOKEN"
EOF

echo "Zendesk started with user $Zendesk_EMAIL and token $Zendesk_TOKEN"
