package com.liluo.moyan.infrastructure.mq;

import com.liluo.moyan.modules.work.dto.EsSyncMessage;
import com.liluo.moyan.modules.work.entity.Work;
import com.liluo.moyan.modules.work.mapper.WorkMapper;
import com.liluo.moyan.modules.search.service.SearchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * ES 同步消费者
 * 监听作品变更消息，异步同步到 Elasticsearch
 */
@Slf4j
@Component
public class EsSyncConsumer {
    
    @Autowired
    private SearchService searchService;
    
    @Autowired
    private WorkMapper workMapper;
    
    /**
     * 监听 ES 同步队列
     */
    @RabbitListener(queues = "es.sync.queue")
    public void handleEsSync(EsSyncMessage message) {
        try {
            log.info("收到ES同步消息: workId={}, action={}", message.getWorkId(), message.getAction());
            
            switch (message.getAction()) {
                case "CREATE":
                case "UPDATE":
                    // 查询最新数据
                    Work work = workMapper.selectById(message.getWorkId());
                    if (work != null && work.getDeleted() == 0) {
                        searchService.indexWork(work);
                        log.info("作品 {} ES索引成功", message.getWorkId());
                    }
                    break;
                    
                case "DELETE":
                    searchService.deleteWorkIndex(message.getWorkId());
                    log.info("作品 {} ES索引删除成功", message.getWorkId());
                    break;
                    
                default:
                    log.warn("未知的操作类型: {}", message.getAction());
            }
            
        } catch (Exception e) {
            log.error("ES同步失败: workId={}", message.getWorkId(), e);
            // 抛出异常触发 MQ 重试
            throw e;
        }
    }
}
