function fn() {
  var baseUrl = java.lang.System.getProperty('karate.baseUrl');
  if (!baseUrl) {
    baseUrl = 'http://localhost:8080';
  }
  return { baseUrl: baseUrl };
}
