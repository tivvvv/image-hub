<template>
  <div id="spaceDetailView">
    <!-- 空间信息 -->
    <a-flex justify="space-between">
      <h2>{{ spaceVO.spaceName }} (私有空间)</h2>
      <a-space size="middle">
        <a-button type="primary" :href="`/image/add?spaceId=${id}`" target="_blank"
          >+ 上传图片
        </a-button>
        <a-button :icon="h(EditOutlined)" @click="doBatchEdit">批量编辑</a-button>

        <a-tooltip
          :title="`占用空间 ${formatSize(spaceVO.currentSize)} / ${formatSize(spaceVO.maxSize)}`"
        >
          <a-progress
            type="circle"
            :size="42"
            :percent="
              spaceVO.maxSize
                ? Number((((spaceVO.currentSize || 0) * 100) / spaceVO.maxSize).toFixed(1))
                : 0
            "
          />
        </a-tooltip>
      </a-space>
    </a-flex>
    <div style="margin-bottom: 16px" />

    <!-- 图片搜索表单 -->
    <ImageSearchForm :onSearch="onSearch" />
    <div style="margin-bottom: 16px" />

    <!-- 图片列表 -->
    <ImageList :dataList="dataList" :loading="loading" @refresh="fetchData" :showOperation="true" />
    <!-- 分页 -->
    <a-pagination
      style="text-align: right"
      v-model:current="searchParams.current"
      v-model:pageSize="searchParams.pageSize"
      :total="total"
      @change="onPageChange"
    />
    <BatchEditImageModal
      ref="batchEditImageModalRef"
      :spaceId="id"
      :imageList="dataList"
      :onSuccess="onBatchEditImageSuccess"
    />
  </div>
</template>

<script setup lang="ts">
import { h, onMounted, ref } from 'vue'
import { getSpaceVoByIdUsingGet } from '@/api/spaceController.ts'
import { message } from 'ant-design-vue'
import { useRouter } from 'vue-router'
import { useLoginUserStore } from '@/stores/useLoginUserStore.ts'
import { listImageVoByPageUsingPost } from '@/api/imageController.ts'
import { formatSize } from '@/utils/imageUtil.ts'
import ImageList from '@/components/ImageList.vue'
import ImageSearchForm from '@/components/ImageSearchForm.vue'
import BatchEditImageModal from '@/components/BatchEditImageModal.vue'
import { EditOutlined } from '@ant-design/icons-vue'

const spaceVO = ref<API.SpaceVO>({})
const router = useRouter()
const loginUserStore = useLoginUserStore()
const props = defineProps<Props>()

interface Props {
  id: string
}

// 获取空间详情
const fetchSpaceDetail = async () => {
  const res = await getSpaceVoByIdUsingGet({
    id: props.id,
  })
  if (res.data.code === 0 && res.data.data) {
    spaceVO.value = res.data.data
  } else {
    message.error('获取空间详情失败,' + res.data.message)
  }
}

const dataList = ref<API.ImageVO[]>([])
const total = ref(0)
const loading = ref(true)

// 搜索条件
const searchParams = ref<API.ImageQueryRequest>({
  current: 1,
  pageSize: 12,
  sortField: 'create_time',
  sortOrder: 'desc',
})

// 搜索
const onSearch = (newSearchParams: API.ImageQueryRequest) => {
  searchParams.value = {
    ...searchParams.value,
    ...newSearchParams,
    current: 1,
  }
  console.log('searchParams', searchParams.value)
  fetchData()
}

// 获取数据
const fetchData = async () => {
  loading.value = true
  // 转换搜索参数
  const params = {
    spaceId: props.id,
    ...searchParams.value,
  }

  const res = await listImageVoByPageUsingPost(params)
  if (res.data.code === 0 && res.data.data) {
    dataList.value = res.data.data.records ?? []
    // 确保 total 是数字类型
    total.value = Number(res.data.data.total) ?? 0
  } else {
    message.error(res.data.message)
  }
  loading.value = false
}

// 页面加载时请求一次
onMounted(() => {
  ;(fetchSpaceDetail(), fetchData())
})

// 分页参数
const onPageChange = (page: number, pageSize: number) => {
  searchParams.value.current = page
  searchParams.value.pageSize = pageSize
  fetchData()
}

// 批量编辑图片
const batchEditImageModalRef = ref()

const onBatchEditImageSuccess = () => {
  fetchData()
}

// 打开批量编辑图片弹窗
const doBatchEdit = () => {
  if (batchEditImageModalRef.value) {
    batchEditImageModalRef.value.openModal()
  }
}
</script>

<style scoped>
#spaceDetailView {
}
</style>
