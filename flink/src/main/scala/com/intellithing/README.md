#### Run Scala code with sbt
$ sbt "flink/run --port 9000"
[info] Loading settings from plugins.sbt ...
[info] Loading project definition from /Users/intelliuser/intellithing/rules-engine/project
[info] Loading settings from build.sbt ...
....
Account 1 is risky because
- It is created from a known bad ip: 202.62.86.10
Account 2 is risky because
- It is created from a known bad ip: 192.30.71.11

```

### netcat input stream
$ /usr/bin/nc -l 9000
{ "data_type":"account", "id": 1, "type": "Trial", "created_at": 1517792363, "created_from_ip": "202.62.86.10" }
{ "data_type":"account", "id": 2, "type": "Customer", "created_at": 1517942625, "created_from_ip": "192.30.71.11" }
{ "data_type":"account", "id": 3, "type": "Trial", "created_at": 1517847244, "created_from_ip": "103.194.89.77" }
{ "data_type":"account", "id": 4, "type": "Trial", "created_at": 1517928354, "created_from_ip": "119.61.25.226" }
{ "data_type":"account", "id": 5, "type": "Trial", "created_at": 1517848756, "created_from_ip": "187.157.189.150" }
{ "data_type":"account", "id": 6, "type": "Customer", "created_at": 1517888092, "created_from_ip": "187.157.189.150" }
{ "data_type":"account", "id": 7, "type": "Customer", "created_at": 1517923355, "created_from_ip": "37.72.216.38" }
{ "data_type":"account", "id": 8, "type": "Trial", "created_at": 1517845209, "created_from_ip": "62.94.244.231" }
{ "data_type":"account", "id": 9, "type": "Trial", "created_at": 1517861933, "created_from_ip"94.244.231" }
{ "data_type":"account", "id": 10, "type": "Customer", "created_at": 1517861251, "created_from_ip": "103.206.130.182" }
