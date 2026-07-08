package com.tiv.image.hub.manager.sharding;

import com.tiv.image.hub.constant.Constants;
import org.apache.shardingsphere.sharding.api.sharding.standard.PreciseShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.RangeShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.StandardShardingAlgorithm;

import java.util.Collection;
import java.util.Objects;
import java.util.Properties;

/**
 * 图片分片算法
 */
public class ImageShardingAlgorithm implements StandardShardingAlgorithm<Long> {

    @Override
    public String doSharding(Collection<String> availableTargetNames, PreciseShardingValue<Long> shardingValue) {
        String logicTableName = shardingValue.getLogicTableName();
        Long spaceId = shardingValue.getValue();
        if (spaceId == null || Objects.equals(spaceId, Constants.PUBLIC_SPACE_ID)) {
            return logicTableName;
        }
        String realTableName = "image_" + spaceId;
        // 如果该空间存在独立分表,路由到分表,否则回退到主表
        if (availableTargetNames.contains(realTableName)) {
            return realTableName;
        }
        return logicTableName;
    }

    @Override
    public Collection<String> doSharding(Collection<String> availableTargetNames, RangeShardingValue<Long> shardingValue) {
        // 范围查询无法确定单个空间表,返回全部可用表避免路由结果为空
        return availableTargetNames;
    }

    @Override
    public Properties getProps() {
        return new Properties();
    }

    @Override
    public void init(Properties props) {

    }

}
