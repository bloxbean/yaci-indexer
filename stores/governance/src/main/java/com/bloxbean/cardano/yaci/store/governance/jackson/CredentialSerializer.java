package com.bloxbean.cardano.yaci.store.governance.jackson;

import com.bloxbean.cardano.yaci.core.model.Credential;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

//TODO: add to yaci
public class CredentialSerializer extends JsonSerializer<Credential> {

    @Override
    public void serialize(Credential credential, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        jsonGenerator.writeFieldName(mapper.writeValueAsString(credential));
    }
}
