import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import UserRegisterView from '@/views/user/UserRegisterView.vue'
import UserLoginView from '@/views/user/UserLoginView.vue'
import UserManageView from '@/views/admin/UserManageView.vue'
import ImageAddView from '@/views/image/ImageAddView.vue'
import ImageManageView from '@/views/admin/ImageManageView.vue'
import ImageDetailView from '@/views/image/ImageDetailView.vue'
import ImageFetchView from '@/views/admin/ImageFetchView.vue'
import SpaceManageView from '@/views/admin/SpaceManageView.vue'
import SpaceAddView from '@/views/space/SpaceAddView.vue'
import MySpaceView from '@/views/space/MySpaceView.vue'
import SpaceDetailView from '@/views/space/SpaceDetailView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView,
    },
    {
      path: '/user/register',
      name: '用户注册',
      component: UserRegisterView,
    },
    {
      path: '/user/login',
      name: '用户登录',
      component: UserLoginView,
    },
    {
      path: '/admin/userManage',
      name: '用户管理',
      component: UserManageView,
    },
    {
      path: '/image/add',
      name: '上传图片',
      component: ImageAddView,
    },
    {
      path: '/space/add',
      name: '创建空间',
      component: SpaceAddView,
    },
    {
      path: '/my/space',
      name: '我的空间',
      component: MySpaceView,
    },
    {
      path: '/admin/imageManage',
      name: '图片管理',
      component: ImageManageView,
    },
    {
      path: '/admin/spaceManage',
      name: '空间管理',
      component: SpaceManageView,
    },
    {
      path: '/admin/image/fetch',
      name: '抓取图片',
      component: ImageFetchView,
    },
    {
      path: '/image/:id',
      name: '图片详情',
      component: ImageDetailView,
      props: true,
    },
    {
      path: '/space/:id',
      name: '空间详情',
      component: SpaceDetailView,
      props: true,
    },
  ],
})

export default router
