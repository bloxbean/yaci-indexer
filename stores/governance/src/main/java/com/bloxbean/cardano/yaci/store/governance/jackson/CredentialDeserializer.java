package com.bloxbean.cardano.yaci.store.governance.jackson;

import com.bloxbean.cardano.yaci.core.model.Credential;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

//TODO: add to yaci
public class CredentialDeserializer extends KeyDeserializer {
    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Object deserializeKey(String s, DeserializationContext deserializationContext)
            throws IOException {
        return objectMapper.readValue(s, Credential.class);
    }
}
