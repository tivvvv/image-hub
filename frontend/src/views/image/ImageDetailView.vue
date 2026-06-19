<template>
  <div id="imageDetailView">
    <a-row :gutter="[16, 16]">
      <!-- 图片预览 -->
      <a-col :sm="24" :md="16" :xl="18">
        <a-card title="图片预览">
          <a-image :src="imageVO.imageUrl" style="max-height: 600px; object-fit: contain" />
        </a-card>
      </a-col>

      <!-- 图片信息区域 -->
      <a-col :sm="24" :md="8" :xl="6">
        <a-card title="图片信息">
          <a-descriptions :column="1">
            <a-descriptions-item label="作者">
              <a-space>
                <a-avatar :size="24" :src="imageVO.userVO?.userAvatar" />
                <div>{{ imageVO.userVO?.userName }}</div>
              </a-space>
            </a-descriptions-item>
            <a-descriptions-item label="名称">
              {{ imageVO.imageName ?? '未命名' }}
            </a-descriptions-item>
            <a-descriptions-item label="简介">
              {{ imageVO.imageIntro ?? '-' }}
            </a-descriptions-item>
            <a-descriptions-item label="分类">
              {{ imageVO.imageCategory ?? '默认' }}
            </a-descriptions-item>
            <a-descriptions-item label="标签">
              <a-tag v-for="tag in imageVO.imageTagList" :key="tag">
                {{ tag }}
              </a-tag>
            </a-descriptions-item>
            <a-descriptions-item label="格式">
              {{ imageVO.imageFormat ?? '-' }}
            </a-descriptions-item>
            <a-descriptions-item label="宽度">
              {{ imageVO.imageWidth ?? '-' }}
            </a-descriptions-item>
            <a-descriptions-item label="高度">
              {{ imageVO.imageHeight ?? '-' }}
            </a-descriptions-item>
            <a-descriptions-item label="宽高比">
              {{ imageVO.imageScale ?? '-' }}
            </a-descriptions-item>
            <a-descriptions-item label="大小">
              {{ formatSize(imageVO.imageSize) }}
            </a-descriptions-item>
          </a-descriptions>

          <!-- 图片操作 -->
          <a-space wrap>
            <a-button type="primary" @click="doDownload">
              下载
              <template #icon>
                <DownloadOutlined />
              </template>
            </a-button>
            <a-button :icon="h(ShareAltOutlined)" type="primary" ghost @click="doShare">
              分享
            </a-button>
            <a-button v-if="canEdit" :icon="h(EditOutlined)" type="default" @click="doEdit">
              编辑
            </a-button>
            <a-button v-if="canEdit" :icon="h(DeleteOutlined)" danger @click="doDelete">
              删除
            </a-button>
          </a-space>
        </a-card>
      </a-col>
    </a-row>
    <share-model ref="shareModalRef" :link="shareLink" />
  </div>
</template>

<script setup lang="ts">
import { computed, h, onMounted, ref } from 'vue'
import { deleteImageUsingDelete, getImageVoByIdUsingGet } from '@/api/imageController.ts'
import { message } from 'ant-design-vue'
import { downloadImage, formatSize } from '@/utils/imageUtil.ts'
import {
  DeleteOutlined,
  DownloadOutlined,
  EditOutlined,
  ShareAltOutlined,
} from '@ant-design/icons-vue'
import { useRouter } from 'vue-router'
import { useLoginUserStore } from '@/stores/useLoginUserStore.ts'
import ShareModel from '@/components/ShareModel.vue'

const imageVO = ref<API.ImageVO>({})
const router = useRouter()
const loginUserStore = useLoginUserStore()
const props = defineProps<Props>()

interface Props {
  id: string
}

// 获取图片详情
const fetchImageDetail = async () => {
  if (props.id === 'others' || isNaN(parseInt(props.id))) {
    return
  }

  const res = await getImageVoByIdUsingGet({
    id: props.id,
  })
  if (res.data.code === 0 && res.data.data) {
    imageVO.value = res.data.data
  } else {
    message.error('获取图片详情失败,' + res.data.message)
  }
}

// 页面加载时请求一次
onMounted(() => {
  fetchImageDetail()
})

// 只有创建人或管理员可编辑
const canEdit = computed(() => {
  const loginUser = loginUserStore.loginUser
  if (!loginUser.id) {
    return false
  }
  const user = imageVO.value.userVO || {}
  return loginUser.id === user.id || loginUser.userRole === 'admin'
})

// 下载
const doDownload = () => {
  downloadImage(imageVO.value.imageUrl)
}

// 分享
const shareModalRef = ref()
const shareLink = ref<string>()
const doShare = () => {
  shareLink.value = `${window.location.protocol}//${window.location.host}/image/${imageVO.value.id}`
  if (shareModalRef.value) {
    shareModalRef.value.openModal()
  }
}

// 编辑
const doEdit = () => {
  router.push({
    path: '/image/add',
    query: {
      id: imageVO.value.id,
      spaceId: imageVO.value.spaceId,
    },
  })
}

// 删除
const doDelete = async () => {
  const id = imageVO.value?.id?.toString()
  if (!id) {
    return
  }
  const res = await deleteImageUsingDelete({ id })
  if (res.data.code === 0) {
    message.success('删除成功')
  } else {
    message.error('删除失败')
  }
}
</script>

<style scoped>
#imageDetailView {
}
</style>
