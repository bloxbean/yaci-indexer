package com.bloxbean.cardano.yaci.store.script.processor;

import com.bloxbean.cardano.client.util.HexUtil;
import com.bloxbean.cardano.yaci.core.model.NativeScript;
import com.bloxbean.cardano.yaci.core.model.PlutusScript;
import com.bloxbean.cardano.yaci.core.model.PlutusScriptType;
import com.bloxbean.cardano.yaci.store.common.util.JsonUtil;
import com.bloxbean.cardano.yaci.store.common.util.ScriptReferenceUtil;
import com.bloxbean.cardano.yaci.store.events.TransactionEvent;
import com.bloxbean.cardano.yaci.store.script.domain.Script;
import com.bloxbean.cardano.yaci.store.script.domain.ScriptType;
import com.bloxbean.cardano.yaci.store.script.helper.ScriptUtil;
import com.bloxbean.cardano.yaci.store.script.storage.ScriptStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.bloxbean.cardano.yaci.store.script.helper.ScriptUtil.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class ScriptRefProcessor {
    private final ScriptStorage scriptStorage;

    @EventListener
    @Transactional
    public void processScriptRefInUtxo(TransactionEvent transactionEvent) {
        try {
            List<Script> scriptList = transactionEvent.getTransactions()
                    .stream()
                    .flatMap(transaction -> transaction.getBody().getOutputs().stream())
                    .map(transactionOutput -> transactionOutput.getScriptRef())
                    .filter(Objects::nonNull)
                    .map(scriptRef -> ScriptReferenceUtil.deserializeScriptRef(HexUtil.decodeHexString(scriptRef))) //todo: refactor with ccl
                    .filter(Objects::nonNull)
                    .map(script -> {
                        ScriptType scriptType = toScriptType(script);
                        String scriptHash = null;

                        try {
                            scriptHash = HexUtil.encodeHexString(script.getScriptHash());
                        } catch (Exception e) {
                            log.error("Unable to get reference script hash, Block hash: " + transactionEvent.getMetadata().getBlockHash(), e);
                        }

                        String content = null;
                        if (scriptType == ScriptType.NATIVE_SCRIPT) {
                            NativeScript nativeScript = NativeScript.builder()
                                    .type(script.getScriptType())
                                    .content(JsonUtil.getJson(script))
                                    .build();
                            content = JsonUtil.getJson(nativeScript);
                        } else {
                            PlutusScript plutusScript = toPlutusScript(script);
                            content = JsonUtil.getJson(plutusScript);
                        }

                        return Script.builder()
                                .scriptHash(scriptHash)
                                .scriptType(scriptType)
                                .content(content)
                                .build();

                    })
                    .collect(Collectors.toList());

            //Save the scripts
            if (scriptList.size() > 0) {
                scriptStorage.saveScripts(scriptList);
            }
        } catch (Exception e) {
            log.error("Error saving script ref in utxo. Block: {} ", transactionEvent.getMetadata().getBlock(), e);
        }
    }
}
