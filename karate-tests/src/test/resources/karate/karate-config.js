function fn() {
  var config = {
    baseUrl: karate.properties['base.url'] || 'http://localhost:8090'
  };
  karate.configure('connectTimeout', 5000);
  karate.configure('readTimeout', 10000);
  return config;
}
