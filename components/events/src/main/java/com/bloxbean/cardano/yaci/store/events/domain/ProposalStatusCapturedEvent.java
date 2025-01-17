package com.bloxbean.cardano.yaci.store.events.domain;


import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class ProposalStatusCapturedEvent {
    private int epoch;
    private long slot;
}
