package com.bloxbean.cardano.yaci.store.protocolparams.mapper;

import com.bloxbean.cardano.yaci.core.model.ProtocolParamUpdate;
import com.bloxbean.cardano.yaci.store.common.domain.ProtocolParams;
import org.mapstruct.DecoratedWith;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
@DecoratedWith(DomainMapperDecorator.class)
public interface DomainMapper {
    DomainMapper INSTANCE = Mappers.getMapper(DomainMapper.class);

    @Mapping(target = "costModels", ignore = true) //TODO
    @Mapping(target = "extraEntropy", ignore = true)
    ProtocolParams toProtocolParams(ProtocolParamUpdate protocolParamUpdate);
}
