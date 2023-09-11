package com.rm.common.redis.utils;

import com.rm.common.core.exception.ErrorType;
import com.rm.common.core.exception.RmCommonException;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.rm.common.core.exception.ServiceStatusCode.ERROR_SAME_TIME_ACT;


@Component
public class DistributedLock {


    @Autowired
    RedissonClient redisson;
    private RLock lock;

    /**
     *  분산락을 이용하여 동시성 제어
        1. enum에 저장된 Key값으로 락을 얻음
        2. 동시에 어떤 행위를 했을 때 락을 얻은 클라이언트가 먼저 해당 작업을 수행
        3. 락을 못 얻은 클라이언트는 에러 처리 또는 다른 클라이언트가 가진 락을 해제 후 작업 수행
        4. 동시성을 제어하는 곳에서는 락을 얻은 후 프로세스가 끝난 뒤 꼭! 락해제를 해주어야 함 아니면 계속 잠겨있음

     * try {
     *       distributedLock.tryAcquireLock(LockKeyEnum.~);
     *       프로세스...
     *     } finally {
     *           distributedLock.releaseLock();
     *     }
     * finally를 써줘야 프로세스가 흘러가다가 에러가 나도 락해제가 됨
     *

    */
    public void releaseLock() { // 락 해제
        if (lock != null && lock.isHeldByCurrentThread()) {
            lock.unlock();
        }
    }

    // 분산 락을 얻는데 못 얻으면 에러 처리
    public boolean tryAcquireLock(String lockKey) {

        lock = redisson.getLock(lockKey); // 락 설정

        boolean isLocked = lock.tryLock(); // 해당 클라이언트가 락을 얻었는지 확인

        if (!isLocked) {
            // 락을 획득하지 못한 경우 다른 로직 수행 false
            System.out.println("락을 획득하지 못하여 다른 로직을 수행합니다.");
            // 에러 처리
            throw new RmCommonException(ErrorType.ERROR, ERROR_SAME_TIME_ACT, "ERROR_SAME_TIME_ACT !!");
        }
        // 락을 획득한 경우 작업 수행 true
        System.out.println("락을 획득하여 작업을 수행합니다.");
        return isLocked; // 락 상태를 리턴 무조건 true 나옴 void써도 상관 없는데 BO단에서 혹시 또 처리 할까봐 return 넣음
    }



    public boolean tryAcquireLockWait(String lockKey) {  // 분산 락을 얻는데 못 얻으면 다른 클라이언트가 락이 해제 될때까지 기다렸다가 풀리면 실행

        lock = redisson.getLock(lockKey); // 락 설정

        boolean isLocked = lock.tryLock(); // 해당 클라이언트가 락을 얻었는지 확인

        if (!isLocked) {
            // 락을 획득하지 못한 경우 다른 로직 수행 false
            System.out.println("락을 획득하지 못하여 다른 로직을 수행합니다.");
            lock.lock(); // 다른 클라이언트에서 락이 해제 될 때 락을 얻음 그떄까지 대기 무한
//            lock.lock(10, TimeUnit.SECONDS); // 다른 클라이언트에서 락이 해제 될 때 락을 얻음 그떄까지 대기 10초 대기
        }
        // 락을 획득한 경우 작업 수행 true
        System.out.println("락을 획득하여 작업을 수행합니다.");
        return isLocked;
    }

}
