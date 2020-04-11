package com.rastiehaiev.aws.dynamodb.rds.sample.model;

import java.util.List;

public class CandidatesResponse extends MultipleResults<Candidate> {

    public CandidatesResponse withElements(List<Candidate> candidates) {
        super.withElements(candidates);
        return this;
    }
}
