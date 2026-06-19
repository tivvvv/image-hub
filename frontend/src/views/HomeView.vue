<template>
  <div id="homeView">
    <!-- 搜索框 -->
    <div class="searchBar">
      <a-input-search
        v-model:value="searchParams.keyword"
        placeholder="从海量图片中搜索"
        enter-button="搜索"
        size="large"
        @search="doSearch"
      />
    </div>

    <!-- 分类筛选 -->
    <a-tabs v-model:active-key="selectedCategory" @change="doSearch">
      <a-tab-pane key="all" tab="全部" />
      <a-tab-pane v-for="category in categoryList" :tab="category" :key="category" />
    </a-tabs>

    <!-- 标签筛选 -->
    <div class="tagBar">
      <span style="margin-right: 8px">标签</span>
      <a-space :size="[0, 8]" wrap>
        <a-checkable-tag
          v-for="(tag, index) in tagList"
          :key="tag"
          v-model:checked="selectedTags[index]"
          @change="doSearch"
        >
          {{ tag }}
        </a-checkable-tag>
      </a-space>
    </div>
    <!-- 图片列表 -->
    <ImageList :dataList="dataList" :loading="loading" />
    <!-- 分页 -->
    <a-pagination
      style="text-align: right"
      v-model:current="searchParams.current"
      v-model:pageSize="searchParams.pageSize"
      :total="total"
      @change="onPageChange"
    />
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { listImageTagCategoryUsingGet, listImageVoByPageUsingPost } from '@/api/imageController.ts'
import { message } from 'ant-design-vue'
import { useRouter } from 'vue-router'
import ImageList from '@/components/ImageList.vue'

const router = useRouter()

const dataList = ref<API.ImageVO[]>([])
const total = ref(0)
const loading = ref(true)

// 搜索条件
const searchParams = reactive<API.ImageQueryRequest>({
  current: 1,
  pageSize: 12,
  sortField: 'create_time',
  sortOrder: 'desc',
})

// 获取数据
const fetchData = async () => {
  loading.value = true
  // 转换搜索参数
  const params = {
    ...searchParams,
    imageTagList: [] as string[],
  }
  if (selectedCategory.value !== 'all') {
    params.imageCategory = selectedCategory.value
  }
  selectedTags.value.forEach((selectTag, index) => {
    if (selectTag) {
      params.imageTagList.push(tagList.value[index])
    }
  })
  const res = await listImageVoByPageUsingPost(params)
  if (res.data.code === 0 && res.data.data) {
    dataList.value = res.data.data.records ?? []
    total.value = Number(res.data.data.total) ?? 0
  } else {
    message.error(res.data.message)
  }
  loading.value = false
}

// 页面加载时请求一次
onMounted(() => {
  fetchData()
  getTagAndCategoryOptions()
})

// 分页参数
const onPageChange = (page: number, pageSize: number) => {
  searchParams.current = page
  searchParams.pageSize = pageSize
  fetchData()
}

// 搜索
const doSearch = () => {
  // 重置搜索条件
  searchParams.current = 1
  fetchData()
}

const tagList = ref<string[]>([])
const selectedTags = ref<boolean[]>([])

const categoryList = ref<string[]>([])
const selectedCategory = ref<string>('all')

const getTagAndCategoryOptions = async () => {
  const res = await listImageTagCategoryUsingGet()
  if (res.data.code === 0 && res.data.data) {
    tagList.value = res.data.data.tagList ?? []
    categoryList.value = res.data.data.categoryList ?? []
  } else {
    message.error('获取标签和分类选项失败,' + res.data.message)
  }
}
</script>

<style scoped>
#homeView {
  margin-bottom: 16px;
}

#homeView .searchBar {
  max-width: 480px;
  margin: 0 auto 16px;
}

#homeView .tagBar {
  margin-bottom: 20px;
}
</style>
