<template>
  <a-modal
    class="image-expand"
    v-model:open="visible"
    title="AI 扩图"
    :footer="false"
    @cancel="closeModal"
  >
    <a-row gutter="16">
      <a-col span="12">
        <h4>原始图片</h4>
        <img :src="imageVO?.imageUrl" :alt="imageVO?.imageName" style="max-width: 100%" />
      </a-col>
      <a-col span="12">
        <h4>扩图结果</h4>
        <img
          v-if="resultImageUrl"
          :src="resultImageUrl"
          :alt="imageVO?.imageName"
          style="max-width: 100%"
        />
      </a-col>
    </a-row>
    <div style="margin-bottom: 16px" />
    <a-flex justify="center" gap="16">
      <a-button
        :icon="h(EditOutlined)"
        type="primary"
        :loading="!!taskId"
        ghost
        @click="expandImage"
        >开始扩图</a-button
      >
      <a-button
        v-if="resultImageUrl"
        type="primary"
        :icon="h(FullscreenOutlined)"
        :loading="loading"
        @click="handleUpload"
      >
        应用结果
      </a-button>
    </a-flex>
  </a-modal>
</template>

<script lang="ts" setup>
import { h, ref } from 'vue'
import {
  expandImageUsingPost,
  queryImageExpandTaskStatusUsingGet,
  uploadImageByUrlUsingPost,
} from '@/api/imageController.ts'
import { EditOutlined, FullscreenOutlined } from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'

interface Props {
  imageVO?: API.ImageVO
  spaceId?: string
  onSuccess?: (newImage: API.ImageVO) => void
}

const taskId = ref<string>()

const props = defineProps<Props>()

const resultImageUrl = ref<string>('')

const loading = ref(false)

const expandImage = async () => {
  if (!props.imageVO?.id) {
    message.error('图片信息不存在')
    return
  }
  try {
    const res = await expandImageUsingPost({
      id: String(props.imageVO.id),
      // 设置扩图参数
      parameters: {
        xScale: 2,
        yScale: 2,
      },
    })
    if (res.data.code === 0 && res.data.data) {
      message.success('扩图ing, 请稍等')
      taskId.value = res.data.data?.output?.taskId
      // 开启轮询
      startPolling()
    } else {
      message.error('扩图失败, ' + res.data.message)
    }
  } catch (error) {
    let errorMessage = '扩图失败'
    if (error instanceof Error) {
      errorMessage += ', ' + error.message
    }
    message.error(errorMessage)
  }
}

// 轮询定时器
let pollingTimer: ReturnType<typeof setInterval> | null = null

/**
 * 开始轮询
 */
const startPolling = () => {
  if (!taskId.value) {
    return
  }

  pollingTimer = setInterval(async () => {
    try {
      const res = await queryImageExpandTaskStatusUsingGet({
        taskId: String(taskId.value),
      })
      if (res.data.code === 0 && res.data.data) {
        const taskResult = res.data.data.output
        if (!taskResult) {
          return
        }
        if (taskResult.taskStatus === 'SUCCEEDED') {
          message.success('扩图成功')
          resultImageUrl.value = taskResult.outputImageUrl || ''
          // 清理轮询
          clearPolling()
        } else if (taskResult.taskStatus === 'FAILED') {
          message.error('扩图失败' + (taskResult.message ? ', ' + taskResult.message : ''))
          // 清理轮询
          clearPolling()
        }
      }
    } catch (error) {
      if (error instanceof Error) {
        message.error('扩图失败, ' + error.message)
      } else {
        message.error('扩图失败')
      }
      // 清理轮询
      clearPolling()
    }
  }, 3000) // 每 3 秒轮询一次
}

/**
 * 停止轮询
 */
const clearPolling = () => {
  if (pollingTimer) {
    clearInterval(pollingTimer)
    pollingTimer = null
    taskId.value = undefined
  }
}

/**
 * 应用扩图结果
 */
const handleUpload = async () => {
  loading.value = true
  try {
    const params: API.ImageUploadRequest = {
      fileUrl: resultImageUrl.value,
      spaceId: props.spaceId,
    }

    if (props.imageVO) {
      params.id = String(props.imageVO.id)
    }
    const res = await uploadImageByUrlUsingPost(params)
    if (res.data.code == 0 && res.data.data) {
      message.success('图片上传成功')
      props.onSuccess?.(res.data.data)
      closeModal()
    } else {
      message.error('图片上传失败,' + res.data.message)
    }
  } catch (e) {
    let errorMessage = '图片上传失败'

    if (e instanceof Error) {
      errorMessage += ', ' + e.message
    }

    message.error(errorMessage)
  }
  loading.value = false
}

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
</script>

<style>
.image-expand {
  text-align: center;
}
</style>
