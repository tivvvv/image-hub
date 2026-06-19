<template>
  <div id="imageSearchForm">
    <!-- 搜索表单 -->
    <a-form layout="inline" :model="searchParams" @finish="doSearch">
      <a-form-item name="keyword" label="关键词">
        <a-input
          v-model:value="searchParams.keyword"
          placeholder="从名称和简介中搜索"
          allow-clear
        />
      </a-form-item>

      <a-form-item name="imageCategory" label="分类">
        <a-auto-complete
          v-model:value="searchParams.imageCategory"
          style="min-width: 180px"
          placeholder="请输入分类"
          :options="categoryOptions"
          allow-clear
        />
      </a-form-item>

      <a-form-item name="imageTagList" label="标签">
        <a-select
          v-model:value="searchParams.imageTagList"
          style="min-width: 180px"
          mode="tags"
          placeholder="请输入标签"
          :options="tagOptions"
          allow-clear
        />
      </a-form-item>

      <a-form-item label="日期" name="dateRange">
        <a-range-imageker
          style="width: 400px"
          show-time
          v-model:value="dateRange"
          :placeholder="['编辑开始时间', '编辑结束时间']"
          format="YYYY/MM/DD HH:mm:ss"
          :presets="rangePresets"
          @change="onRangeChange"
        />
      </a-form-item>

      <a-form-item label="名称" name="imageName">
        <a-input v-model:value="searchParams.imageName" placeholder="请输入名称" allow-clear />
      </a-form-item>

      <a-form-item label="简介" name="imageIntro">
        <a-input v-model:value="searchParams.imageIntro" placeholder="请输入简介" allow-clear />
      </a-form-item>

      <a-form-item label="宽度" name="imageWidth">
        <a-input-number v-model:value="searchParams.imageWidth" />
      </a-form-item>

      <a-form-item label="高度" name="imageHeight">
        <a-input-number v-model:value="searchParams.imageHeight" />
      </a-form-item>

      <a-form-item label="格式" name="imageFormat">
        <a-input v-model:value="searchParams.imageFormat" placeholder="请输入格式" allow-clear />
      </a-form-item>

      <a-form-item label="颜色" name="imageColor">
        <div class="color-picker-wrapper">
          <ColorPicker
            v-model:pureColor="pureColor"
            v-model:gradientColor="gradientColor"
            format="hex"
            shape="circle"
            @pureColorChange="onColorChange"
          />
          <span v-if="searchParams.imageColor" class="color-value">{{
            searchParams.imageColor
          }}</span>
          <a-button v-if="searchParams.imageColor" type="link" size="small" @click="clearColor"
            >清除
          </a-button>
        </div>
      </a-form-item>

      <a-form-item>
        <a-space>
          <a-button type="primary" html-type="submit" style="width: 96px">搜索</a-button>
          <a-button html-type="reset" @click="doClear">重置</a-button>
        </a-space>
      </a-form-item>
    </a-form>
    <div style="margin-bottom: 16px" />
  </div>
</template>
<script lang="ts" setup>
import { onMounted, reactive, ref } from 'vue'
import dayjs from 'dayjs'
import { message } from 'ant-design-vue'
import { listImageTagCategoryUsingGet } from '@/api/imageController.ts'
import { ColorPicker } from 'vue3-colorpicker'
import 'vue3-colorpicker/style.css'

const dateRange = ref<[]>([])
const tagOptions = ref<{ value: string; label: string }[]>([])
const categoryOptions = ref<{ value: string; label: string }[]>([])
const pureColor = ref<string>('')
const gradientColor = ref<string>('')

onMounted(() => {
  getTagAndCategoryOptions()
})

const getTagAndCategoryOptions = async () => {
  const res = await listImageTagCategoryUsingGet()
  if (res.data.code === 0 && res.data.data) {
    tagOptions.value = (res.data.data.tagList ?? []).map((data: string) => {
      return {
        value: data,
        label: data,
      }
    })
    categoryOptions.value = (res.data.data.categoryList ?? []).map((data: string) => {
      return {
        value: data,
        label: data,
      }
    })
  } else {
    message.error('获取默认标签和分类选项失败,' + res.data.message)
  }
}

// 时间范围预设
const rangePresets = ref([
  { label: '过去 7 天', value: [dayjs().add(-7, 'd'), dayjs()] },
  { label: '过去 14 天', value: [dayjs().add(-14, 'd'), dayjs()] },
  { label: '过去 30 天', value: [dayjs().add(-30, 'd'), dayjs()] },
  { label: '过去 90 天', value: [dayjs().add(-90, 'd'), dayjs()] },
])

const onRangeChange = (dates: any[], dateStrings: string[]) => {
  if (dates?.length >= 2) {
    searchParams.updateTimeStart = dates[0].toDate()
    searchParams.updateTimeEnd = dates[1].toDate()
  } else {
    searchParams.updateTimeStart = undefined
    searchParams.updateTimeEnd = undefined
  }
}

interface Props {
  onSearch?: (searchParams: API.ImageQueryRequest) => void
}

const props = defineProps<Props>()

// 搜索条件
const searchParams = reactive<API.ImageQueryRequest>({})

// 搜索数据
const doSearch = () => {
  props.onSearch?.(searchParams)
}

const doClear = () => {
  // 取消所有对象的值
  const keys = Object.keys(searchParams) as (keyof API.ImageQueryRequest)[]
  keys.forEach((key) => {
    searchParams[key] = undefined
  })
  dateRange.value = []
  pureColor.value = ''
  gradientColor.value = ''
  // 清空后重新搜索
  props.onSearch?.(searchParams)
}

// 颜色变化处理
const onColorChange = (color: string) => {
  if (color) {
    searchParams.imageColor = color
  } else {
    searchParams.imageColor = undefined
  }
}

// 清除颜色
const clearColor = () => {
  pureColor.value = ''
  gradientColor.value = ''
  searchParams.imageColor = undefined
}
</script>

<style scoped>
#imageSearchForm .ant-form-item {
  margin-right: 8px;
  margin-bottom: 8px;
}

.color-picker-wrapper {
  display: flex;
  align-items: center;
  gap: 8px;
}

.color-value {
  font-size: 12px;
  color: #666;
  font-family: monospace;
}
</style>
