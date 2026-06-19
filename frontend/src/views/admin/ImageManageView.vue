<template>
  <div id="imageManageView">
    <a-flex justify="space-between">
      <h2>图片管理</h2>
      <a-space>
        <a-button type="primary" href="/image/add" target="_blank">+ 上传图片</a-button>
        <a-button type="primary" href="/admin/image/fetch" target="_blank" ghost
          >+ 抓取图片
        </a-button>
      </a-space>
    </a-flex>
    <div style="margin-bottom: 16px" />

    <!-- 搜索表单 -->
    <a-form layout="inline" :model="searchParams" @finish="doSearch">
      <a-form-item name="keyword" label="关键词">
        <a-input
          v-model:value="searchParams.keyword"
          placeholder="从名称和简介中搜索"
          allow-clear
        />
      </a-form-item>

      <a-form-item name="imageCategory" label="类型">
        <a-input v-model:value="searchParams.imageCategory" placeholder="请输入类型" allow-clear />
      </a-form-item>

      <a-form-item name="imageTagList" label="标签">
        <a-select
          v-model:value="searchParams.imageTagList"
          mode="tags"
          placeholder="请输入标签"
          style="min-width: 180px"
          allow-clear
        />
      </a-form-item>

      <a-form-item name="reviewStatus" label="审核状态">
        <a-select
          v-model:value="searchParams.reviewStatus"
          style="min-width: 180px"
          placeholder="请选择审核状态"
          :options="PIC_REVIEW_STATUS_OPTIONS"
          allow-clear
        />
      </a-form-item>

      <a-form-item>
        <a-button type="primary" html-type="submit">搜索</a-button>
      </a-form-item>
    </a-form>
    <div style="margin-bottom: 16px" />
    <!-- 表格 -->
    <a-table
      :columns="columns"
      :data-source="dataList"
      :pagination="pagination"
      @change="doTableChange"
    >
      <template #bodyCell="{ column, record }">
        <!-- 图片 -->
        <template v-if="column.dataIndex === 'imageUrl'">
          <a-image :src="record.imageUrl" :width="120" />
        </template>
        <!-- 标签 -->
        <template v-if="column.dataIndex === 'imageTags'">
          <a-space wrap>
            <a-tag v-for="tag in JSON.parse(record.imageTags || '[]')" :key="tag">
              {{ tag }}
            </a-tag>
          </a-space>
        </template>
        <!-- 图片信息 -->
        <template v-if="column.dataIndex === 'imageInfo'">
          <div>格式: {{ record.imageFormat }}</div>
          <div>宽度: {{ record.imageWidth }}</div>
          <div>高度: {{ record.imageHeight }}</div>
          <div>宽高比: {{ record.imageScale }}</div>
          <div>大小: {{ (record.imageSize / 1024).toFixed(2) }}KB</div>
        </template>
        <template v-if="column.dataIndex === 'spaceId'">
          {{ record.spaceId ? record.spaceId : '公共空间' }}
        </template>
        <!-- 审核信息 -->
        <template v-if="column.dataIndex === 'reviewInfo'">
          <div style="display: flex; flex-direction: column; min-width: 120px">
            <div>审核状态: {{ PIC_REVIEW_STATUS_DESC[record.reviewStatus as 0 | 1 | 2] }}</div>
            <div>审核信息: {{ record.reviewMessage }}</div>
            <div>审核人: {{ record.reviewerId }}</div>
            <div v-if="record.reviewTime">
              审核时间: {{ dayjs(record.reviewTime).format('YYYY-MM-DD HH:mm:ss') }}
            </div>
          </div>
        </template>
        <template v-if="column.dataIndex === 'createTime'">
          {{ dayjs(record.createTime).format('YYYY-MM-DD HH:mm:ss') }}
        </template>
        <template v-if="column.dataIndex === 'updateTime'">
          {{ dayjs(record.updateTime).format('YYYY-MM-DD HH:mm:ss') }}
        </template>

        <template v-else-if="column.key === 'action'">
          <a-space wrap>
            <a-button type="link" :href="`/image/add?id=${record.id}`" target="_blank">
              编辑
            </a-button>
            <a-button
              v-if="record.reviewStatus !== PIC_REVIEW_STATUS_CODE.PASS"
              type="link"
              @click="handleReview(record, PIC_REVIEW_STATUS_CODE.PASS)"
            >
              通过
            </a-button>
            <a-button
              v-if="record.reviewStatus !== PIC_REVIEW_STATUS_CODE.REJECT"
              type="link"
              danger
              @click="handleReview(record, PIC_REVIEW_STATUS_CODE.REJECT)"
            >
              驳回
            </a-button>
            <a-button danger @click="doDelete(record.id)">删除</a-button>
          </a-space>
        </template>
      </template>
    </a-table>
  </div>
</template>
<script lang="ts" setup>
import { computed, onMounted, reactive, ref } from 'vue'
import {
  deleteImageUsingDelete,
  listImageByPageUsingPost,
  reviewImageUsingPost,
} from '@/api/imageController.ts'
import { message } from 'ant-design-vue'
import dayjs from 'dayjs'
import {
  PIC_REVIEW_STATUS_CODE,
  PIC_REVIEW_STATUS_DESC,
  PIC_REVIEW_STATUS_OPTIONS,
} from '@/constants/imageConstant.ts'

const columns = [
  {
    title: 'id',
    dataIndex: 'id',
    width: 80,
  },
  {
    title: '图片',
    dataIndex: 'imageUrl',
  },
  {
    title: '名称',
    dataIndex: 'imageName',
  },
  {
    title: '简介',
    dataIndex: 'imageIntro',
  },
  {
    title: '类型',
    dataIndex: 'imageCategory',
  },
  {
    title: '标签',
    dataIndex: 'imageTags',
  },
  {
    title: '图片信息',
    dataIndex: 'imageInfo',
  },
  {
    title: '用户id',
    dataIndex: 'userId',
    width: 80,
  },
  {
    title: '空间id',
    dataIndex: 'spaceId',
    width: 80,
  },
  {
    title: '审核信息',
    dataIndex: 'reviewInfo',
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
  },
  {
    title: '更新时间',
    dataIndex: 'updateTime',
  },
  {
    title: '操作',
    key: 'action',
  },
]

const dataList = ref<API.Image[]>([])
const total = ref(0)

// 搜索条件
const searchParams = reactive<API.ImageQueryRequest>({
  current: 1,
  pageSize: 10,
  sortField: 'create_time',
  sortOrder: 'desc',
})

// 获取数据
const fetchData = async () => {
  const res = await listImageByPageUsingPost({
    ...searchParams,
    spaceId: undefined,
  })
  if (res.data.code === 0 && res.data.data) {
    dataList.value = res.data.data.records ?? []
    total.value = res.data.data.total ?? 0
  } else {
    message.error(res.data.message)
  }
}

// 页面加载时请求一次
onMounted(() => {
  fetchData()
})

// 分页参数
const pagination = computed(() => {
  return {
    current: searchParams.current,
    pageSize: searchParams.pageSize,
    total: total.value,
    showSizeChanger: true,
    showTotal: (total: number) => `共 ${total} 条`,
  }
})

// 表格变化后,重新请求获取数据
const doTableChange = (page: any) => {
  searchParams.current = page.current
  searchParams.pageSize = page.pageSize
  fetchData()
}

// 搜索数据
const doSearch = () => {
  // 重置页码
  searchParams.current = 1
  fetchData()
}

// 删除数据
const doDelete = async (id: string) => {
  if (!id) {
    return
  }
  const res = await deleteImageUsingDelete({ id })
  if (res.data.code === 0) {
    message.success('删除成功')
    // 刷新数据
    await fetchData()
  } else {
    message.error('删除失败')
  }
}

// 审核图片
const handleReview = async (record: API.Image, reviewStatus: number) => {
  const reviewMessage = reviewStatus === PIC_REVIEW_STATUS_CODE.PASS ? '审核通过' : '审核驳回'
  const res = await reviewImageUsingPost({
    id: record.id,
    reviewStatus,
    reviewMessage,
  })
  if (res.data.code === 0) {
    message.success('审核操作成功')
    // 重新获取列表数据
    await fetchData()
  } else {
    message.error('审核操作失败, ' + res.data.message)
  }
}
</script>
