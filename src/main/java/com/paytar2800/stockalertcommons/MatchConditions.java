package com.paytar2800.stockalertcommons;

import java.util.Objects;


public class MatchConditions {

    HttpMethod method;
    String path;


    private MatchConditions(HttpMethod method, String path) {
        this.method = method;
        this.path = path;
    }

    public static MatchConditions create(HttpMethod method, String path) {
        return new MatchConditions(method, path);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MatchConditions that = (MatchConditions) o;
        return method == that.method &&
                path.equals(that.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(method, path);
    }
}
