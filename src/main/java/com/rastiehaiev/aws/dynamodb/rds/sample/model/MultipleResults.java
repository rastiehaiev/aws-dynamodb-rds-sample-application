package com.rastiehaiev.aws.dynamodb.rds.sample.model;

import lombok.Data;

import java.util.List;

import static org.apache.commons.collections4.ListUtils.emptyIfNull;

@Data
public class MultipleResults<T> {

    private List<T> elements;
    private int total;

    public MultipleResults<T> withElements(List<T> elements) {
        this.elements = emptyIfNull(elements);
        this.total = elements.size();
        return this;
    }
}
