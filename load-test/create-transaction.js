import http from "k6/http";
import { check } from "k6";
import { uuidv4 } from "https://jslib.k6.io/k6-utils/1.4.0/index.js";

// 20 req/s for 2 minutes against a locally-run stack. Rerun it yourself with:
//   docker run --rm --network pix-payment-gateway_default \
//     -v "$PWD/load-test:/scripts" grafana/k6 run /scripts/create-transaction.js
export const options = {
    scenarios: {
        steady_load: {
            executor: "constant-arrival-rate",
            rate: 20,
            timeUnit: "1s",
            duration: "2m",
            preAllocatedVUs: 10,
            maxVUs: 50,
        },
    },
};

const BASE_URL = __ENV.BASE_URL || "http://localhost:8080";

export default function () {
    const payload = JSON.stringify({
        payerAccount: "alice@example.com",
        payeeAccount: "bob@example.com",
        amountCents: 5000,
    });

    const params = {
        headers: {
            "Content-Type": "application/json",
            "Idempotency-Key": uuidv4(),
        },
    };

    const res = http.post(`${BASE_URL}/transactions`, payload, params);
    check(res, { "status is 202": (r) => r.status === 202 });
}
