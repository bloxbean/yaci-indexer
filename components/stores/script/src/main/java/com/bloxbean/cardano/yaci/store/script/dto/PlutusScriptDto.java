package com.bloxbean.cardano.yaci.store.script.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class PlutusScriptDto extends ScriptDto {
    private String scriptHash;
    private String content;
    private String type;
}