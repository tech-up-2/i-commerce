import thresholds from '../config/threshold.js'; // 공통 성능 목표(Threshold) 가져오기
import { executeMixedLoadFlow } from '../scenarios/mixed-flows.js'; // 시나리오 가져오기

// 오직 부하 설정(RPS, VU, Stage)과 성공 기준(Threshold)만 관리
export let options = {
    scenarios: {
        mixed_load: {
            executor: 'ramping-arrival-rate',
            startRate: 300,
            timeUnit: '1s',
            preAllocatedVUs: 100,
            maxVUs: 150,
            stages: [
                { target: 300, duration: '2m' },
                { target: 400, duration: '3m' },
                { target: 500, duration: '5m' },
                { target: 400, duration: '5m' },
                { target: 0, duration: '2m' }
            ],
            exec: 'scenarioExecutor' // 👈 아래의 실행 함수를 매핑
        }
    },
    thresholds: thresholds
};

export function scenarioExecutor() {
    executeMixedLoadFlow(); // 👈 떼어낸 시나리오 흐름을 여기서 실행!
}
