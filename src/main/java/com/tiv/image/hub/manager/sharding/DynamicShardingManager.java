package com.tiv.image.hub.manager.sharding;

import com.tiv.image.hub.model.entity.Space;
import com.tiv.image.hub.model.enums.SpaceLevelEnum;
import com.tiv.image.hub.model.enums.SpaceTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.driver.jdbc.core.connection.ShardingSphereConnection;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

/**
 * 动态分表管理器
 *
 * 用于给旗舰版团队空间创建独立的图片表,并在运行时刷新 ShardingSphere 的真实数据节点
 * 普通空间和未创建分表的空间会继续回退到 image 主表
 */
@Slf4j
@Component
public class DynamicShardingManager {

    @Resource
    private DataSource dataSource;

    @Value("${spring.shardingsphere.database.name:logic_db}")
    private String logicDatabase;

    @Value("${spring.shardingsphere.datasource.image_hub.url}")
    private String physicalJdbcUrl;

    @Value("${spring.shardingsphere.datasource.image_hub.username}")
    private String physicalUsername;

    @Value("${spring.shardingsphere.datasource.image_hub.password}")
    private String physicalPassword;

    private static final String DATA_SOURCE_NAME = "image_hub";

    private static final String PHYSICAL_DATABASE = "image_hub";

    private static final String LOGIC_TABLE = "image";

    private static final String SHARDING_TABLE_PREFIX = LOGIC_TABLE + "_";

    private final Object refreshLock = new Object();

    @PostConstruct
    private void init() {
        log.info("初始化 DynamicShardingManager.");
        if (!updateActualDataNodes()) {
            throw new IllegalStateException("初始化动态分表规则失败");
        }
    }

    /**
     * 为需要独立存储的空间创建图片分表
     *
     * 目前只有旗舰版团队空间会创建 image_{spaceId} 分表
     *
     * @return 无需分表或创建成功时返回 true,创建失败时返回 false
     */
    public boolean createSpacePictureTable(Space space) {
        if (!needSeparateTable(space)) {
            return true;
        }

        String tableName = getActualTableName(space.getId());
        boolean tableAlreadyExists;
        try {
            tableAlreadyExists = tableExists(tableName);
        } catch (Exception e) {
            log.error("创建图片空间分表前检查表状态失败,空间 id = {},表名 = {}", space.getId(), tableName, e);
            return false;
        }
        registerRollbackCleanup(tableName, tableAlreadyExists);
        String createTableSql = String.format("CREATE TABLE IF NOT EXISTS `%s` LIKE `%s`", tableName, LOGIC_TABLE);
        try (Connection connection = getPhysicalConnection();
             Statement statement = connection.createStatement()) {
            // 建表操作保持幂等,避免接口重试或重复刷新时因为表已存在而失败
            statement.executeUpdate(createTableSql);
            // 物理表创建完成后,把新表加入 ShardingSphere 的运行时路由规则
            boolean updated = updateActualDataNodes();
            if (!updated && !tableAlreadyExists) {
                dropActualTable(tableName);
            }
            return updated;
        } catch (Exception e) {
            log.error("创建图片空间分表失败,空间 id = {},表名 = {}", space.getId(), tableName, e);
            if (!tableAlreadyExists) {
                dropActualTable(tableName);
            }
            return false;
        }
    }

    /**
     * 刷新 ShardingSphere 运行时真实数据节点
     */
    private boolean updateActualDataNodes() {
        synchronized (refreshLock) {
            Set<String> dataNodes;
            try {
                dataNodes = getActualDataNodes();
            } catch (Exception e) {
                log.error("动态分表更新失败,获取实际数据节点异常", e);
                return false;
            }
            ContextManager contextManager = getContextManager();
            if (!contextManager.getMetaDataContexts().getMetaData().getDatabases().containsKey(logicDatabase)) {
                log.error("动态分表更新失败,未找到逻辑库配置: {},可用逻辑库 = {}",
                        logicDatabase,
                        contextManager.getMetaDataContexts().getMetaData().getDatabases().keySet());
                return false;
            }
            ShardingSphereRuleMetaData ruleMetaData = contextManager.getMetaDataContexts()
                    .getMetaData()
                    .getDatabases()
                    .get(logicDatabase)
                    .getRuleMetaData();
            Optional<ShardingRule> shardingRule = ruleMetaData.findSingleRule(ShardingRule.class);
            if (shardingRule.isEmpty()) {
                log.error("动态分表更新失败, 未找到 ShardingSphere 的分片规则配置");
                return false;
            }

            ShardingRuleConfiguration ruleConfig = (ShardingRuleConfiguration) shardingRule.get().getConfiguration();
            List<ShardingTableRuleConfiguration> updatedRules = new ArrayList<>();
            boolean imageRuleFound = false;
            for (ShardingTableRuleConfiguration oldTableRule : ruleConfig.getTables()) {
                if (LOGIC_TABLE.equals(oldTableRule.getLogicTable())) {
                    imageRuleFound = true;
                    ShardingTableRuleConfiguration newTableRuleConfig =
                            new ShardingTableRuleConfiguration(LOGIC_TABLE, String.join(",", dataNodes));
                    // 动态替换真实数据节点时,保留原来的分库分表策略和主键生成策略
                    newTableRuleConfig.setDatabaseShardingStrategy(oldTableRule.getDatabaseShardingStrategy());
                    newTableRuleConfig.setTableShardingStrategy(oldTableRule.getTableShardingStrategy());
                    newTableRuleConfig.setKeyGenerateStrategy(oldTableRule.getKeyGenerateStrategy());
                    newTableRuleConfig.setAuditStrategy(oldTableRule.getAuditStrategy());
                    updatedRules.add(newTableRuleConfig);
                    continue;
                }
                updatedRules.add(oldTableRule);
            }
            if (!imageRuleFound) {
                log.error("动态分表更新失败,未找到逻辑表配置: {}", LOGIC_TABLE);
                return false;
            }
            ruleConfig.setTables(updatedRules);
            contextManager.alterRuleConfiguration(logicDatabase, Collections.singleton(ruleConfig));
            contextManager.reloadDatabase(logicDatabase);
            log.info("动态分表更新成功,实际数据节点 = {}", dataNodes);
            return true;
        }
    }

    /**
     * 获取所有真实存在的图片数据节点
     */
    private Set<String> getActualDataNodes() throws SQLException {
        Set<String> tableNames = new TreeSet<>();
        tableNames.add(getActualDataNode(LOGIC_TABLE));

        // 只要物理分表存在就加入路由,避免空间等级变化后历史分表数据不可达
        for (String tableName : listShardingTableNames()) {
            tableNames.add(getActualDataNode(tableName));
        }
        return tableNames;
    }

    /**
     * 判断空间是否需要独立图片表
     */
    private boolean needSeparateTable(Space space) {
        return space != null
                && space.getId() != null
                && Objects.equals(space.getSpaceType(), SpaceTypeEnum.TEAM.getValue())
                && Objects.equals(space.getSpaceLevel(), SpaceLevelEnum.ULTRA.getValue());
    }

    /**
     * 获取分表物理表名
     */
    private String getActualTableName(Long spaceId) {
        return SHARDING_TABLE_PREFIX + spaceId;
    }

    /**
     * 获取 ShardingSphere 数据节点名
     */
    private String getActualDataNode(String tableName) {
        return DATA_SOURCE_NAME + "." + tableName;
    }

    /**
     * 检查物理表是否存在
     */
    private boolean tableExists(String tableName) throws SQLException {
        String sql = "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = ? AND table_name = ?";
        try (Connection connection = getPhysicalConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, getPhysicalDatabase(connection));
            statement.setString(2, tableName);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() && resultSet.getInt(1) > 0;
            }
        }
    }

    /**
     * 获取物理库中已存在的图片分表名
     */
    private Set<String> listShardingTableNames() throws SQLException {
        Set<String> tableNames = new TreeSet<>();
        String sql = "SELECT table_name FROM information_schema.tables WHERE table_schema = ? AND table_name LIKE ?";
        try (Connection connection = getPhysicalConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, getPhysicalDatabase(connection));
            statement.setString(2, SHARDING_TABLE_PREFIX + "%");
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    String tableName = resultSet.getString(1);
                    if (isValidShardingTableName(tableName)) {
                        tableNames.add(tableName);
                    }
                }
            }
        }
        return tableNames;
    }

    /**
     * 判断是否为合法图片分表名
     */
    private boolean isValidShardingTableName(String tableName) {
        if (tableName == null || !tableName.startsWith(SHARDING_TABLE_PREFIX)) {
            return false;
        }
        String suffix = tableName.substring(SHARDING_TABLE_PREFIX.length());
        return suffix.chars().allMatch(Character::isDigit);
    }

    /**
     * 删除本次新建但未成功加入路由规则的物理表
     */
    private void dropActualTable(String tableName) {
        String sql = String.format("DROP TABLE IF EXISTS `%s`", tableName);
        try (Connection connection = getPhysicalConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        } catch (Exception e) {
            log.error("清理分表失败,表名 = {}", tableName, e);
        }
    }

    /**
     * 注册事务回滚清理
     */
    private void registerRollbackCleanup(String tableName, boolean tableAlreadyExists) {
        if (tableAlreadyExists || !TransactionSynchronizationManager.isSynchronizationActive()) {
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status == TransactionSynchronization.STATUS_COMMITTED) {
                    return;
                }
                dropActualTable(tableName);
                updateActualDataNodes();
            }
        });
    }

    /**
     * 获取物理库名
     */
    private String getPhysicalDatabase(Connection connection) throws SQLException {
        String catalog = connection.getCatalog();
        if (catalog == null || catalog.isBlank()) {
            return PHYSICAL_DATABASE;
        }
        return catalog;
    }

    /**
     * 获取 ShardingSphere 上下文管理器
     */
    private ContextManager getContextManager() {
        try (ShardingSphereConnection connection = dataSource.getConnection().unwrap(ShardingSphereConnection.class)) {
            return connection.getContextManager();
        } catch (Exception e) {
            throw new RuntimeException("获取 ShardingSphere ContextManager 失败", e);
        }
    }

    /**
     * 获取物理库连接
     */
    private Connection getPhysicalConnection() throws SQLException {
        return DriverManager.getConnection(physicalJdbcUrl, physicalUsername, physicalPassword);
    }
}
