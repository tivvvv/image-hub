<template>
  <div class="urlImageUpload">
    <a-input-group compact>
      <a-input
        v-model:value="fileUrl"
        style="width: calc(100% - 80px)"
        placeholder="请输入图片地址"
      />
      <a-button type="primary" style="width: 80px" :loading="loading" @click="handleUpload">
        提交
      </a-button>
    </a-input-group>
    <div class="img-wrapper">
      <img v-if="image?.imageUrl" :src="image?.imageUrl" alt="avatar" />
    </div>
  </div>
</template>
<script lang="ts" setup>
import { ref } from 'vue'
import { message } from 'ant-design-vue'
import { uploadImageByUrlUsingPost } from '@/api/imageController.ts'

interface Props {
  image?: API.ImageVO
  spaceId?: string
  onSuccess?: (newImage: API.ImageVO) => void
}

const props = defineProps<Props>()
const fileUrl = ref<string>()
const loading = ref<boolean>(false)

/**
 * 上传图片
 * @param file
 */
const handleUpload = async () => {
  loading.value = true
  try {
    const params: API.ImageUploadRequest = { fileUrl: fileUrl.value }
    params.spaceId = props.spaceId
    if (props.image) {
      params.id = props.image.id
    }
    const res = await uploadImageByUrlUsingPost(params)
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
.urlImageUpload {
  margin-bottom: 16px;
}

.urlImageUpload img {
  max-width: 100%;
  max-height: 500px;
}

.urlImageUpload .img-wrapper {
  text-align: center;
  margin-top: 16px;
}
</style>
