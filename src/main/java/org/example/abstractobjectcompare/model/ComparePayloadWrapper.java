package org.example.abstractobjectcompare.model;

public record ComparePayloadWrapper(ComparePayload source, ComparePayload target) {

    public ComparePayloadWrapper concat(ComparePayloadWrapper other) {
        return new ComparePayloadWrapper(source.concat(other.source), target.concat(other.target));
    }

}

