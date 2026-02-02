package com.hh.documentapplication.scheduler;

import com.hh.documentapplication.service.WorkersService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
@RequiredArgsConstructor

@Component
public class ScheduledWorkers {

    private final WorkersService workersService;

    @Scheduled(fixedDelay = 60_000)
    public void submitWorker() {
        workersService.submitWorker();
    }

    @Scheduled(fixedDelay = 60_000)
    public void approveWorker() {
        workersService.approveWorker();
    }


}
