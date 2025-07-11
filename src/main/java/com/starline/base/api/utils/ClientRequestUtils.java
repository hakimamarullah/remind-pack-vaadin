package com.starline.base.api.utils;

import org.springframework.data.domain.Pageable;
import org.springframework.web.util.UriBuilder;

import java.net.URI;
import java.util.Map;
import java.util.function.Function;

public class ClientRequestUtils {

    private ClientRequestUtils() {

    }


    public static Function<UriBuilder, URI> withPageable(String path, Pageable pageable, Map<String, String> extraParams) {
        return builder -> {
            builder.path(path)
                    .queryParam("page", pageable.getPageNumber())
                    .queryParam("size", pageable.getPageSize());
            pageable.getSort().forEach(order ->
                    builder.queryParam("sort", order.getProperty() + "," + order.getDirection()));
            extraParams.forEach(builder::queryParam);
            return builder.build();
        };
    }


}
