package com.bloxbean.cardano.yaci.store.protocolparams.processor;

import com.bloxbean.cardano.yaci.store.protocolparams.service.ProtocolParamService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.DependsOn;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@DependsOn({"localClientProvider"})
@Slf4j
public class ProtocolParamsProcessor {
    private ProtocolParamService protocolParamService;

    public ProtocolParamsProcessor(ProtocolParamService protocolParamService) {
        this.protocolParamService = protocolParamService;
    }

    @Scheduled(fixedRate = 5, timeUnit = TimeUnit.MINUTES)
    public void scheduleFetchAndSetCurrentProtocolParams() {
        log.info("Fetching protocol params ....");
        protocolParamService.fetchAndSetCurrentProtocolParams();
    }
}
