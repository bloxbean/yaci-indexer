package com.bloxbean.cardano.yaci.store.api.utxo.controller;

import com.bloxbean.cardano.yaci.store.api.utxo.service.UtxoService;
import com.bloxbean.cardano.yaci.store.common.domain.AddressUtxo;
import com.bloxbean.cardano.yaci.store.common.domain.UtxoKey;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@Tag(name = "Transaction Service")
@RequestMapping("${apiPrefix}/utxos")
@RequiredArgsConstructor
@ConditionalOnExpression("${store.utxo.endpoints.transaction.enabled:true}")
public class UtxoController {

    private final UtxoService utxoService;

    @GetMapping(value = "/{txHash}/{index}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Optional<AddressUtxo> getUtxo(@PathVariable @Pattern(regexp = "^[0-9a-fA-F]{64}$") String txHash, @PathVariable Integer index) {
        return utxoService.getUtxo(txHash, index);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<AddressUtxo> getUtxos(@RequestBody List<UtxoKey> utxoIds) {
        return utxoService.getUtxos(utxoIds);
    }

}