<template>
  <div class="batch-edit-image-modal">
    <a-modal v-model:open="visible" title="批量编辑图片" :footer="false" @cancel="closeModal">
      <a-typography-paragraph type="secondary">* 只对当前页面的图片生效</a-typography-paragraph>
      <a-form name="formData" layout="vertical" :model="formData" @finish="handleSubmit">
        <a-form-item name="imageCategory" label="分类">
          <a-auto-complete
            v-model:value="formData.imageCategory"
            placeholder="请输入分类"
            :options="categoryOptions"
            allow-clear
          />
        </a-form-item>
        <a-form-item name="imageTagList" label="标签">
          <a-select
            v-model:value="formData.imageTagList"
            mode="tags"
            placeholder="请输入标签"
            :options="tagOptions"
            allow-clear
          />
        </a-form-item>
        <a-form-item name="nameTemplate" label="命名模版">
          <a-input
            v-model:value="formData.nameTemplate"
            placeholder="请输入命名模版,输入 {序号} 可动态生成"
            allow-clear
          />
        </a-form-item>
        <a-form-item>
          <a-button type="primary" html-type="submit" style="width: 100%">提交</a-button>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>
<script lang="ts" setup>
import { onMounted, reactive, ref } from 'vue'
import { batchUpdateImageUsingPost, listImageTagCategoryUsingGet } from '@/api/imageController.ts'
import { message } from 'ant-design-vue'

interface Props {
  imageList: API.ImageVO[]
  spaceId: String
  onSuccess: () => void
}

const props = withDefaults(defineProps<Props>(), {})

// 是否可见
const visible = ref(false)

// 打开弹窗
const openModal = () => {
  visible.value = true
}

// 关闭弹窗
const closeModal = () => {
  visible.value = false
}

// 暴露函数给父组件
defineExpose({
  openModal,
})

const formData = reactive<API.ImageBatchUpdateRequest>({
  imageCategory: '',
  imageTagList: [],
  nameTemplate: '',
})

/**
 * 提交表单
 * @param values
 */
const handleSubmit = async (values: any) => {
  if (!props.imageList) {
    return
  }
  const res = await batchUpdateImageUsingPost({
    imageIds: props.imageList.map((image) => image.id),
    spaceId: props.spaceId,
    ...values,
  })
  // 操作成功
  if (res.data.code === 0 && res.data.data) {
    message.success('操作成功')
    closeModal()
    props.onSuccess?.()
  } else {
    message.error('操作失败,' + res.data.message)
  }
}

interface Option {
  value: string
  label: string
}

const categoryOptions = ref<Option[]>([])
const tagOptions = ref<Option[]>([])

/**
 * 获取标签和分类选项
 * @param values
 */
const getTagCategoryOptions = async () => {
  const res = await listImageTagCategoryUsingGet()
  if (res.data.code === 0 && res.data.data) {
    tagOptions.value = (res.data.data.tagList ?? []).map((data) => {
      return {
        value: data,
        label: data,
      }
    })
    categoryOptions.value = (res.data.data.categoryList ?? []).map((data) => {
      return {
        value: data,
        label: data,
      }
    })
  } else {
    message.error('获取标签分类列表失败,' + res.data.message)
  }
}

onMounted(() => {
  getTagCategoryOptions()
})
</script>
