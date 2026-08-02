package com.vela.im.service.message.infrastructure.elasticsearch;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageSearchRepository extends ElasticsearchRepository<MessageDocument, Long> {
}
