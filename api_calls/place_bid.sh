curl --location 'http://localhost:8000/auctions/1/bid' \
--header 'Content-Type: application/json' \
--header 'Authorization: Bearer {"payload":{"user_id":123,"user_details":{"user_name": "RANDOM USERNAME"}}}' \
--data '{"bidValue": 1}'