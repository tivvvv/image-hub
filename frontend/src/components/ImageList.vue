<template>
  <div class="imageList">
    <a-list
      :grid="{ gutter: 16, xs: 1, sm: 2, md: 3, lg: 4, xl: 5, xxl: 6 }"
      :data-source="dataList"
      :loading="loading"
    >
      <template #renderItem="{ item: imageVO }">
        <a-list-item style="padding: 0">
          <!-- 单张图片 -->
          <a-card hoverable @click="doClickImage(imageVO)">
            <!-- 图片封面 -->
            <template #cover>
              <img
                :alt="imageVO.imageName"
                :src="imageVO.thumbnailUrl ?? imageVO.imageUrl"
                style="height: 200px; object-fit: contain"
              />
            </template>
            <a-card-meta :title="imageVO.imageName">
              <template #description>
                <a-flex>
                  <a-tag color="blue">
                    {{ imageVO.imageCategory ?? '默认分类' }}
                  </a-tag>
                  <a-tag v-for="tag in imageVO.imageTagList" :key="tag">
                    {{ tag }}
                  </a-tag>
                </a-flex>
              </template>
            </a-card-meta>
            <template v-if="showOperation" #actions>
              <download-outlined @click="(e: MouseEvent) => doDownload(imageVO, e)" />
              <edit-outlined @click="(e: MouseEvent) => doEdit(imageVO, e)" />
              <delete-outlined @click="(e: MouseEvent) => doDelete(imageVO, e)" />
              <share-alt-outlined @click="(e: MouseEvent) => doShare(imageVO, e)" />
            </template>
          </a-card>
        </a-list-item>
      </template>
    </a-list>
    <share-model ref="shareModalRef" :link="shareLink" />
  </div>
</template>

<script setup lang="ts">
import { downloadImage } from '@/utils/imageUtil.ts'
import {
  DeleteOutlined,
  DownloadOutlined,
  EditOutlined,
  ShareAltOutlined,
} from '@ant-design/icons-vue'
import { useRouter } from 'vue-router'
import { deleteImageUsingDelete } from '@/api/imageController.ts'
import { message } from 'ant-design-vue'
import ShareModel from '@/components/ShareModel.vue'
import { ref } from 'vue'

interface Props {
  dataList?: API.ImageVO[]
  loading?: boolean
  showOperation?: boolean
}

const emit = defineEmits(['refresh'])
const router = useRouter()
const props = withDefaults(defineProps<Props>(), {
  dataList: () => [],
  loading: false,
  showOperation: false,
})

// 点击图片跳转详情页
const doClickImage = (imageVO: API.ImageVO) => {
  router.push({
    path: `/image/${imageVO.id}`,
  })
}

// 下载
const doDownload = (imageVO: API.ImageVO, e: MouseEvent) => {
  // 阻止事件冒泡
  e.stopPropagation()
  downloadImage(imageVO.imageUrl)
}

// 编辑
const doEdit = (imageVO: API.ImageVO, e: MouseEvent) => {
  // 阻止事件冒泡
  e.stopPropagation()
  router.push({
    path: '/image/add',
    query: {
      id: imageVO.id,
      spaceId: imageVO.spaceId,
    },
  })
}

// 删除
const doDelete = async (imageVO: API.ImageVO, e: MouseEvent) => {
  // 阻止事件冒泡
  e.stopPropagation()
  const id = imageVO.id?.toString()
  if (!id) {
    return
  }
  const res = await deleteImageUsingDelete({ id })
  if (res.data.code === 0) {
    message.success('删除成功')
    emit('refresh')
  } else {
    message.error('删除失败')
  }
}

const shareModalRef = ref()
const shareLink = ref<string>()
const doShare = (imageVO: API.ImageVO, e: MouseEvent) => {
  // 阻止冒泡
  e.stopPropagation()
  shareLink.value = `${window.location.protocol}//${window.location.host}/image/${imageVO.id}`
  if (shareModalRef.value) {
    shareModalRef.value.openModal()
  }
}
</script>

<style scoped></style>
