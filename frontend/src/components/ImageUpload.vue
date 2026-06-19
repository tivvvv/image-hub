<template>
  <div class="imageUpload">
    <a-upload
      list-type="image-card"
      :show-upload-list="false"
      :custom-request="handleUpload"
      :before-upload="beforeUpload"
    >
      <img v-if="image?.imageUrl" :src="image?.imageUrl" alt="avatar" />
      <div v-else>
        <loading-outlined v-if="loading"></loading-outlined>
        <plus-outlined v-else></plus-outlined>
        <div class="ant-upload-text">点击或拖拽上传图片</div>
      </div>
    </a-upload>
  </div>
</template>
<script lang="ts" setup>
import { ref } from 'vue'
import { LoadingOutlined, PlusOutlined } from '@ant-design/icons-vue'
import { message, type UploadFile } from 'ant-design-vue'
import { uploadImageUsingPost } from '@/api/imageController.ts'

interface Props {
  image?: API.ImageVO
  spaceId?: string
  onSuccess?: (newImage: API.ImageVO) => void
}

const props = defineProps<Props>()

const loading = ref<boolean>(false)

/**
 * 上传文件之前校验
 * @param file
 */
const beforeUpload = (file: UploadFile<unknown>) => {
  // 校验图片格式
  const isJpgOrPng = file.type === 'image/jpeg' || file.type === 'image/png'
  if (!isJpgOrPng) {
    message.error('图片格式不支持,请使用jpg或png')
  }
  // 校验图片大小
  const isLt2M = (file.size ?? 0) / 1024 / 1024 <= 10
  if (!isLt2M) {
    message.error('仅支持上传10M以内的图片')
  }
  return isJpgOrPng && isLt2M
}

/**
 * 上传图片
 * @param file
 */
const handleUpload = async ({ file }: any) => {
  loading.value = true
  try {
    const params: API.uploadImageUsingPOSTParams = props.image ? { id: props.image.id } : {}
    params.spaceId = props.spaceId
    const res = await uploadImageUsingPost(params, {}, file)
    if (res.data.code == 0 && res.data.data) {
      message.success('图片上传成功')
      props.onSuccess?.(res.data.data)
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
</script>

<style scoped>
.imageUpload :deep(.ant-upload) {
  width: 100% !important;
  height: 100% !important;
  min-height: 200px;
  min-width: 200px;
}

.imageUpload img {
  display: block;
  margin: 0 auto;
  max-width: 100%;
  max-height: 500px;
}

.ant-upload-select-image-card i {
  font-size: 64px;
  color: #999;
}

.ant-upload-select-image-card .ant-upload-text {
  margin-top: 12px;
  font-size: 18px;
  color: #666;
}
</style>
