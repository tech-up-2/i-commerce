/**
 * config/product-thresholds.js
 *
 */

export default {

  'http_req_duration{name:product_search}': [
    { threshold: 'p(95)<1500', abortOnFail: false },
    { threshold: 'p(99)<3000', abortOnFail: false },
  ],
  'http_req_failed{name:product_search}': [
    { threshold: 'rate<0.01', abortOnFail: false },
  ],
  'http_req_failed': [
    { threshold: 'rate<0.05', abortOnFail: false },
  ],

};