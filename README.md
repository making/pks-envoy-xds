# REST API server that implements Envoy xDS for PKS Master nodes

```
./mvnw package
java -jar target/pks-envoy-xds-0.0.1-SNAPSHOT.jar
```

```
docker run --rm \
  -v $(pwd)/envoy.yml:/etc/envoy/envoy.yaml \
  -p 10000:10000 \
  -p 9901:9901 \
  envoyproxy/envoy-dev:48082bcd22fe9165eb73bed6d27857f578df63b5 \
  /usr/local/bin/envoy -l debug -c /etc/envoy/envoy.yaml
```


```
curl localhost:10000 -H "Host: demo1.pks.example.com"
```