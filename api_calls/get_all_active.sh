curl --location 'http://localhost:8000/auctions/active' \
--header 'Authorization: Bearer {"payload":{"user_id":123,"user_details":{"user_name": "RANDOM USERNAME"}}}'