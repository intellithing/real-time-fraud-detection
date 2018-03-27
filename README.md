# real-time-fraud-detection
Real Time Fraud Detection

Online finical service vendors will most likely face a challenge to detect malicious behaviors quick enough, then disable accounts or block users associated with the malicious behaviors.

To react promptly, they need to collect user behavior data and apply online machine learning based algorithms in real-time to classify accounts at risk.

We introduce a simple fraud detection system. It implements a naive rule engine based on Flink's Streaming API. Code is written in Scala.


### Rules Engine
Below rules may be applied to flag risky accounts.

- Account created from a bad ip
- Account has more than 20 users
- Account created a spammy ticket, for example, any of these:
  - Tickets with a `bit.ly` link
  - Tickets any of these words `Apple`, `Paypal`, `reset password`

### Features
- Flag risky accounts
- Stream the inputs into rules engine through TCP connections
- Handle malformat JSON data

### Netcat
The simplest way to stream text over TCP is using netcat like this:

```bash
/usr/bin/nc -lk 9999
```

### Sample data format

Each line in the data set represents one of the following models:

- Account

  `data_type` is `account`, for example one account can be represented as following:

  ```
  {
    "data_type": "account",
    "id": 1,
    "type": "Trial",
    "created_at": 1517792363,
    "created_from_ip": "202.62.86.10"
  }
  ```

- User

  `data_type` is `user` and users belongs to accounts.
  
  For example one user can be represented as following:

  ```
  {
    "data_type": "user",
    "id": 1,
    "account_id": 1,
    "created_at": 1517792390
  }
  ```

- Ticket

  `data_type` is `ticket` and tickets belongs to accounts.
  
  For example one ticket can be represented as following:

  ```
  {
    "data_type": "ticket",
    "id": 1,
    "account_id": 1,
    "content": "You can reset your password here: http://bit.ly/12zbe09",
    "created_at": 1517803090
  }
  ```

### Output format
All reasons are grouped togethr by account `id`. For example:

```
Account 1 is risky because
- It is created from a known bad ip: 202.62.86.10

Account 2 is risky because
- It created a spammy ticket with bit.ly link
- It created a spammy ticket with Apple word in it
```

