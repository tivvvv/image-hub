<template>
  <div id="imageAddView">
    <h2 style="margin-bottom: 16px">{{ route.query?.id ? '编辑图片' : '上传图片' }}</h2>
    <a-typography-paragraph v-if="spaceId" type="secondary">
      保存至空间: <a :href="`/space/${spaceId}`" target="_blank"> {{ spaceId }} </a>
    </a-typography-paragraph>
    <!-- 选择图片上传方式 -->
    <a-tabs v-model:activeKey="uploadType">
      <a-tab-pane key="file" tab="文件上传">
        <!-- 图片上传组件 -->
        <ImageUpload :image="imageVO" :spaceId="spaceId" :onSuccess="onSuccess" />
      </a-tab-pane>
      <a-tab-pane key="url" tab="URL上传" force-render>
        <!-- URL图片上传组件 -->
        <UrlImageUpload :image="imageVO" :spaceId="spaceId" :onSuccess="onSuccess" />
      </a-tab-pane>
    </a-tabs>
    <!-- 图片编辑 -->
    <div v-if="imageVO" class="edit-bar">
      <a-flex justify="center" gap="middle">
        <a-button :icon="h(EditOutlined)" @click="doEditImage">编辑图片</a-button>
        <a-button :icon="h(FullscreenOutlined)" type="primary" @click="doImageExpand">
          AI 扩图
        </a-button>
      </a-flex>
      <ImageCropper
        ref="imageCropperRef"
        :imageUrl="imageVO?.imageUrl"
        :imageVO="imageVO"
        :spaceId="spaceId"
        :space="space"
        :onSuccess="onCropSuccess"
      />
      <ImageExpand
        ref="imageExpandRef"
        :imageVO="imageVO"
        :spaceId="spaceId"
        :onSuccess="onImageOutPaintingSuccess"
      />
    </div>
    <!-- 图片信息表单 -->
    <a-form
      name="imageForm"
      v-if="imageVO"
      layout="vertical"
      :model="imageForm"
      @finish="handleSubmit"
    >
      <a-form-item name="imageName" label="名称">
        <a-input v-model:value="imageForm.imageName" placeholder="请输入名称" allow-clear />
      </a-form-item>

      <a-form-item name="imageIntro" label="简介">
        <a-textarea
          v-model:value="imageForm.imageIntro"
          placeholder="请输入简介"
          :auto-size="{ minRows: 2, maxRows: 5 }"
          allow-clear
        />
      </a-form-item>

      <a-form-item name="imageCategory" label="分类">
        <a-auto-complete
          v-model:value="imageForm.imageCategory"
          placeholder="请输入分类"
          :options="categoryOptions"
          allow-clear
        />
      </a-form-item>

      <a-form-item name="imageTagList" label="标签">
        <a-select
          v-model:value="imageForm.imageTagList"
          mode="tags"
          placeholder="请输入标签"
          :options="tagOptions"
          allow-clear
        />
      </a-form-item>

      <a-form-item>
        <a-button type="primary" html-type="submit" style="width: 100%">创建</a-button>
      </a-form-item>
    </a-form>
  </div>
</template>

<script setup lang="ts">
import ImageUpload from '@/components/ImageUpload.vue'
import { computed, h, onMounted, reactive, ref } from 'vue'
import {
  getImageVoByIdUsingGet,
  listImageTagCategoryUsingGet,
  updateImageUsingPost,
} from '@/api/imageController.ts'
import { message } from 'ant-design-vue'
import { useRoute, useRouter } from 'vue-router'
import UrlImageUpload from '@/components/UrlImageUpload.vue'
import ImageCropper from '@/components/ImageCropper.vue'
import { EditOutlined, FullscreenOutlined } from '@ant-design/icons-vue'
import ImageExpand from '@/components/ImageExpand.vue'

const route = useRoute()
const router = useRouter()
const imageVO = ref<API.ImageVO>()
const imageForm = reactive<API.ImageUpdateRequest>({})
const tagOptions = ref<{ value: string; label: string }[]>([])
const categoryOptions = ref<{ value: string; label: string }[]>([])
const uploadType = ref<'file' | 'url'>('file')
const spaceId = computed(() => {
  return route.query?.spaceId as string
})

/**
 * 提交表单
 * @param values
 */
const handleSubmit = async (values: any) => {
  console.log(values)
  if (!imageVO.value) {
    return
  }
  const imageId = imageVO.value.id

  if (!imageId) {
    return
  }
  const res = await updateImageUsingPost({
    id: imageId,
    spaceId: spaceId.value,
    ...values,
  })
  // 操作成功
  if (res.data.code === 0 && res.data.data) {
    message.success('图片添加成功,请等待审核')
    // 根据空间类型跳转
    if (spaceId.value) {
      await router.push(`/space/${spaceId.value}`)
    } else {
      await router.push('/')
    }
  } else {
    message.error('图片添加失败,' + res.data.message)
  }
}

const onSuccess = (newImage: API.ImageVO) => {
  imageVO.value = newImage
  imageForm.imageName = newImage.imageName
}

onMounted(() => {
  getTagAndCategoryOptions()
  getExistedImage()
})

/**
 * 获取默认标签和分类选项
 * @param values
 */
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

// 获取指定图片信息
const getExistedImage = async () => {
  // 获取图片id
  const id = route.query?.id as string
  if (id) {
    if (id === 'others' || isNaN(parseInt(id))) {
      return
    }

    const res = await getImageVoByIdUsingGet({
      id,
    })
    if (res.data.code === 0 && res.data.data) {
      const data = res.data.data
      imageVO.value = data
      imageForm.imageName = data.imageName
      imageForm.imageIntro = data.imageIntro
      imageForm.imageCategory = data.imageCategory
      imageForm.imageTagList = data.imageTagList
    }
  }
}
const space = ref<API.SpaceVO>()

const imageCropperRef = ref()
// 编辑图片
const doEditImage = async () => {
  imageCropperRef.value?.openModal()
}
// 编辑成功事件
const onCropSuccess = (newImage: API.ImageVO) => {
  imageVO.value = newImage
}

const imageExpandRef = ref()
// 打开 AI 扩图弹窗
const doImageExpand = async () => {
  imageExpandRef.value?.openModal()
}

// AI 扩图保存事件
const onImageOutPaintingSuccess = (newImage: API.ImageVO) => {
  imageVO.value = newImage
}
</script>

<style scoped>
#imageAddView {
  max-width: 720px;
  margin: 0 auto;
}

#imageAddView .edit-bar {
  display: flex;
  justify-content: center;
  margin: 16px 0;
}
</style>
