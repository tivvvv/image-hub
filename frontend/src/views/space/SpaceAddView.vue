<template>
  <div id="spaceAddView">
    <h2 style="margin-bottom: 16px">{{ route.query?.id ? '编辑空间' : '创建空间' }}</h2>
    <!-- 空间信息表单 -->
    <a-form name="spaceForm" layout="vertical" :model="spaceForm" @finish="handleSubmit">
      <a-form-item name="spaceName" label="空间名称">
        <a-input v-model:value="spaceForm.spaceName" placeholder="请输入空间名称" allow-clear />
      </a-form-item>
      <a-form-item name="spaceLevel" label="空间级别">
        <a-select
          v-model:value="spaceForm.spaceLevel"
          style="min-width: 180px"
          placeholder="请选择空间级别"
          :options="SPACE_LEVEL_OPTIONS"
          allow-clear
        />
      </a-form-item>
      <a-form-item>
        <a-button type="primary" html-type="submit" :loading="loading" style="width: 100%"
          >提交
        </a-button>
      </a-form-item>
    </a-form>
    <a-card title="空间级别介绍">
      <a-typography-paragraph>
        * 目前仅支持开通
        <a-typography-text strong>普通版</a-typography-text>
        , 如需升级空间, 请联系
        <a href="https://tivvvv.com" target="_blank">系统管理员</a>
      </a-typography-paragraph>
      <a-typography-paragraph v-for="spaceLevel in spaceLevelList">
        <a-typography-text strong>{{ spaceLevel.desc }}:</a-typography-text>
        大小
        <a-typography-text strong>{{ formatSize(spaceLevel.baseMaxSize) }}</a-typography-text>
        , 数量
        <a-typography-text strong>{{ spaceLevel.baseMaxCount }}</a-typography-text>
        张
      </a-typography-paragraph>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import {
  addSpaceUsingPost,
  getSpaceLevelUsingGet,
  getSpaceVoByIdUsingGet,
  updateSpaceUsingPost,
} from '@/api/spaceController.ts'
import { message } from 'ant-design-vue'
import { useRoute, useRouter } from 'vue-router'
import { SPACE_LEVEL_OPTIONS } from '@/constants/spaceConstant.ts'
import { formatSize } from '@/utils/imageUtil.ts'

const loading = ref(false)
const route = useRoute()
const router = useRouter()
const spaceVO = ref<API.SpaceVO>()
const spaceForm = reactive<API.SpaceAddRequest | API.SpaceUpdateRequest>({})
const spaceLevelList = ref<API.SpaceLevelVO[]>([])

/**
 * 提交表单
 * @param values
 */
const handleSubmit = async (values: any) => {
  const spaceId = spaceVO.value?.id
  loading.value = true

  let res
  if (spaceId) {
    // 编辑
    res = await updateSpaceUsingPost({
      id: spaceId,
      ...values,
    })
  } else {
    // 创建
    res = await addSpaceUsingPost({
      ...values,
    })
  }
  // 操作成功
  if (res.data.code === 0 && res.data.data) {
    message.success('操作成功')
    // 跳转到空间详情页
    await router.push({
      path: `/space/${res.data.data.id}`,
    })
  } else {
    message.error('操作失败,' + res.data.message)
  }
  loading.value = false
}

// 获取指定空间信息
const getExistedSpace = async () => {
  // 获取空间id
  const id = route.query?.id as string
  if (id) {
    const res = await getSpaceVoByIdUsingGet({
      id,
    })
    if (res.data.code === 0 && res.data.data) {
      const data = res.data.data
      spaceVO.value = data
      spaceForm.spaceName = data.spaceName
      spaceForm.spaceLevel = data.spaceLevel
    }
  }
}

const getSpaceLevelList = async () => {
  const res = await getSpaceLevelUsingGet()
  if (res.data.code === 0 && res.data.data) {
    spaceLevelList.value = res.data.data
  } else {
    message.error('获取空间级别列表失败,' + res.data.message)
  }
}

onMounted(() => {
  getExistedSpace()
  getSpaceLevelList()
})
</script>

<style scoped>
#spaceAddView {
  max-width: 720px;
  margin: 0 auto;
}
</style>
