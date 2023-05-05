package com.bloxbean.cardano.yaci.store.core.storage.impl;

import com.bloxbean.cardano.yaci.store.core.domain.CardanoEra;
import com.bloxbean.cardano.yaci.store.core.storage.api.EraStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EraStorageImpl implements EraStorage {
    private final EraRepository eraRepository;
    private final EraMapper eraMapper;

    @Override
    public void saveEra(CardanoEra era) {
        eraRepository.findById(era.getEra().getValue())
                .ifPresentOrElse(eraEntity -> {
                    //TODO -- Do nothing
                }, () -> {
                    eraRepository.save(eraMapper.toEraEntity(era));
                });
    }

    @Override
    public Optional<CardanoEra> findEra(int era) {
        return eraRepository.findById(era)
                .map(eraEntity -> eraMapper.toEra(eraEntity));
    }

    @Override
    public Optional<CardanoEra> findFirstNonByronEra() {
        return eraRepository.findFirstNonByronEra()
                .map(eraEntity -> eraMapper.toEra(eraEntity));
    }
}
